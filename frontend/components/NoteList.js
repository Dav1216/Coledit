import React from 'react';
import { useState, useEffect, useContext } from 'react';
import Note from './Note';
import noteService from './../services/noteService';
import UserContext from '../contexts/UserContext';
import './NoteList.css';

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

  // fetch in the background, maybe some other user added the current user as
  // a collaborator to one of their notes
  useEffect(() => {
    const intervalId = setInterval(fetchNotes, 2000);

    return () => {
      clearInterval(intervalId);
    };
  }, [userEmail]);

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
    <div className='container'>
      <button onClick={handleAddNote}>Add Note</button>
      {isPopupVisible && (
        <div className="popup-overlay">
          <div className="popup-form-container">
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
        </div>
      )}
      <ul>
        {notes.map(note => (
          <li key={note.noteId} onClick={() => { setSelectedNote(prevSelectedNote => prevSelectedNote === null || prevSelectedNote.noteId !== note.noteId ? note : null); }}>
            <div className="note-container">
              <div className="note-title-wrapper">
                <span className="note-title">{note.title}</span>
              </div>
              <div className="note-actions">
                {note.owner === userId && (
                  <button className="delete-button" onClick={(e) => {
                    e.stopPropagation();
                    deleteNote(note.noteId);
                    setSelectedNote(null);
                  }}>X</button>
                )}
              </div>
            </div>
          </li>
        ))}
      </ul>
      {selectedNote ? (
        <Note note={selectedNote} setNote={updateSelectedNote} fetchNotes={fetchNotes} setSelectedNote={setSelectedNote} />
      ) : (
        <p>Select a note to view its content here</p>
      )}
    </div>
  );
};

export default NoteList;