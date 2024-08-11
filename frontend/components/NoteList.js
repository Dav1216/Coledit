import React from 'react';
import { useState, useEffect, useRef } from 'react';
import Note from './Note';

function NoteList(props) {
  const [notes, setNotes] = useState([]);
  const [selectedNote, setSelectedNote] = useState(null);
  const [isOpen, setIsOpen] = useState(false)

  const fetchNotes = async () => {
    try {
      const response = await fetch(`https://${process.env.HOSTNAME}/api/note/getByUserEmail/${props.userEmail}`);
      const data = await response.json();
      console.log(data);

      if (Array.isArray(data)) {
        setNotes(data);
      } else {
        console.error('Fetched data is not an array:', data);
      }
    } catch (error) {
      console.error('Error fetching notes:', error);
    }
  };

  useEffect(() => {
    fetchNotes();
    console.log("notes")
    notes.forEach(note => console.log(note))
  }, [isOpen, props.userEmail]);

  const updateSelectedNote = (updatedNote) => {
    setSelectedNote(updatedNote);
    setNotes((prevNotes) =>
      prevNotes.map((note) => (note.id === updatedNote.id ? updatedNote : note))
    );
  };

  return (
    <div>
      <ul>
        {notes.map(note => (
          <li key={note.id} onClick={() => {setIsOpen(!isOpen); setSelectedNote(note)}}>
            {note.title}
          </li>
        ))}
      </ul>
      {isOpen && selectedNote ? (
        <Note note={selectedNote} setNote={updateSelectedNote} />
      ) : (
        <p>Select a note to view its content</p>
      )}
    </div>
  );
};

export default NoteList;