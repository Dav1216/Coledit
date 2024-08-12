"use client";

import NoteList from '../../components/NoteList';
import { getCookie } from 'cookies-next';
import { useRouter } from 'next/navigation';
import React, { useState, useEffect } from 'react';
import authenticationService from '../../services/authenticationService';
import noteService from '../../services/noteService';

function Home() {
  const [userEmail, setUserEmail] = useState(null);
  const [showLogoutButton, setShowLogoutButton] = useState(false); 
  const [isPopupVisible, setIsPopupVisible] = useState(false);
  const [noteTitle, setNoteTitle] = useState(''); 
  const router = useRouter();

  useEffect(() => {
    const userEmailFromCookie = getCookie('email');
    if (userEmailFromCookie) {
      setUserEmail(userEmailFromCookie);
      setShowLogoutButton(true);
    } else {
      router.push('/log-in');
    }
  }, []);

  const handleLogout = async () => {
    await authenticationService.sendLogOutRequest();
    setShowLogoutButton(false); // Hide Log Out button after logout
    router.push('/log-in'); // Redirect to login page
  };

  const handleAddNote = () => {
    setIsPopupVisible(true); // Show popup when "Add Note" button is clicked
  };

  const handleSubmitNoteTitle = async () => {
    const data = {
      title: noteTitle
    };

    noteService.createNote(data, userEmail);
    setNoteTitle('');
    setIsPopupVisible(false); // Close popup after saving
  };

  const handleClosePopup = () => {
    setIsPopupVisible(false); // Close popup without saving
  };

  return (
    <div>
      <h1>ColEdit</h1>
      {showLogoutButton && (
        <>
          <button onClick={handleLogout}>Log Out</button>
          <button onClick={handleAddNote}>Add Note</button>
        </>
      )}
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
      <NoteList userEmail={userEmail} popup={isPopupVisible} />
    </div>
  );
};

export default Home;