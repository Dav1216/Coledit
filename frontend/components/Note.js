import React, { useState, useEffect } from 'react';

function Note(props) {
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const [users, setUsers] = useState([]);
  const [isUserListVisible, setIsUserListVisible] = useState(false);

  useEffect(() => {
    fetchCollaborators();
  }, [isUserListVisible]);

  const fetchCollaborators = async () => {
    const noteId = props.note.noteId; // Assuming the note ID is part of the props

    try {
      const response = await fetch(`https://${process.env.HOSTNAME}/api/note/getCollaborators/${noteId}`);
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
      <p>{props.note.content}</p>
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
