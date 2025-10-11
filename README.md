# StudyStart REST API (Task 4 — Security + HATEOAS)

This repository contains the REST API for the **TIES4560 Task-4** assignment.

---

## ✅ Completed Scope

* **User Registration & Credential Storage**
* **Basic Authentication**
* **JWT Authentication** via `/studystart/api/auth/login`
* **RBAC (Role-Based Access Control)** using `@RolesAllowed`, `@PermitAll`, `@DenyAll`
* **HATEOAS links on all User and Document responses**

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

Base URL:

```
http://localhost:8080/studystart/
```

API root:

```
http://localhost:8080/studystart/api/
```

---

## 🔐 Authentication Methods

### Option A — Basic Auth

Use **Basic Authentication** with:

* **username:** `admin`
* **password:** `admin123`

In Postman: **Authorization → Basic Auth →** enter credentials above.

---

### Option B — JWT Authentication

Authenticate via `/studystart/api/auth/login` to receive a **Bearer token**, then use that token in the `Authorization` header for subsequent requests.

**Endpoint**

`POST /studystart/api/auth/login`

**Request (JSON)**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Successful Response (JSON)**

```json
{
  "token": "<JWT>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "admin",
  "roles": ["ADMIN", "USER"]
}
```

**Usage**

```
Authorization: Bearer <JWT>
```

---

## 🔧 Example cURL

```bash
# 1) Login to receive JWT
curl --location 'http://localhost:8080/studystart/api/auth/login' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "username": "admin",
    "password": "admin123"
  }'

# 2) Use the token in subsequent requests (example: list users)
curl --location 'http://localhost:8080/studystart/api/users' \
  --header 'Authorization: Bearer <JWT>'
```

---

## 🧩 JWT Reference (Decoded)

JWTs consist of **Header**, **Payload**, and **Signature**.

**Header**

```json
{ "alg": "HS256", "typ": "JWT" }
```

**Payload**

```json
{
  "sub": "admin",
  "roles": ["ADMIN", "USER"],
  "iat": 1738820656,
  "exp": 1738824256
}
```

---

## 📂 Endpoints & Role Access

> **Note:** Role protection is enforced via annotations. Depending on your instructor’s rubric, the “Guest” access in tables may still require Basic/JWT if your servlet container protects the resource path.

### 🔑 Auth

| Method | Path                         | Access | Description                |
| ------ | ---------------------------- | ------ | -------------------------- |
| POST   | `/studystart/api/auth/login` | All    | Authenticate and issue JWT |

### 👤 Users

| Method | Path                               | Access       | Description                       |
| ------ | ---------------------------------- | ------------ | --------------------------------- |
| POST   | `/studystart/api/users/register`   | All (Guest)  | Register new user                 |
| GET    | `/studystart/api/users`            | User / Admin | List all users (HATEOAS per item) |
| GET    | `/studystart/api/users/{username}` | Self / Admin | Get user details (HATEOAS)        |
| PUT    | `/studystart/api/users/{username}` | Self / Admin | Update user (HATEOAS)             |
| DELETE | `/studystart/api/users/{username}` | Admin        | Delete user                       |
| GET    | `/studystart/api/users/deny-test`  | — (DenyAll)  | Always 403 (@DenyAll demo)        |

### 📄 Documents (Profile-scoped)

| Method | Path                                                 | Access       | Description                      |
| ------ | ---------------------------------------------------- | ------------ | -------------------------------- |
| GET    | `/studystart/api/profiles/{username}/documents`      | User / Admin | List documents (HATEOAS)         |
| GET    | `/studystart/api/profiles/{username}/documents/{id}` | User / Admin | View document (HATEOAS)          |
| POST   | `/studystart/api/profiles/{username}/documents`      | User / Admin | Create/Upload metadata (HATEOAS) |
| PUT    | `/studystart/api/profiles/{username}/documents/{id}` | User / Admin | Update document (HATEOAS)        |
| DELETE | `/studystart/api/profiles/{username}/documents/{id}` | Admin        | Delete document                  |

### 📤 Binary Upload (if applicable in your project)

| Method | Path                                                                 | Access       | Description         |
| ------ | -------------------------------------------------------------------- | ------------ | ------------------- |
| POST   | `/studystart/api/profiles/{username}/documents/new/{docType}/upload` | User / Admin | Upload file content |

---

## 🔗 HATEOAS — Users

Every **User** response contains a `links` array.

### Example — List Users (`GET /studystart/api/users`)

