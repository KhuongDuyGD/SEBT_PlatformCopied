# Second-hand EV Battery Trading Platform

This project is structured following the UI-API architectural pattern, separating the frontend and backend concerns for better maintainability and scalability.

## Project Structure

### Frontend (/frontend)
- `/src`
  - `/components` - Reusable UI components
  - `/pages` - Different pages/views
  - `/services` - API integration
  - `/assets` - Images, styles, etc.

### Backend (/backend)
- `/src`
  - `/controllers` - Business logic
  - `/models` - Data models
  - `/routes` - API endpoints
  - `/middleware` - Authentication, validation, etc.
  - `/config` - Configuration files
  - `/utils` - Helper functions

## Getting Started

### Prerequisites
- Node.js (v14 or higher)
- npm or yarn
- Docker (optional)

### Installation

1. Clone the repository
```bash
git clone https://github.com/NguyenPhuocDucMinh/Second-hand-EV-Battery-Trading-Platform.git
```

2. Frontend setup
```bash
cd frontend
npm install
npm start
```

3. Backend setup
```bash
cd backend
npm install
npm start
```

## Development

- Frontend runs on `http://localhost:3000`
- Backend API runs on `http://localhost:8000`

## Documentation
- Frontend documentation can be found in `/frontend/README.md`
- Backend documentation can be found in `/backend/README.md`