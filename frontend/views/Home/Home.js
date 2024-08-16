"use client";

import React, { useState, useEffect } from 'react';
import NoteList from './../../components/NoteList';
import { getCookie } from 'cookies-next';
import { useRouter } from 'next/navigation';
import authenticationService from '../../services/authenticationService';
import UserContext from './../../contexts/UserContext'
import './Home.css';

function Home() {
  const [userEmail, setUserEmail] = useState(null);
  const [userId, setUserId] = useState(null);
  const [showLogoutButton, setShowLogoutButton] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const userEmailFromCookie = getCookie('email');
    const userIdFromCookie = getCookie('userId');

    if (userEmailFromCookie && userIdFromCookie) {
      setUserEmail(userEmailFromCookie);
      setUserId(userIdFromCookie);
      setShowLogoutButton(true);
    } else {
      router.push('/log-in');
    }

    // hard timeout since jwt will also expire after this time
    const timeoutId = setTimeout(handleLogout, 25 * 60 * 1000); 

    return () => {
      clearTimeout(timeoutId);
    };
  }, []);

  const handleLogout = async () => {
    await authenticationService.sendLogOutRequest();
    setShowLogoutButton(false);
    router.push('/log-in');
  };

  return (
    <div className='container'>
      <h1>ColEdit</h1>
      {showLogoutButton && (
        <button onClick={handleLogout}>Log Out</button>
      )}
      {userEmail && userId && (
        <UserContext.Provider value={{ userEmail, userId }}>
          <NoteList />
        </UserContext.Provider>
      )}
    </div>
  );
};

export default Home;