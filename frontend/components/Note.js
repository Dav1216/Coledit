import React, { useState, useEffect, useRef } from 'react';
import noteService from './../services/noteService';

function Note({ note, setNote }) {
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const [users, setUsers] = useState([]);
  const [isCollaboratorListVisible, setIsCollaboratorListVisible] = useState(false);

  const socketRef = useRef();
  const heartbeatIntervalRef = useRef();
  const versionNumberRef = useRef(0);
  const lastContentFromServerRef = useRef(null); // Track the last sent content

  useEffect(() => {
    noteService.initializeWebSocket(note.noteId, note, setNote, socketRef, heartbeatIntervalRef, versionNumberRef, lastContentFromServerRef);
  }, []);

  useEffect(() => {
    fetchCollaborators();
  }, [isCollaboratorListVisible]);

  const fetchCollaborators = async () => {
    try {
      const collaborators = await noteService.fetchCollaborators(note.noteId)
      setUsers(collaborators);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  useEffect(() => {
    if (note.content !== lastContentFromServerRef.current?.content) {
      versionNumberRef.current++;
      noteService.sendWebSocketMessage(note, socketRef, versionNumberRef);
    }
  }, [note.content]);

  const addUserByEmail = async (e) => {
    e.preventDefault();
    const email = e.target.elements.email.value;
    const noteId = note.noteId;

    try {
      noteService.addUserByEmail(noteId, email);
      setIsPopupOpen(false);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const removeUserByEmail = async (email) => {
    const noteId = note.noteId;

    try {
      noteService.removeUserByEmail(noteId, email);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const textAreaOnChange = (e) => {
    const newContent = e.target.value;
    if (newContent.length <= 1000) {
      let updateNote = { ...note, content: newContent }
      setNote(updateNote);
    } else {
      alert("Sorry, but the note cannot exceed 1000 characters.");
      setNote((prevNote) => ({
        ...prevNote,
        content: prevNote.content.slice(0, 999),
      }));
    }
  }

  return (
    <div className='note'>
      <h2>{note.title}</h2>
      <textarea
        value={note.content}
        onChange={textAreaOnChange}
      />
      <button onClick={() => setIsPopupOpen((prev) => !prev)}>Add User</button>
      <button onClick={() => setIsCollaboratorListVisible(!isCollaboratorListVisible)}>
        Toggle User List
      </button>
      {isCollaboratorListVisible && (
        <ul>
          {users.map((user, index) => (
            <li key={index}>{user.email}
              <button onClick={() => {
                removeUserByEmail(user.email);
                setIsCollaboratorListVisible(false);
              }
              }>Remove</button>
            </li>
          ))}
        </ul>
      )}
      {isPopupOpen && (
        <div className="popup">
          <form onSubmit={(e) => addUserByEmail(e)}>
            <input name="email" type="email" placeholder="Enter email" />
            <button type="submit">Add</button>
          </form>
        </div>
      )}
    </div>
  );
}

export default Note;
