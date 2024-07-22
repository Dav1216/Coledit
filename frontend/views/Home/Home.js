"use client";

import NoteList from '../../components/NoteList';
import { getCookie } from 'cookies-next';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

function Home() {
  const [userEmail, setUserEmail] = useState(null);

  useEffect(() => {
    const userEmailFromCookie = getCookie('email');
    console.log("jere2");
    console.log(userEmailFromCookie);
    if (userEmailFromCookie) {
      console.log("jere");
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