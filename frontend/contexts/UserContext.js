import { createContext } from 'react';

const UserContext = createContext({
  userEmail: null,
  userId: null,
});

export default UserContext;
