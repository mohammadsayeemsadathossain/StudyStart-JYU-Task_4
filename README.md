# StudyStart REST API (Task 4 — Security)

This repository contains the REST API for the **TIES4560 Task-4** assignment.

## Completed scope (by Sayeem & Remon)
- **User Registration & Credential Storage**
- **Basic Authentication** for protected endpoints

> Team: **Mohammad Sayeem Sadat Hossain (Sayeem)** and **Rakibul Hasan Remon**  
> The first two tasks were implemented by us.

## Running
Build with Maven and deploy to your servlet container (e.g., Tomcat) or run via your chosen setup.

```bash
mvn clean package
```

## Authentication
Use **Basic Auth** with:
- **username:** `admin`
- **password:** `admin123`

When using Postman: Authorization tab → Type **Basic Auth** → enter the credentials above.

## Postman collections
Exported Postman collection(s) are included under:
```
/postman/StudyStart.postman_collection.json
```

## Notes
- Basic Auth sends `username:password` Base64-encoded in the `Authorization` header (e.g., `Authorization: Basic YWRtaW46YWRtaW4xMjM=` for `admin:admin123`).
- For coursework only—do not use these credentials in production.

## Main Features

- **Authentication**: Basic Auth (extendable to JWT)  
- **Roles**: `ADMIN`, `USER`, `GUEST`  
  - **ADMIN** → full access  
  - **USER** → can manage own profile and own documents  
  - **GUEST** → can only read public GET endpoints  

- **RBAC enforcement** with `@RolesAllowed`, `@PermitAll`, `@DenyAll`  

- **401 vs 403 handling**:  
  - **401 Unauthorized** → not logged in (no/invalid credentials)  
  - **403 Forbidden** → logged in but not enough rights  


## Endpoints & Roles

### Users
- `POST /users/register` → Register new user (**guest allowed**)  
- `GET /users` → List all users (**guest allowed**)  
- `GET /users/{username}` → Self or admin  
- `PUT /users/{username}` → Self or admin  
- `DELETE /users/{username}` → Admin only  
- `GET /users/deny-test` → **@DenyAll demo** → always returns 403  

### Documents
- `GET /profiles/{profileId}/documents` → Guest/User/Admin  
- `GET /profiles/{profileId}/documents/{id}` → Guest/User/Admin  
- `POST /profiles/{profileId}/documents` → User/Admin (user = own docs)  
- `PUT /profiles/{profileId}/documents/{id}` → User/Admin (user = own docs)  
- `DELETE /profiles/{profileId}/documents/{id}` → Admin only  

### Document Upload
- `POST /profiles/{profileId}/documents/new/{docType}/upload`  
  → Upload PDF/JPEG/PNG (**User/Admin**)  

