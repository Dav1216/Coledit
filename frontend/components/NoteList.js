import React from 'react';
import { useState, useEffect } from 'react';
import Note from './Note';
import noteService from './../services/noteService';

function NoteList(props) {
  const [notes, setNotes] = useState([]);
  const [selectedNote, setSelectedNote] = useState(null);
  const [isOpen, setIsOpen] = useState(false)

  const fetchNotes = async () => {
    try {
      let fetchedNotes = await noteService.fetchNotesByUserEmail(props.userEmail);
      fetchedNotes.sort((a, b) => a.noteId - b.noteId);
      setNotes(fetchedNotes);
    } catch (error) {
      console.error('Error fetching notes:', error);
    }
  };
  
  useEffect(() => {
    console.log("here");
    fetchNotes();
  }, [isOpen, props.userEmail, props.popup]);

  const updateSelectedNote = (updatedNote) => {
    setSelectedNote(updatedNote);
    setNotes((prevNotes) =>
      prevNotes.map((note) => (note.noteId === updatedNote.noteId ? updatedNote : note))
        .sort((a, b) => a.noteId - b.noteId)
    );
  };

  return (
    <div>
      <ul>
        {notes.map(note => (
          <li key={note.id} onClick={() => { setIsOpen(!isOpen); setSelectedNote(note) }}>
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