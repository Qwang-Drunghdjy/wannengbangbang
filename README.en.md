# WanNengBangBang - Lost and Found & Smart Matching Platform

## 📋 Project Introduction

WanNengBangBang is a Lost and Found and Smart Matching Platform based on **Spring Boot + Vue 3**. It supports users publishing lost and found item information and automatically generates item descriptions using AI technology, realizing smart matching and recommendation functions.

### Core Features

- **User Authentication**: Registration, Login, JWT Token Authentication
- **Lost and Found**: Publish/Browse Lost Items and Found Items information
- **AI Description Generation**: Based on Zhipu AI (GLM), automatically identify item images and generate titles and descriptions
- **Smart Matching**: Similarity matching based on multiple dimensions such as item title, description, location, etc.
- **Message Notification**: Real-time push of matching results
- **Personal Center**: Manage personal posting records

---

## 🛠 Tech Stack

### Backend

| Technology | Usage |
|------|------|
| **Spring Boot 3.x** | Application Framework |
| **Spring Data JPA** | Data Persistence |
| **MySQL** | Main Database |
| **JWT** | User Authentication |
| **GLM (Zhipu AI)** | Image Description Generation |

### Frontend

| Technology | Usage |
|------|------|
| **Vue 3** | UI Framework |
| **TypeScript** | Type Safety |
| **Vite** | Build Tool |
| **Pinia** | State Management |
| **Vue Router** | Routing Management |
| **Axios** | HTTP Client |

---

## 📁 Project Structure

```
├── backend/                 # Backend Spring Boot Project
│   ├── src/main/java/com/uang/backend/
│   │   ├── config/         # Configuration Classes (JWT, Interceptors, etc.)
│   │   ├── controller/     # REST API Controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # Entity Classes
│   │   ├── repository/     # Data Repository
│   │   ├── service/        # Business Logic Layer
│   │   ├── client/         # AI Client
│   │   └── exception/      # Custom Exceptions
│   └── src/main/resources/
│       └── application.yml # Configuration File
│
├── frontend/               # Frontend Vue 3 Project
│   ├── src/
│   │   ├── api/           # API Interface Definitions
│   │   ├── components/    # Common Components
│   │   ├── views/         # Page Views
│   │   ├── stores/        # Pinia State Management
│   │   ├── router/        # Routing Configuration
│   │   └── utils/         # Utility Functions
│   └── nginx.conf         # Nginx Configuration
│
├── docs/                   # Documentation Directory
│   ├── development.md     # Development Manual
│   └── data-model.md      # Data Model Explanation
│
└── plans/                  # Development Plan Documents
```

---

## 🚀 Quick Start

### Environment Requirements

- **Backend**: JDK 17+, Maven 3.9+, MySQL 8.0+
- **Frontend**: Node.js 18+, npm/yarn/pnpm

### Backend Configuration

1. **Create Database**:
   ```sql
   CREATE DATABASE wannengbangbang DEFAULT CHARACTER SET utf8mb4;
   ```

2. **Configure application.yml**:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/wannengbangbang
       username: your_username
       password: your_password
   
   jwt:
     secret: your-jwt-secret-key
     expire-days: 7
   
   glm:
     api-key: your-glm-api-key
     rate-limit-per-minute: 5
   ```

3. **Start Backend**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

   The backend will start at `http://localhost:8080`.

### Frontend Configuration

1. **Install Dependencies**:
   ```bash
   cd frontend
   npm install
   ```

2. **Configure Environment Variables** (Optional):
   ```bash
   # .env.development
   VITE_API_BASE_URL=http://localhost:8080/api/v1
   ```

3. **Start Development Server**:
   ```bash
   npm run dev
   ```

   The frontend will start at `http://localhost:5173`.

---

## 📡 API Documentation

### Authentication Endpoints

| Method | Path | Description |
|------|------|------|
| POST | `/api/v1/auth/register` | User Registration |
| POST | `/api/v1/auth/login` | User Login |

### Item Endpoints

| Method | Path | Description |
|------|------|------|
| GET | `/api/v1/lost-items` | Get Lost Items List |
| POST | `/api/v1/lost-items` | Publish Lost Item (Authentication Required) |
| GET | `/api/v1/lost-items/{id}` | Get Item Details |
| GET | `/api/v1/find-items` | Get Found Items List |
| POST | `/api/v1/find-items` | Publish Found Item (Authentication Required) |
| GET | `/api/v1/find-items/{id}` | Get Item Details |

### AI Endpoints

| Method | Path | Description |
|------|------|------|
| POST | `/api/v1/ai/describe` | Image Description Generation (Authentication Required) |

### Matching Endpoints

| Method | Path | Description |
|------|------|------|
| GET | `/api/v1/find-items/{id}/matches` | Find Lost Items Matching a Found Item |
| GET | `/api/v1/lost-items/{id}/matches` | Find Found Items Matching a Lost Item |

---

## 🔐 JWT Authentication

All endpoints requiring authentication must carry the Token in the request header:

```
Authorization: Bearer <your-jwt-token>
```

The Token will be returned after successful login, with a default validity period of 7 days.

---

## 🐳 Docker Deployment

### Build Backend Image

```bash
cd backend
docker build -t wannengbangbang-backend .
```

### Build Frontend Image

```bash
cd frontend
docker build -t wannengbangbang-frontend .
```

### Using Docker Compose

```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/wannengbangbang
    depends_on:
      - mysql
  
  frontend:
    build: ./frontend
    ports:
      - "80:80"
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=wannengbangbang
```

---

## 📝 Development Guide

### Code Standards

- Backend follows Java naming conventions, uses Lombok to simplify code
- Frontend uses TypeScript, components adopt Composition API
- Please run tests before committing code: `./mvnw test` (Backend)

### Frontend/Backend Terminology Mapping

| Frontend | Backend | Description |
|------|------|------|
| `lostItems` | `LostItem` | Lost Item |
| `findItems` | `FindItem` | Found Item |
| `imageUrl` | `imageUrl` | Image URL |
| `contact` | `contact` | Contact Information |

See [docs/frontend-development.md](docs/frontend-development.md) for details.

---

## 🧪 Testing

### Backend Unit Tests

```bash
cd backend
./mvnw test
```

Test Coverage:
- Controller Layer Tests (MockMvc)
- Service Layer Tests
- Matching Algorithm Tests

---

## 📄 License

This project is for learning and research purposes only.

---

## 📞 Contact Us

If you have any questions or suggestions, feel free to submit an Issue or Pull Request.