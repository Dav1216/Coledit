import React, { useState, useEffect, useRef, useContext } from 'react';
import noteService from './../services/noteService';
import UserContext from '../contexts/UserContext';

const UNINITIALIZED_VALUE = "__UNINITIALIZED__";

function Note({ note, setNote, fetchNotes }) {
  const { userEmail, userId } = useContext(UserContext);

  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const [users, setUsers] = useState([]);
  const [isCollaboratorListVisible, setIsCollaboratorListVisible] = useState(false);

  const socketRef = useRef();
  const heartbeatIntervalRef = useRef();
  const versionNumberRef = useRef(0);
  const lastContentFromServerRef = useRef(UNINITIALIZED_VALUE); // Track the last received content from server for this note

  useEffect(() => {
    const cleanup = noteService.initializeWebSocket(note.noteId, note, setNote, socketRef, heartbeatIntervalRef, versionNumberRef, lastContentFromServerRef);

    return () => { cleanup(); versionNumberRef.current = 0; };
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
    console.log("note.content", note.content);
    console.log("last content from server", lastContentFromServerRef.current);
    
    if (note.content === "" && lastContentFromServerRef.current === UNINITIALIZED_VALUE) {
      return;
    }

    if (note.content !== lastContentFromServerRef.current) {
      versionNumberRef.current++;
      console.log("versionNumberRef.current ", versionNumberRef.current);
      noteService.sendWebSocketMessage(note, socketRef, versionNumberRef);
      lastContentFromServerRef.current = note.content;
    }
  }, [note.content]);


  const addUserByEmail = async (e) => {
    e.preventDefault();
    const email = e.target.elements.email.value;
    const noteId = note.noteId;
    if (userId === note.owner) {
      try {
        await noteService.addUserByEmail(noteId, email);
        await fetchCollaborators();
      } catch (error) {
        console.error('Error:', error);
      } finally {
        setIsPopupOpen(false);
      }
    } else {
      alert("Only the owner can add users to this note.");
    }
  };

  const removeUserByEmail = async (email) => {
    const noteId = note.noteId;
    if (userId === note.owner || email === userEmail) {
      try {
        await noteService.removeUserByEmail(noteId, email);
        fetchCollaborators();
        fetchNotes();
      } catch (error) {
        console.error('Error:', error);
      }
    } else {
      alert("Only the owner or the user themselves can remove the user from this note.");
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

  const handleRemoveOnClick = (userEmail) => {
    removeUserByEmail(userEmail);
    setIsCollaboratorListVisible(false);
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
              <button onClick={() =>
                handleRemoveOnClick(user.email)
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
