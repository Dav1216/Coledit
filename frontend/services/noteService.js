const fetchNotesByUserEmail = async (userEmail) => {
    try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/getByUserEmail/${userEmail}`);
        if (response.ok) {
            const data = await response.json();

            if (!Array.isArray(data)) {
                throw new Error('Fetched data is not an array');
            }

            return data;
        } else {
            throw new Error('Failed to fetch notes for user');
        }
    } catch (error) {
        console.error('Error fetching notes:', error);
        throw error;
    }
};

const fetchCollaborators = async (noteId) => {
    try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/getCollaborators/${noteId}`);
        if (response.ok) {
            const data = await response.json();

            if (!Array.isArray(data)) {
                throw new Error('Fetched data is not an array');
            }

            return data;
        } else {
            throw new Error('Failed to fetch collaborators');
        }
    } catch (error) {
        throw error;
    }
};

const addUserByEmail = async (noteId, userEmail) => {
    try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/addCollaborator?noteId=${noteId}&userEmail=${userEmail}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error('Failed to add collaborator');
        }
    } catch (error) {
        throw error;
    }
};

const removeUserByEmail = async (noteId, userEmail) => {
    try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/removeCollaborator?noteId=${noteId}&userEmail=${userEmail}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error('Failed to remove collaborator');
        }
    } catch (error) {
        throw error;
    }
};

const createNote = async (data, userEmail) => {
    try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/create/${userEmail}`, {
            method: 'POST',
            body: JSON.stringify(data),
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error('Failed to add collaborator');
        }
    } catch (error) {
        throw error;
    }
};


const deleteNote = async (noteId) => {
    try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/delete/${noteId}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Failed to delete note');
        }
    } catch (error) {
        throw error;
    }
};

const initializeWebSocket = (noteId, note, setNote, socketRef, heartbeatIntervalRef, versionNumberRef, lastContentFromServerRef) => {
    // Initialize WebSocket connection
    socketRef.current = new WebSocket(`wss://localhost/wsapp/document/${noteId}`);

    socketRef.current.onopen = () => {
        // console.log('Connected to WebSocket');

        // Start sending heartbeat messages every 30 seconds
        heartbeatIntervalRef.current = setInterval(() => {
            if (socketRef.current.readyState === WebSocket.OPEN) {
                socketRef.current.send(JSON.stringify({ type: 'heartbeat' }));
            }
        }, 30000);

        socketRef.current.onmessage = (event) => {
            try {
                // Assuming the server sends JSON formatted messages
                const data = JSON.parse(event.data);

                if (data.type === 'updateNotification') {
                    const updatedNote = {
                        ...note,
                        content: data.payload
                    };

                    lastContentFromServerRef.current = updatedNote.content;
                    setNote(updatedNote);
                    versionNumberRef.current = data.version;
                }
            } catch (error) {
                console.error('Error parsing message:', error);
            }
        };

        socketRef.current.onerror = (error) => {
            console.error('WebSocket Error:', error);
        };

        socketRef.current.onclose = () => {
            // console.log('Disconnected from WebSocket');
        };
    };

    return () => {
        // Clean up on component unmount
        if (socketRef.current) {
            socketRef.current.close();
        }
        clearInterval(heartbeatIntervalRef.current);
    };
};

const sendWebSocketMessage = (note, socketRef, versionNumberRef) => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN && note) {
        const message = JSON.stringify({
            type: 'updateNote',
            payload: note.content,
            version: versionNumberRef.current
        });
        socketRef.current.send(message);
    }
};


export default {
    fetchNotesByUserEmail,
    fetchCollaborators,
    addUserByEmail,
    initializeWebSocket,
    sendWebSocketMessage,
    createNote,
    removeUserByEmail,
    deleteNote
};
