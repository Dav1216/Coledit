import React from 'react';
import { useState, useEffect, useContext } from 'react';
import Note from './Note';
import noteService from './../services/noteService';
import UserContext from '../contexts/UserContext';

function NoteList() {
  const { userEmail, userId } = useContext(UserContext);
  const [notes, setNotes] = useState([]);
  const [selectedNote, setSelectedNote] = useState(null);
  const [noteTitle, setNoteTitle] = useState('');
  const [isPopupVisible, setIsPopupVisible] = useState(false);

  const fetchNotes = async () => {
    try {
      let fetchedNotes = await noteService.fetchNotesByUserEmail(userEmail);
      fetchedNotes.sort((a, b) => a.noteId.localeCompare(b.noteId));
      setNotes(fetchedNotes);
    } catch (error) {
      console.error('Error fetching notes:', error);
    }
  };

  useEffect(() => {
    fetchNotes();
  }, [selectedNote, userEmail]);

  const updateSelectedNote = (updatedNote) => {
    setSelectedNote(updatedNote);
    setNotes((prevNotes) =>
      prevNotes.map((note) => (note.noteId === updatedNote.noteId ? updatedNote : note))
        .sort((a, b) => a.noteId.localeCompare(b.noteId))
    );
  };

  const deleteNote = async (noteId) => {
    try {
      await noteService.deleteNote(noteId);
      setNotes(notes.filter(note => note.noteId !== noteId));
    } catch (error) {
      console.error('Error deleting note:', error);
    }
  };

  const handleAddNote = () => {
    setIsPopupVisible(true);
  };

  const handleSubmitNoteTitle = async () => {
    const data = {
      title: noteTitle
    };

    await noteService.createNote(data, userEmail);
    setNoteTitle('');
    setIsPopupVisible(false);

    fetchNotes();
  };

  const handleClosePopup = () => {
    setIsPopupVisible(false);
  };

  return (
    <div>
      <button onClick={handleAddNote}>Add Note</button>
      {isPopupVisible && (
        <div>
          <form onSubmit={(e) => {
            e.preventDefault();
            handleSubmitNoteTitle();
          }}>
            <input
              type="text"
              placeholder="Enter note title..."
              value={noteTitle}
              onChange={(e) => setNoteTitle(e.target.value)}
            />
            <button type="submit">Save</button>
            <button onClick={handleClosePopup}>Cancel</button>
          </form>
        </div>
      )}
      <ul>
        {notes.map(note => (
          <li key={note.id} onClick={() => { setSelectedNote(prevSelectedNote => prevSelectedNote === null || prevSelectedNote.noteId !== note.noteId ? note : null); }}>
            {note.title}
            <button onClick={(e) => {
              e.stopPropagation(); if (note.owner === userId) {
                deleteNote(note.noteId);
              } else {
                alert("Only the owner can delete this note.");
              }
            }}>Delete</button>
          </li>
        ))}
      </ul>
      {selectedNote ? (
        <Note note={selectedNote} setNote={updateSelectedNote} fetchNotes={fetchNotes} />
      ) : (
        <p>Select a note to view its content</p>
      )}
    </div>
  );
};

export default NoteList;