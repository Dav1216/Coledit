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
  const [contextMenu, setContextMenu] = useState({ visible: false, noteId: null });

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


  // CONTEXT MENU

  const handleContextMenu = (event, note) => { // Added function to handle right-click
    event.preventDefault();
    setContextMenu({
      visible: true,
      note: note
    });
  };

  const handleDeleteNote = () => { // Added function to handle delete action from context menu
    if (contextMenu.note?.noteId) {
      deleteNote(contextMenu.note.noteId);
      setContextMenu({ visible: false, note: null });
      if (contextMenu.note?.noteId === selectedNote?.noteId) {
        setSelectedNote(null);
      }
    }
  };

  const handleCloseContextMenu = () => { // Added function to close context menu
    setContextMenu({ visible: false, noteId: null });
  };

  useEffect(() => {
    const reloadCount = Number(sessionStorage.getItem('reloadCount')) || 0;
    if (reloadCount < 1) {
      sessionStorage.setItem('reloadCount', String(reloadCount + 1));
      window.location.reload();
    }
  }, []);
  
  return (
    <div className='container' onClick={handleCloseContextMenu}>
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
              <button type="button" onClick={handleClosePopup}>Cancel</button>
            </form>
          </div>
        </div>
      )}
      <ul>
        {notes.map(note => (
          <>
            <li key={note.noteId}
              onContextMenu={(e) => handleContextMenu(e, note)}
              onClick={() => {
                setSelectedNote(prevSelectedNote => prevSelectedNote === null
                  || prevSelectedNote.noteId !== note.noteId ? note : null);
              }}>
              <div className="note-title-wrapper">
                <span className="note-title">{note.title}</span>
              </div>
            </li>
            {(selectedNote?.noteId === note.noteId) && (
              <Note note={selectedNote} setNote={updateSelectedNote} fetchNotes={fetchNotes} setSelectedNote={setSelectedNote} />
            )}
            {contextMenu.visible && note.owner === userId && contextMenu.note.noteId === note.noteId && (
              <div className="context-menu">
                <button onClick={handleDeleteNote}>Delete</button>
              </div>
            )}
          </>
        ))}
      </ul>
    </div>
  );
};

export default NoteList;