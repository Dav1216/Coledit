import React from 'react';
import { useState, useEffect } from 'react';
import Note from './Note';

function NoteList(props) {
  const [notes, setNotes] = useState([]);
  const [selectedNote, setSelectedNote] = useState(null);

  useEffect(() => {
    const fetchNotes = async () => {
      try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/getByUserEmail/${props.userEmail}`);
        const data = await response.json();

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
     const intervalId = setInterval(fetchNotes, 3000); // Fetch notes every 30 seconds

     return () => clearInterval(intervalId); // Cleanup interval on component unmount
  }, [props.userEmail]);

  const selectNote = (note) => {
    setSelectedNote(note === selectedNote ? null : note);
  };

  const updateSelectedNote = (updatedNote) => {
    setSelectedNote(updatedNote);
    setNotes((prevNotes) =>
      prevNotes.map((note) => (note.id === updatedNote.id ? updatedNote : note))
    );
  };

    // Log the notes state whenever it changes
    useEffect(() => {
      console.log("Updated notes:", notes);
    }, [notes]);

  return (
    <div>
      <ul>
        {notes.map(note => (
            <li key={note.id} onClick={() => selectNote(note)}>
              {note.title}
            </li>
        ))}
      </ul>
      {selectedNote ? (
        <Note note={selectedNote} setNote={updateSelectedNote} />
      ) : (
        <p>Select a note to view its content</p>
      )}
    </div>
  );
};

export default NoteList;