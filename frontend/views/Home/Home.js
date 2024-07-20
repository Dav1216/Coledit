"use client";

import NoteList from '../../components/NoteList';
import { getCookie } from 'cookies-next';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

function Home() {
  const [userId, setUserId] = useState(null);

  useEffect(() => {
    const userIdFromCookie = getCookie('id');
    console.log(userIdFromCookie)
    if (userIdFromCookie) {
      setUserId(userIdFromCookie);
    }
  }, []);

  return (
    <div>
      <h1>ColEdit</h1>
      <NoteList userId={userId} />
    </div>
  );
};

export default Home;