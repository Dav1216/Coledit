"use client";

import NoteList from '../../components/NoteList';

const HomePage = () => {
  const userId = "2dbb3a5c-b48f-4f92-b20d-992e4a839418";

  return (
    <div>
      <h1>ColEdit</h1>
      <NoteList userId={userId} />
    </div>
  );
};

export default HomePage;