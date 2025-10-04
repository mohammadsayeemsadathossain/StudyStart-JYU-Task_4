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
