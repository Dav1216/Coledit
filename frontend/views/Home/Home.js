"use client";
import React, { useState, useEffect, useRef } from 'react';

// Hardcoded note object
const hardcodedNote = {
  "noteId": "559528eb-9ea3-4f6c-81a9-6c5e073b7548",
  "title": "Sample tit",
  "content": "Sample desc.",
  "owner": {
    "userId": "887ddf62-43fd-4ba8-945b-02823f2cd914",
    "email": "example2@example.com",
    "hashPassword": "$2a$10$VnLLsBIQ.M0fjqkSRQRNNehljlTGxJvN7jxMhXVhgx1YyvByRJYL6",
    "roles": null,
    "ownedNotes": [
      "559528eb-9ea3-4f6c-81a9-6c5e073b7548"
    ],
    "collaboratedNotes": []
  },
  "collaborators": [
  ]
};

export default function Home() {
  const [note, setNote] = useState(hardcodedNote);
  const socketRef = useRef(); // Create a ref to store the socket
  const heartbeatIntervalRef = useRef();

  useEffect(() => {
    // Initialize WebSocket connection
    socketRef.current = new WebSocket(`wss://localhost/wsapp/document/${123}`);

    socketRef.current.onopen = () => {
      console.log('Connected to WebSocket');

      // Start sending heartbeat messages every 30 seconds
      heartbeatIntervalRef.current = setInterval(() => {
        if (socketRef.current.readyState === WebSocket.OPEN) {
          socketRef.current.send(JSON.stringify({ type: 'heartbeat' }));
        }
      }, 30000); // Adjust interval as needed

      socketRef.current.onmessage = (event) => {
        console.log('Received message:', event.data);
        try {
          // Assuming the server sends JSON formatted messages
          const data = JSON.parse(event.data);

          if (data.type === 'updateNotification') {
            console.log('Received note update:', data.payload);
            setNote(data.payload);
          }
        } catch (error) {
          console.error('Error parsing message:', error);
        }
      };

      socketRef.current.onerror = (error) => {
        console.error('WebSocket Error:', error);
      };

      socketRef.current.onclose = () => {
        console.log('Disconnected from WebSocket');
      };
    };


    return () => {
      // Clean up on component unmount
      if (socketRef.current) {
        socketRef.current.close();
      }
      clearInterval(heartbeatIntervalRef.current);
    };
  }, []);

  const sendMessage = () => {
    console.log("Sending message");
    if (socketRef.current && note) { // Check if both socket connection and note exist
      // Assuming the server expects JSON formatted messages
      const message = JSON.stringify({
        type: 'updateNote',
        payload: note // Include the entire note object in the payload
      });
      socketRef.current.send(message);
    }
  };

  return (
    <div>
      <button onClick={sendMessage}>Send note</button>
      {JSON.stringify(note)}
    </div>
  );
}
