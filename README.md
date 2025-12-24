# Auth Server (OAuth2-like JWT Authorization Server)

This microservice provides authentication endpoints and issues JWT access and refresh tokens.

Endpoints:
- POST `/auth/register` - register new users (expects `StandardRequest` with `RegisterRequest`).
- POST `/auth/login` - login with username/password, returns access/refresh tokens in `StandardResponse`.
- POST `/auth/refresh` - exchange refresh token for new tokens.
- POST `/auth/logout` - invalidate refresh token.
- GET `/auth/userinfo` - returns user info, requires Authorization: Bearer <accessToken>.

Run:

```
mvn spring-boot:run
```

H2 console: `http://localhost:8081/h2-console`
# Auth_Server