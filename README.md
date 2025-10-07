# StudyStart REST API (Task 4 — Security)

This repository contains the REST API for the **TIES4560 Task-4** assignment.

---

## ✅ Completed Scope

* **User Registration & Credential Storage**
* **Basic Authentication**
* **JWT Authentication** via `/studystart/api/auth/login`
* **RBAC (Role-Based Access Control)** using `@RolesAllowed`, `@PermitAll`, `@DenyAll`

> **Team Members:**
>
> * Mohammad Sayeem Sadat Hossain
> * Kateryna Chukhrai
> * Marta-Sofiya Klakovych
> * Rakibul Hasan Rem
> * Mushfiqul Islam Chowdhury — [GitHub ↗](https://github.com/mushfiqulIslam)

---

## ⚙️ Running

Build with Maven and deploy to your servlet container (e.g., Tomcat):

```bash
mvn clean package
```

Then access the API via:
**[http://localhost:8080/studystart/](http://localhost:8080/studystart/)**

---

## 🔐 Authentication Methods

### Option A — Basic Auth

Use **Basic Authentication** with:

* **username:** `admin`
* **password:** `admin123`

In Postman:
**Authorization → Basic Auth →** enter credentials above.

---

### Option B — JWT Authentication

Authenticate via `/studystart/api/auth/login` to receive a **Bearer token**, then use that token in the `Authorization` header for subsequent requests.

#### **Endpoint**

`POST /studystart/api/auth/login`

#### **Request (JSON)**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

#### **Successful Response (JSON)**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIkFETUlOIiwiVVNFUiJdLCJpYXQiOjE3Mzg4MjA2NTYsImV4cCI6MTczODgyNDI1Nn0.VxJ2EkjCBpRkb7B3AIfUgWJbZ1qK4lWjPgfGrxTObQw",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "admin",
  "roles": ["ADMIN", "USER"]
}
```

#### **Usage**

Add to headers:

```
Authorization: Bearer <JWT_STRING>
```

---

### 🧩 Example JWT (Decoded)

JWTs consist of three parts: **Header**, **Payload**, and **Signature**.

Example token:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIkFETUlOIiwiVVNFUiJdLCJpYXQiOjE3Mzg4MjA2NTYsImV4cCI6MTczODgyNDI1Nn0.
VxJ2EkjCBpRkb7B3AIfUgWJbZ1qK4lWjPgfGrxTObQw
```

**Header:**

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**

```json
{
  "sub": "admin",
  "roles": ["ADMIN", "USER"],
  "iat": 1738820656,
  "exp": 1738824256
}
```

---

### 🔧 Example cURL

```bash
# 1. Login to receive JWT
curl --location 'http://localhost:8080/studystart/api/auth/login' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "admin",
    "password": "admin123"
}'

# 2. Use the token in subsequent requests
curl --location 'http://localhost:8080/studystart/users' \
--header 'Authorization: Bearer <JWT_STRING>'
```

---

## 📘 Postman Collections

Preconfigured Postman collection(s) available under:

```
/postman/StudyStart.postman_collection.json
```

---

## 🔒 Notes

* **Basic Auth** → sends Base64-encoded credentials in `Authorization: Basic ...`
* **JWT Auth** → use `Authorization: Bearer <token>`
* **Tokens expire** after `expiresIn` seconds (default: 3600).
* For coursework only — not for production use.

---

## 🧩 Main Features

| Feature                              | Description                                                          |
| ------------------------------------ | -------------------------------------------------------------------- |
| **Authentication**                   | Supports Basic Auth and JWT                                          |
| **RBAC (Role-Based Access Control)** | Enforced via annotations (`@RolesAllowed`, `@PermitAll`, `@DenyAll`) |
| **Roles**                            | `ADMIN`, `USER`, `GUEST`                                             |
| **Token Expiry**                     | Controlled via `exp` claim                                           |
| **Error Handling**                   | `401` for unauthenticated, `403` for insufficient rights             |

---

## 📂 Endpoints & Role Access

### 🔑 Auth

| Method | Path                         | Access | Description                |
| ------ | ---------------------------- | ------ | -------------------------- |
| POST   | `/studystart/api/auth/login` | All    | Authenticate and issue JWT |

### 👤 Users

| Method | Path                | Access       | Description                |
| ------ | ------------------- | ------------ | -------------------------- |
| POST   | `/users/register`   | Guest        | Register new user          |
| GET    | `/users`            | Guest        | List all users             |
| GET    | `/users/{username}` | Self / Admin | Get user details           |
| PUT    | `/users/{username}` | Self / Admin | Update user                |
| DELETE | `/users/{username}` | Admin        | Delete user                |
| GET    | `/users/deny-test`  | —            | Always 403 (@DenyAll demo) |

### 📄 Documents

| Method | Path                                   | Access               | Description     |
| ------ | -------------------------------------- | -------------------- | --------------- |
| GET    | `/profiles/{profileId}/documents`      | Guest / User / Admin | List documents  |
| GET    | `/profiles/{profileId}/documents/{id}` | User / Admin         | View document   |
| POST   | `/profiles/{profileId}/documents`      | User / Admin         | Upload document |
| PUT    | `/profiles/{profileId}/documents/{id}` | User / Admin         | Update document |
| DELETE | `/profiles/{profileId}/documents/{id}` | Admin                | Delete document |

### 📤 Document Upload

| Method | Path                                                   | Access       |
| ------ | ------------------------------------------------------ | ------------ |
| POST   | `/profiles/{profileId}/documents/new/{docType}/upload` | User / Admin |

---

## 🛡️ Error Responses

| Code               | Meaning           | Cause                              |
| ------------------ | ----------------- | ---------------------------------- |
| `401 Unauthorized` | Not authenticated | Missing, invalid, or expired token |
| `403 Forbidden`    | Access denied     | Valid token but insufficient role  |


