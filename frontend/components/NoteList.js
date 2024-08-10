import React from 'react';
import { useState, useEffect, useRef } from 'react';
import Note from './Note';

function NoteList(props) {
  const [notes, setNotes] = useState([]);
  const [selectedNote, setSelectedNote] = useState(null);
  const selectedNoteRef = useRef(selectedNote);
  const firstOpenedNote = useRef(null);

  useEffect(() => {
    selectedNoteRef.current = selectedNote;
  }, [selectedNote]);

  useEffect(() => {
    const fetchNotes = async () => {
      try {
        const response = await fetch(`https://${process.env.HOSTNAME}/api/note/getByUserEmail/${props.userEmail}`);
        const data = await response.json();

        if (Array.isArray(data)) {
          setNotes(data);

          const fetchedSelectedNote = data.find((note) => note.id === selectedNoteRef.current?.id);
          if (firstOpenedNote.current === null) {
            firstOpenedNote.current = fetchedSelectedNote;
            console.log("here")
            console.log(fetchedSelectedNote);
          } else if (fetchedSelectedNote.content !== firstOpenedNote.current.content) {
            alert("Newer version detected, updated your document");
            firstOpenedNote.current = fetchedSelectedNote;

            setSelectedNote((previousNote) => ({ ...previousNote, content: fetchedSelectedNote.content }));
          }
        } else {
          console.error('Fetched data is not an array:', data);
        }
      } catch (error) {
        console.error('Error fetching notes:', error);
      }
    };

    fetchNotes();
    const intervalId = setInterval(fetchNotes, 10000); // Fetch notes every 10 seconds

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