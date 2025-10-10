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

## Pricing Suggestion Endpoint

POST `/api/pricing/suggest`

Request JSON:
```json
{
  "title": "Feliz S 2023 còn mới",
  "description": "Xe đi 5000km, pin còn tốt",
  "category": "EV", // or BATTERY
  "product": {
    "brand": "VinFast",
    "model": "Feliz S",
    "year": 2023,
    "batteryCapacity": "3.5 kWh",
    "condition": "used_good",
    "mileage": 5000,
    "healthPercentage": 90
  },
  "location": { "province": "HCM", "district": "Quan 1" }
}
```

Response JSON (Gemini mode):
```json
{
  "suggestedPrice": 26500000,
  "reason": "Giá dựa trên năm 2023, tình trạng tốt và dung lượng pin",
  "model": "gemini-1.5-flash-latest",
  "mode": "gemini"
}
```

If Gemini API key not configured, it returns heuristic:
```json
{
  "suggestedPrice": 27000000,
  "reason": "Heuristic: brand/model/year/condition/battery",
  "model": null,
  "mode": "heuristic"
}
```

### Configuration
Set environment variable `GEMINI_API_KEY` OR set property `app.ai.gemini.apiKey`.

Supported methods:
1. PowerShell (temporary session):
  `$env:GEMINI_API_KEY = "YOUR_KEY_HERE"`
2. Windows CMD:
  `set GEMINI_API_KEY=YOUR_KEY_HERE`
3. Linux/macOS:
  `export GEMINI_API_KEY=YOUR_KEY_HERE`
4. .env file (spring-dotenv picks it up):
  - Copy `.env.example` to `.env`
  - Fill `GEMINI_API_KEY=...`
5. Docker Compose: add `- GEMINI_API_KEY=YOUR_KEY_HERE` under backend `environment:`

Verify:
 - Restart backend.
 - Check logs: absence of warning `Gemini API key not configured`.
 - Test POST /api/pricing/suggest and ensure `mode":"gemini"` in response.

Never commit a real production key. Rotate key if accidentally exposed.