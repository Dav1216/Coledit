# Coledit: Collaborative Note Taking Web Application

## Overview
A collaborative note-taking application allowing a number of users to work on notes simultaneously. 
It features automatic conflict resolution through server-side merging and real-time updates via WebSockets. 
The application emphasizes security with Spring Security and JWT tokens, uses Next.js for the frontend,
Java for the backend, JPA for database interactions, and Nginx for reverse proxying. 
Docker Compose facilitates both development and deployment environments.

## Architecture
Coledit follows a Controller-Service-Repository REST architecture pattern,
which promotes separation of concerns and enhances maintainability.
This structure enables efficient data management, business logic processing, and request handling, making Coledit scalable and robust.

## Features
- **Real-Time Collaboration**: Multiple users can edit the same note concurrently.
- **Conflict Resolution**: Server merges changes in case of conflicts on the same version.
- **Fast Refresh**: WebSockets enable quick updates on notes.
- **Security**: Utilizes Spring Security and JWT tokens for secure access.
- **Technology Stack**:
  - Frontend: Next.js
  - Backend: Java
  - Database Interaction: JPA
  - Database: PostgreSQL
  - Reverse Proxy: Nginx
  - Development & Deployment: Docker Compose

## Custom Implementation
- **String Merger**: A custom implementation for merging different versions of strings, available in `StringMerger.class`.
- To understand how this implementation works the StringMergerTest contains explanations and examples.

## Getting Started

### Development Setup
To start the application in development mode, run:
```zsh
docker compose -f compose.yml -f compose.dev.yml up --build --watch 
```

### Running Tests
To execute backend tests, use:
```zsh
docker exec -it backend mvn test
```

### Running Tests
To execute backend tests, use:
```zsh
docker exec -it backend mvn test
```

You can open two different browsers. To access the frontend of the application enter the following URL in your preferred browsers: https://localhost/

Log in with the provided email addresses and passwords. Use one for each browser tab:

- **Email**: user1@example.com, **Password**: pass1
- **Email**: user2@example.com, **Password**: pass2

You can then proceed to experiment with the application as shown in the tutorial video.


