<div align="center">

# 🍃 Workshop MongoDB

**A RESTful API built with Spring Boot 4 and MongoDB**

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/Maven-Build%20Tool-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

</div>

---

## 📌 About the Project

**Workshop MongoDB** is a study/workshop project that demonstrates how to integrate **Spring Boot** with **MongoDB** to build a modern, document-oriented RESTful API.

The project follows a standard layered architecture — **Controller → Service → Repository** — and leverages **Spring Data MongoDB** to handle CRUD operations without boilerplate persistence code. It serves as a practical reference for anyone learning NoSQL integration with the Spring ecosystem.

**Key highlights:**
- Full REST API with standard HTTP methods
- Spring Data MongoDB repository pattern (no raw queries needed)
- Clean separation of concerns across layers
- Ready-to-use Maven Wrapper (no local Maven installation required)

---

## 🏛 Architecture

```
Client (HTTP)
     │
     ▼
┌─────────────────────┐
│   REST Controller   │  ← Receives HTTP requests, returns JSON responses
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│      Service        │  ← Business logic layer
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│    Repository       │  ← Spring Data MongoDB interface
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│      MongoDB        │  ← NoSQL Document Database
└─────────────────────┘
```

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 25 | Programming language |
| **Spring Boot** | 4.0.5 | Application framework |
| **Spring Data MongoDB** | (managed) | Data access layer |
| **Spring Web MVC** | (managed) | REST API layer |
| **MongoDB** | 6.0+ | NoSQL document database |
| **Maven** | 3.8+ (wrapper included) | Build and dependency management |

---

## ✅ Prerequisites

Before you begin, make sure you have the following installed on your machine:

| Requirement | Version | Download |
|---|---|---|
| JDK | 25+ | [openjdk.org](https://openjdk.org/) |
| MongoDB | 6.0+ | [mongodb.com](https://www.mongodb.com/try/download/community) |
| Git | any | [git-scm.com](https://git-scm.com/) |

> **Note:** Maven is **not** required — the project includes the `mvnw` / `mvnw.cmd` wrapper scripts.

---

## 📡 API Endpoints

> Base URL: `http://localhost:8080`

Below are the standard REST endpoints exposed by this application. Replace `{id}` with the MongoDB document `ObjectId`.

### Example resource: `/api/items`

| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `GET` | `/api/items` | Retrieve all items | `200 OK` |
| `GET` | `/api/items/{id}` | Retrieve a single item by ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/items` | Create a new item | `201 Created` |
| `PUT` | `/api/items/{id}` | Update an existing item | `200 OK` / `404 Not Found` |
| `DELETE` | `/api/items/{id}` | Delete an item by ID | `204 No Content` |

### Example Request — Create an item

```http
POST /api/items
Content-Type: application/json

{
  "name": "Sample Item",
  "description": "A test document stored in MongoDB"
}
```

### Example Response

```json
{
  "id": "64a1f2c3e45b9d0012ab34cd",
  "name": "Sample Item",
  "description": "A test document stored in MongoDB"
}
```

---

## 📁 Project Structure

```
project-spring-mongodb/
│
├── src/
│   ├── main/
│   │   ├── java/com/thaleskirchner/workshopmongo/
│   │   │   ├── WorkshopmongoApplication.java      # Spring Boot entry point
│   │   │   ├── controller/                        # REST Controllers (@RestController)
│   │   │   ├── service/                           # Business Logic Layer
│   │   │   ├── repository/                        # Spring Data MongoDB Repositories
│   │   │   ├── model/                             # Domain/Document classes (@Document)
│   │   │   └── config/                            # App configuration (if any)
│   │   │
│   │   └── resources/
│   │       └── application.properties             # App configuration
│   │
│   └── test/
│       └── java/com/thaleskirchner/workshopmongo/ # Unit & integration tests
│
├── .mvn/wrapper/                                  # Maven wrapper files
├── mvnw                                           # Maven wrapper (Linux/macOS)
├── mvnw.cmd                                       # Maven wrapper (Windows)
├── pom.xml                                        # Project dependencies & build config
├── .gitignore
└── README.md
```

---


## 👤 Author

**Thales Kirchner**

- GitHub: [@thaleskirchner](https://github.com/thaleskirchner)

---

<div align="center">
  Made with ☕ and 🍃 MongoDB
</div>