```json
[
  {
    "username": "alice",
    "email": "alice@example.com",
    "firstName": "Alice",
    "lastName": "A.",
    "roles": ["USER"],
    "createdAt": 1760185743514,
    "links": [
      {
        "href": "http://localhost:8080/studystart/api/users/alice",
        "rel": "self"
      },
      {
        "href": "http://localhost:8080/studystart/api/profiles/alice/documents",
        "rel": "documents"
      }
    ]
  },
  {
    "username": "admin",
    "email": "admin@studystart.jyu.fi",
    "firstName": "Admin",
    "lastName": "User",
    "roles": ["ADMIN","USER"],
    "createdAt": 1760185698563,
    "links": [
      {
        "href": "http://localhost:8080/studystart/api/users/admin",
        "rel": "self"
      },
      {
        "href": "http://localhost:8080/studystart/api/profiles/admin/documents",
        "rel": "documents"
      }
    ]
  }
]
```

### Example — Single User (`GET /studystart/api/users/{username}`)

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "A.",
  "roles": ["USER"],
  "createdAt": 1760185743514,
  "links": [
    {
      "href": "http://localhost:8080/studystart/api/users/alice",
      "rel": "self"
    },
    {
      "href": "http://localhost:8080/studystart/api/profiles/alice/documents",
      "rel": "documents"
    }
  ]
}
```

---

## 🔗 HATEOAS — Documents

All **Document** responses (collection and item) contain HATEOAS links.

### Example — List Documents (`GET /studystart/api/profiles/{username}/documents`)

**Response**

```json
{
  "items": [
    {
      "id": 1,
      "username": "alice",
      "documentType": "PASSPORT",
      "fileName": "passport_alice.pdf",
      "status": "VERIFIED",
      "uploadDate": null,
      "expiryDate": null,
      "contentType": null,
      "sizeBytes": 0,
      "storagePath": null,
      "links": [
        {
          "href": "http://localhost:8080/studystart/api/profiles/alice/documents/1",
          "rel": "self"
        },
        {
          "href": "http://localhost:8080/studystart/api/profiles/alice",
          "rel": "profile"
        }
      ]
    }
  ],
  "links": [
    {
      "href": "http://localhost:8080/studystart/api/profiles/alice/documents",
      "rel": "self"
    },
    {
      "href": "http://localhost:8080/studystart/api/profiles/alice",
      "rel": "profile"
    }
  ]
}
```

### Example — Single Document (`GET /studystart/api/profiles/{username}/documents/{id}`)

```json
{
  "id": 1,
  "username": "alice",
  "documentType": "PASSPORT",
  "fileName": "passport_alice.pdf",
  "status": "VERIFIED",
  "uploadDate": null,
  "expiryDate": null,
  "contentType": null,
  "sizeBytes": 0,
  "storagePath": null,
  "links": [
    {
      "href": "http://localhost:8080/studystart/api/profiles/alice/documents/1",
      "rel": "self"
    },
    {
      "href": "http://localhost:8080/studystart/api/profiles/alice",
      "rel": "profile"
    }
  ]
}
```

### Example — Create Document (`POST /studystart/api/profiles/{username}/documents`)

**Request**

```json
{
  "documentType": "PASSPORT",
  "fileName": "passport_alice.pdf",
  "status": "UPLOADED"
}
```

**201 Created — Response (with Location header + HATEOAS)**

```json
{
  "id": 42,
  "username": "alice",
  "documentType": "PASSPORT",
  "fileName": "passport_alice.pdf",
  "status": "UPLOADED",
  "uploadDate": 1760189000000,
  "links": [
    {
      "href": "http://localhost:8080/studystart/api/profiles/alice/documents/42",
      "rel": "self"
    },
    {
      "href": "http://localhost:8080/studystart/api/profiles/alice",
      "rel": "profile"
    }
  ]
}
```

---

## 📘 Postman Collections

Preconfigured Postman collection(s) available under:

```
/postman/StudyStart.postman_collection.json
```

Import it and adjust environment variables if needed.

---

## 🛡️ Error Responses

| Code               | Meaning           | Cause                              |
| ------------------ | ----------------- | ---------------------------------- |
| `401 Unauthorized` | Not authenticated | Missing, invalid, or expired token |
| `403 Forbidden`    | Access denied     | Valid token but insufficient role  |
| `404 Not Found`    | Resource missing  | Wrong ID / username / path         |
| `400 Bad Request`  | Invalid input     | Missing/invalid fields, etc.       |

---

## ℹ️ Notes & Conventions

* **HATEOAS**

  * `rel: "self"` → resource’s own URL
  * `rel: "documents"` → documents collection for the user
  * `rel: "profile"` → the user’s profile root (e.g., `/profiles/{username}`)
* **Absolute URLs** are generated based on `UriInfo.getBaseUri()` to ensure the correct context path:
  `http://localhost:8080/studystart/api/...`
* **DELETE** usually returns `204 No Content`. If you need links in delete responses (per assignment), return `200 OK` with a body that includes `links`.

