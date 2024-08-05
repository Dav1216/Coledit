import React, { useState, useEffect, useRef } from 'react';

function Note(props) {
  const [note, setNote] = useState(props.note);
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const [users, setUsers] = useState([]);
  const [isUserListVisible, setIsUserListVisible] = useState(false);
  const socketRef = useRef();
  const heartbeatIntervalRef = useRef();

  useEffect(() => {
    // Initialize WebSocket connection
    socketRef.current = new WebSocket(`wss://localhost/wsapp/document/${note.noteId}`);

    socketRef.current.onopen = () => {
      console.log('Connected to WebSocket');

      // Start sending heartbeat messages every 30 seconds
      heartbeatIntervalRef.current = setInterval(() => {
        if (socketRef.current.readyState === WebSocket.OPEN) {
          socketRef.current.send(JSON.stringify({ type: 'heartbeat' }));
        }
      }, 30000);

      socketRef.current.onmessage = (event) => {
        console.log('Received message:', event.data);
        try {
          // Assuming the server sends JSON formatted messages
          const data = JSON.parse(event.data);

          if (data.type === 'updateNotification') {
            const updatedNote = {
              ...note,
              content: data.payload
            };

            setNote(updatedNote);
          }
        } catch (error) {
          console.error('Error parsing message:', error);
        }
      };

      socketRef.current.onerror = (error) => {
        console.error('WebSocket Error:', error);
      };

      socketRef.current.onclose = () => {
        console.log('Disconnected from WebSocket');
      };
    };


    return () => {
      // Clean up on component unmount
      if (socketRef.current) {
        socketRef.current.close();
      }
      clearInterval(heartbeatIntervalRef.current);
    };
  }, []);

  const sendMessage = () => {
    console.log("Sending message");
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN && note) { // Check if both socket connection exists and is open
      // Assuming the server expects JSON formatted messages
      const message = JSON.stringify({
        type: 'updateNote',
        payload: note.content // Include the entire note object in the payload
      });
      socketRef.current.send(message);
    }
  };


  useEffect(() => {
    fetchCollaborators();
  }, [isUserListVisible]);

  const fetchCollaborators = async () => {
    try {
      const response = await fetch(`https://${process.env.HOSTNAME}/api/note/getCollaborators/${note.noteId}`);
      if (response.ok) {
        const collaborators = await response.json();
        setUsers(collaborators);
      } else {
        console.error('Failed to fetch collaborators');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  useEffect(() => {
    sendMessage(); // Broadcast the changes
  }, [note.content]); // Run the effect whenever the note state changes

  const addUserByEmail = async (email) => {
    const noteId = props.note.noteId; // Assuming the note ID is part of the props

    try {
      const response = await fetch(`https://${process.env.HOSTNAME}/api/note/addCollaborator?noteId=${noteId}&userEmail=${email}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (response.ok) {
        const updatedNote = await response.json();
        setIsPopupOpen(false); // Close the popup after adding a user
      } else {
        console.error('Failed to add collaborator');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className='note'>
      <h2>{props.note.title}</h2>
      <textarea
        value={note.content}
        onChange={(e) => setNote((prevNote) => ({
          ...prevNote,
          content: e.target.value,
        }))}
      />
      <button onClick={() => setIsPopupOpen(true)}>Add User</button>
      <button onClick={() => setIsUserListVisible(!isUserListVisible)}>
        Toggle User List
      </button>
      {isUserListVisible && (
        <ul>
          {users.map((user, index) => (
            <li key={index}>{user.email}</li>
          ))}
        </ul>
      )}
      {/* Placeholder for the popup */}
      {isPopupOpen && (
        <div className="popup">
          <form onSubmit={(e) => {
            e.preventDefault();
            const email = e.target.elements.email.value;
            addUserByEmail(email);
          }}>
            <input name="email" type="email" placeholder="Enter email" />
            <button type="submit">Add</button>
          </form>
        </div>
      )}
    </div>
  );
}

export default Note;
