"use client";

import NoteList from '../../components/NoteList';
import { getCookie } from 'cookies-next';
import React, { useState, useEffect } from 'react';
function Home() {
  const [userEmail, setUserEmail] = useState(null);

  useEffect(() => {
    const userEmailFromCookie = getCookie('email');
    if (userEmailFromCookie) {
      setUserEmail(userEmailFromCookie);
    }
  }, []);

  return (
    <div>
      <h1>ColEdit</h1>
      <NoteList userEmail={userEmail} />
    </div>
  );
};

export default Home;