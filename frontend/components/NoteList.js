import React from 'react';
import { useState, useEffect } from 'react';
import Note from './Note';

function NoteList(props) {
  const [notes, setNotes] = useState([]);
  const [selectedNote, setSelectedNote] = useState(null);

  useEffect(() => {
    const fetchNotes = async () => {
      try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/getByUser/${props.userId}`);
        const data = await response.json();
        
        // Log the data to inspect its structure
        console.log('Fetched notes:', data);

        // Ensure data is an array before setting it to state
        if (Array.isArray(data)) {
          setNotes(data);
        } else {
          console.error('Fetched data is not an array:', data);
        }
      } catch (error) {
        console.error('Error fetching notes:', error);
      }
    };
    fetchNotes();
  }, [props.userId]);

  const selectNote = (note) => {
    setSelectedNote(note);
  };

  return (
    <div>
      <ul>
        {notes.map(note => (
           <li key={note.id} onClick={() => selectNote(note)}>{note.title}</li>
        ))}
      </ul>
      {selectedNote ? (
          <Note note={selectedNote} />
        ) : (
          <p>Select a note to view its content</p>
        )}
    </div>
  );
};

export default NoteList;