## Spring Boot Security JWT – Application Overview

This project is a **JWT-based authentication and authorization** demo using **Spring Boot 3 / Spring Security 6**.  
It exposes product-related REST endpoints secured with JWT tokens and role-based authorization.

---

## Tech Stack

- **Backend**: Spring Boot
- **Security**: Spring Security with JWT (JSON Web Tokens)
- **JPA Layer**: `UserInfo`, `UserInfoRepository`
- **Auth Components**:
  - `SecurityConfig`
  - `JwtAuthFilter`
  - `JwtService`
  - `UserInfoUserDetails` & `UserInfoUserDetailsService`

---

## How the Flow Works (Start to End)

### 1. Application Startup

- `SecurityConfig` is picked up by Spring because of `@Configuration`, `@EnableWebSecurity`, and `@EnableMethodSecurity`.
- Beans created:
  - `UserDetailsService` → `UserInfoUserDetailsService`
  - `PasswordEncoder` → `BCryptPasswordEncoder`
  - `AuthenticationProvider` → `DaoAuthenticationProvider` wired with `UserInfoUserDetailsService` and `PasswordEncoder`
  - `AuthenticationManager` (from `AuthenticationConfiguration`)
  - `SecurityFilterChain` → configures HTTP security
- `JwtAuthFilter` (a `OncePerRequestFilter`) is registered **before** `UsernamePasswordAuthenticationFilter`.

**Important security chain setup (`SecurityConfig.securityFilterChain`)**:

- Disables CSRF (for stateless REST APIs).
- Configures **public endpoints**:
  - `/product/welcome`
  - `/product/new`
  - `/product/authenticate`
- Secures `/product/**` (everything else under `/product`) as **authenticated**.
- Sets **session management** to `SessionCreationPolicy.STATELESS` (no HTTP session, every request must carry a JWT).
- Registers the custom `authenticationProvider()`.
- Adds `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`.

---

### 2. User Registration (`POST /product/new`)

**Endpoint**: `ProductController.addNewUser`  
**Path**: `/product/new`  
**Access**: Public (no token required)

Flow:

1. Client sends a JSON body with user information, e.g.:

   ```json
   {
     "name": "john",
     "password": "pwd123",
     "roles": "ROLE_USER"
   }
   ```

2. `ProductController` calls `UserService.addUser(UserInfo userInfo)`.
3. `UserService`:
   - Encodes the plain password using `BCryptPasswordEncoder`.
   - Persists a `UserInfo` entity via `UserInfoRepository`.
4. User is now stored in DB and can later authenticate.

---

### 3. Authentication & Token Creation (`POST /product/authenticate`)

**Endpoint**: `ProductController.authenticateAndGetToken`  
**Path**: `/product/authenticate`  
**Access**: Public (no token required)

Request body (`AuthRequest`):

```json
{
  "username": "john",
  "password": "pwd123"
}
```

Flow (step by step):

1. Controller receives `AuthRequest` and constructs a `UsernamePasswordAuthenticationToken`:
   - Principal: `authRequest.getUsername()`
   - Credentials: `authRequest.getPassword()`
2. Calls `authenticationManager.authenticate(...)`.
3. `AuthenticationManager` delegates to the configured `AuthenticationProvider`:
   - `DaoAuthenticationProvider` uses `UserInfoUserDetailsService.loadUserByUsername(username)` to load user data from DB as a `UserDetails` implementation (`UserInfoUserDetails`).
   - Compares the raw password from request with the encoded password from DB using `BCryptPasswordEncoder`.
4. If authentication **fails**:
   - An exception is thrown (e.g. `BadCredentialsException`), and the controller will eventually throw `UsernameNotFoundException("Invalid user request!")`.
5. If authentication **succeeds**:
   - `authenticate.isAuthenticated()` is `true`.
   - Controller calls `jwtService.generateToken(authRequest.getUsername())`.

#### 3.1. How `JwtService.generateToken` Works

- Class: `JwtService`
- Secret key: `SECRET` (a Base64-encoded string).

Main methods:

- `generateToken(String userName)`:
  - Creates a claims map (currently empty).
  - Calls `createToken(userName, claims)`.
- `createToken(String userName, Map<String, Object> claims)`:
  - Builds a JWT using `io.jsonwebtoken.Jwts`:
    - `setClaims(claims)` – custom claims (you can extend this if needed).
    - `setSubject(userName)` – sets the username as the **subject**.
    - `setIssuedAt(new Date(System.currentTimeMillis()))`.
    - `setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))` – token valid for **30 minutes**.
    - `signWith(getSignKey(), SignatureAlgorithm.HS256)` – signs the token using HMAC-SHA256 and the secret key.
  - Returns the compact JWT string.
- `getSignKey()`:
  - Decodes the `SECRET` using `Decoders.BASE64`.
  - Creates a `Key` with `Keys.hmacShaKeyFor(...)`.

**Result**: The controller returns the JWT string to the client.  
The client should store it (e.g., in memory, not in localStorage for browser-based apps) and attach it to subsequent requests.

---

### 4. Sending Authenticated Requests with the JWT

For any **secured endpoint** (e.g. `/product/all`, `/product/{id}`):

- The client must send the JWT in the `Authorization` header:

```http
GET /product/all HTTP/1.1
Host: localhost:8080
Authorization: Bearer <JWT_TOKEN_HERE>
```

---

### 5. Request Filtering & Token Validation (`JwtAuthFilter`)

Class: `JwtAuthFilter` (extends `OncePerRequestFilter`)  
Registered in `SecurityConfig` with:

```java
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

Flow for **every incoming HTTP request**:

1. `doFilterInternal` is executed.
2. Reads the `Authorization` header:
   - If it exists and starts with **`"Bearer "`**, it:
     - Extracts the token `authHeader.substring(7)`.
     - Calls `jwtService.extractUsername(token)` to get the username from token’s subject.
3. If a username is found **and** the `SecurityContext` has no current authentication:
   - Loads user details:

     ```java
     UserDetails userDetails = userInfoUserDetailsService.loadUserByUsername(username);
     ```

   - Validates token with:

     ```java
     jwtService.validateToken(token, userDetails)
     ```

   - `validateToken`:
     - Extracts username from token and compares it with `userDetails.getUsername()`.
     - Checks `!isTokenExpired(token)` (expiration).
4. If token is valid:
   - Creates a `UsernamePasswordAuthenticationToken` with:
     - Principal: `userDetails`
     - Credentials: `null`
     - Authorities: `userDetails.getAuthorities()` (roles from DB).
   - Sets details: `authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));`
   - Stores authentication in `SecurityContextHolder.getContext().setAuthentication(authToken);`.
5. Calls `filterChain.doFilter(request, response)` to continue the chain.

**Effect**:  
After `JwtAuthFilter` succeeds, downstream security checks see the user as **authenticated**, with roles populated from the DB, **without** using an HTTP session.

---

### 6. Authorization on Endpoints

In `ProductController`:

- `/product/all`:

  ```java
  @GetMapping("/all")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public List<Product> getAllTheProducts() { ... }
  ```

  - Requires `ROLE_ADMIN`.

- `/product/{id}`:

  ```java
  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public Product getProductById(@PathVariable int id) { ... }
  ```

  - Requires `ROLE_USER`.

Since `@EnableMethodSecurity` is enabled, Spring checks the authorities from the `Authentication` object set by `JwtAuthFilter`. If the token is missing, invalid, expired, or has insufficient roles, access is denied.

---

## End-to-End Flow Summary

1. **Register user**: `POST /product/new` with user details → user stored in DB with encoded password & roles.
2. **Login / Get token**: `POST /product/authenticate` with username/password.
   - Spring Security authenticates via `AuthenticationManager` and `DaoAuthenticationProvider`.
   - On success, `JwtService` generates a signed JWT (30 minutes expiration) and returns it.
3. **Call secured endpoints**: Client sends `Authorization: Bearer <token>` header.
4. **Filter validates token**:
   - `JwtAuthFilter` extracts and validates token using `JwtService`.
   - If valid, builds `Authentication` with user details and roles, stores it in `SecurityContextHolder`.
5. **Controller methods**:
   - Use `@PreAuthorize` to enforce role-based access:
     - `/product/all` → `ROLE_ADMIN`
     - `/product/{id}` → `ROLE_USER`
6. **Stateless**:
   - No HTTP session is used; every request must include a valid JWT.

---

## How to Run the Application

### Prerequisites

- Java 17+ (or the version required by your Spring Boot setup)
- Maven or Gradle (depending on the build tool configured in the project)
- A database configured and accessible for the `UserInfo` entity (H2/MySQL/etc., as defined in `application.properties` or `application.yml`)

### Steps

1. **Clone the repository** (or open the project in your IDE).
2. Ensure DB configuration in `application.properties` is correct.
3. Build and run:
   - Using Maven:

     ```bash
     mvn spring-boot:run
     ```

   - Or run `SpringBootSecurityApplication` from your IDE.
4. Application will start on the configured port (commonly `8080`).

---

## Testing the Flow (Example with cURL)

### 1. Register a User

```bash
curl -X POST http://localhost:8080/product/new \
  -H "Content-Type: application/json" \
  -d '{"name":"john","password":"pwd123","roles":"ROLE_USER"}'
```

### 2. Authenticate and Get Token

```bash
curl -X POST http://localhost:8080/product/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pwd123"}'
```

Response: a JWT token string (save it).

### 3. Call a Secured Endpoint

```bash
curl -X GET http://localhost:8080/product/1 \
  -H "Authorization: Bearer <PASTE_JWT_HERE>"
```

If the token is valid and the user has `ROLE_USER`, you will get the product response; otherwise, you get an error (e.g., 403 Forbidden or 401 Unauthorized).

---

## Extending the Application

- **Add more claims** to the JWT in `JwtService.createToken` (e.g. roles, user ID).
- **Change token expiration** by adjusting the duration in `setExpiration`.
- **Add more secured endpoints** and protect them with `@PreAuthorize` expressions.
- **Rotate secrets** and move `SECRET` to a secure configuration (environment variable, Vault, etc.).

This README reflects the current implementation of your application and the complete end-to-end security flow using JWT.

# Spring Boot Security Application

A comprehensive Spring Boot application demonstrating security implementation using Spring Security with database-backed user authentication and role-based access control (RBAC).

## 📋 Table of Contents

- [Project Purpose](#project-purpose)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Security Flow](#security-flow)
- [Authentication & Authorization](#authentication--authorization)
- [Database Schema](#database-schema)
- [Usage Examples](#usage-examples)
- [Troubleshooting](#troubleshooting)

## 🎯 Project Purpose

This application demonstrates how to implement secure REST APIs using Spring Security with:
- Database-backed user authentication
- Role-based access control (RBAC)
- Password encryption using BCrypt
- Method-level security annotations
- HTTP Basic Authentication

The application manages products and users, with different access levels based on user roles (ADMIN and USER).

## ✨ Features

- **User Registration**: Public endpoint to register new users with encrypted passwords
- **Role-Based Access Control**: Different endpoints accessible based on user roles
- **Password Encryption**: BCrypt password encoding for secure password storage
- **Database Authentication**: User credentials stored in MySQL database
- **Method-Level Security**: `@PreAuthorize` annotations for fine-grained access control
- **HTTP Basic Authentication**: Simple and effective authentication mechanism
- **Product Management**: CRUD operations for products with role-based restrictions

## 🛠 Technologies Used

- **Java 17**
- **Spring Boot 4.0.1**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **MySQL** - Relational database
- **Lombok** - Reduces boilerplate code
- **Maven** - Dependency management

## 📦 Prerequisites

Before running this application, ensure you have:

- **JDK 17** or higher installed
- **Maven 3.6+** installed
- **MySQL 8.0+** installed and running
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code) with Spring Boot support

## 📁 Project Structure

```
spring-boot-security/
├── src/
│   ├── main/
│   │   ├── java/com/jay/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # Security configuration
│   │   │   │   ├── UserInfoUserDetailsService.java  # Custom UserDetailsService
│   │   │   │   └── UserInfoUserDetails.java     # Custom UserDetails implementation
│   │   │   ├── controller/
│   │   │   │   └── ProductController.java       # REST API endpoints
│   │   │   ├── service/
│   │   │   │   ├── ProductService.java          # Product business logic
│   │   │   │   └── UserService.java            # User business logic
│   │   │   ├── repository/
│   │   │   │   └── UserInfoRepository.java      # JPA repository
│   │   │   ├── entity/
│   │   │   │   └── UserInfo.java               # User entity
│   │   │   ├── dto/
│   │   │   │   └── Product.java                # Product DTO
│   │   │   └── SpringBootSecurityApplication.java  # Main application class
│   │   └── resources/
│   │       └── application.properties          # Application configuration
│   └── test/
└── pom.xml                                      # Maven dependencies
```

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd spring-boot-security
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE springboot;
```

### 3. Configure Database Connection

Update `src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/springboot
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

Or run the `SpringBootSecurityApplication` class from your IDE.

The application will start on `http://localhost:8080`

## ⚙️ Configuration

### Application Properties

The application uses the following key configurations:

```properties
# Database Configuration
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/springboot
spring.datasource.username=root
spring.datasource.password=root

# JPA Configuration
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

### Security Configuration

- **CSRF**: Disabled (for API testing)
- **Password Encoder**: BCryptPasswordEncoder
- **Authentication Provider**: DaoAuthenticationProvider
- **Security Filter Chain**: Custom configuration with public and protected endpoints

## 🔌 API Endpoints

### 1. Welcome Endpoint (Public)

**GET** `/product/welcome`

- **Description**: Public welcome message
- **Authentication**: Not required
- **Response**: 
  ```json
  "Welcome! This endpoint is not secure."
  ```

**Example Request:**
```bash
curl http://localhost:8080/product/welcome
```

---

### 2. Register New User (Public)

**POST** `/product/new`

- **Description**: Register a new user in the system
- **Authentication**: Not required
- **Request Body**:
  ```json
  {
    "name": "Ram",
    "email": "ram@example.com",
    "password": "Pwd1",
    "roles": "ROLE_ADMIN"
  }
  ```
- **Response**: `201 Created`
  ```json
  "User added to system"
  ```

**Example Request:**
```bash
curl -X POST http://localhost:8080/product/new \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ram",
    "email": "ram@example.com",
    "password": "Pwd1",
    "roles": "ROLE_ADMIN"
  }'
```

**Note**: 
- The `id` field is auto-generated and should not be included in the request
- Password will be automatically encrypted using BCrypt
- Roles should be in the format: `ROLE_ADMIN` or `ROLE_USER` (comma-separated for multiple roles)

---

### 3. Get All Products (Admin Only)

**GET** `/product/all`

- **Description**: Retrieve all products from the system
- **Authentication**: Required (HTTP Basic Auth)
- **Authorization**: Requires `ROLE_ADMIN` authority
- **Response**: `200 OK`
  ```json
  [
    {
      "productId": 1,
      "name": "product 1",
      "qty": 5,
      "price": 1234.0
    },
    {
      "productId": 2,
      "name": "product 2",
      "qty": 8,
      "price": 2345.0
    }
  ]
  ```

**Example Request:**
```bash
curl -u Ram:Pwd1 http://localhost:8080/product/all
```

---

### 4. Get Product by ID (User Only)

**GET** `/product/{id}`

- **Description**: Retrieve a specific product by its ID
- **Authentication**: Required (HTTP Basic Auth)
- **Authorization**: Requires `ROLE_USER` authority
- **Path Parameters**:
  - `id` (integer): Product ID
- **Response**: `200 OK`
  ```json
  {
    "productId": 1,
    "name": "product 1",
    "qty": 5,
    "price": 1234.0
  }
  ```

**Example Request:**
```bash
curl -u Jay:Pwd2 http://localhost:8080/product/1
```

**Error Response** (if product not found):
```json
{
  "error": "Product with id 99 not found"
}
```

## 🔐 Security Flow

### Authentication Flow

1. **User Registration**:
   - Client sends POST request to `/product/new` with user details
   - `UserService` encrypts the password using BCrypt
   - User is saved to database with encrypted password

2. **User Login**:
   - Client sends request with HTTP Basic Authentication header
   - Spring Security extracts credentials from the header
   - `UserInfoUserDetailsService` loads user from database by username
   - `UserInfoUserDetails` wraps the user entity and provides authorities
   - `DaoAuthenticationProvider` validates password using BCrypt
   - If valid, user is authenticated and granted authorities

3. **Authorization**:
   - After authentication, Spring Security checks `@PreAuthorize` annotations
   - User's authorities are matched against required authorities
   - Request is allowed or denied based on authorization rules

### Security Filter Chain

```
Request → SecurityFilterChain → Authentication → Authorization → Controller
```

1. **Public Endpoints** (`/product/welcome`, `/product/new`): Bypass authentication
2. **Protected Endpoints** (`/product/**`): Require authentication
3. **Method-Level Security**: Additional authorization checks using `@PreAuthorize`

## 🔑 Authentication & Authorization

### User Roles

The application supports two roles:

- **ROLE_ADMIN**: Can access `/product/all` endpoint
- **ROLE_USER**: Can access `/product/{id}` endpoint

### HTTP Basic Authentication

The application uses HTTP Basic Authentication. Include credentials in the request header:

```
Authorization: Basic <base64(username:password)>
```

Or use curl with `-u` flag:
```bash
curl -u username:password http://localhost:8080/product/all
```

### Password Encryption

All passwords are encrypted using BCrypt before storing in the database. The `PasswordEncoder` bean uses BCryptPasswordEncoder with default strength (10).

## 🗄 Database Schema

### UserInfo Table

The `UserInfo` entity maps to a database table with the following structure:

| Column    | Type         | Constraints           | Description                    |
|-----------|--------------|-----------------------|--------------------------------|
| id        | INTEGER      | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier        |
| name      | VARCHAR      | NOT NULL              | Username (used for login)      |
| email     | VARCHAR      |                       | User email address             |
| password  | VARCHAR      | NOT NULL              | BCrypt encrypted password      |
| roles     | VARCHAR      |                       | Comma-separated roles          |

**Example Data:**
```sql
INSERT INTO user_info (name, email, password, roles) 
VALUES ('Ram', 'ram@example.com', '$2a$10$...', 'ROLE_ADMIN');
```

### Product Data

Products are stored in-memory (not persisted to database) and initialized on application startup with 100 products.

## 📝 Usage Examples

### 1. Register a New Admin User

```bash
curl -X POST http://localhost:8080/product/new \
  -H "Content-Type: application/json" \
  -d '{
    "name": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "roles": "ROLE_ADMIN"
  }'
```

### 2. Register a New Regular User

```bash
curl -X POST http://localhost:8080/product/new \
  -H "Content-Type: application/json" \
  -d '{
    "name": "user",
    "email": "user@example.com",
    "password": "user123",
    "roles": "ROLE_USER"
  }'
```

### 3. Access Admin Endpoint

```bash
curl -u admin:admin123 http://localhost:8080/product/all
```

### 4. Access User Endpoint

```bash
curl -u user:user123 http://localhost:8080/product/5
```

### 5. Using Postman

1. **Public Endpoint** (No Auth):
   - Method: GET
   - URL: `http://localhost:8080/product/welcome`
   - No authentication required

2. **Register User** (No Auth):
   - Method: POST
   - URL: `http://localhost:8080/product/new`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "name": "testuser",
       "email": "test@example.com",
       "password": "test123",
       "roles": "ROLE_USER"
     }
     ```

3. **Protected Endpoint** (Basic Auth):
   - Method: GET
   - URL: `http://localhost:8080/product/all`
   - Authorization: Basic Auth
     - Username: `testuser`
     - Password: `test123`

## 🐛 Troubleshooting

### Common Issues

#### 1. JSON Parse Error: Cannot map `null` into type `int`

**Problem**: Sending a request without the `id` field causes a JSON parsing error.

**Solution**: The `id` field is auto-generated. Do not include it in the request body. The entity uses `Integer` type to handle null values properly.

#### 2. 401 Unauthorized Error

**Possible Causes**:
- Missing or incorrect credentials in HTTP Basic Auth header
- User not found in database
- Incorrect password
- Endpoint requires authentication but none provided

**Solution**:
- Verify user exists in database
- Check username and password are correct
- Ensure HTTP Basic Auth header is properly formatted
- For public endpoints (`/product/welcome`, `/product/new`), no authentication is needed

#### 3. 403 Forbidden Error

**Problem**: User is authenticated but doesn't have required authority.

**Solution**:
- Verify user has the correct role (ROLE_ADMIN for `/product/all`, ROLE_USER for `/product/{id}`)
- Check the `roles` field in the database matches the required authority

#### 4. Database Connection Error

**Problem**: Cannot connect to MySQL database.

**Solution**:
- Verify MySQL is running: `mysql -u root -p`
- Check database exists: `SHOW DATABASES;`
- Verify connection properties in `application.properties`
- Ensure MySQL connector dependency is in `pom.xml`

#### 5. Port Already in Use

**Problem**: Port 8080 is already in use.

**Solution**: Change the port in `application.properties`:
```properties
server.port=8081
```

### Debugging Tips

1. **Enable SQL Logging**: Already enabled via `spring.jpa.show-sql=true`
2. **Check Security Logs**: Look for authentication/authorization failures in console
3. **Verify User in Database**: Query the `user_info` table to confirm user exists
4. **Test with Public Endpoint**: First test `/product/welcome` to ensure application is running

## 📚 Additional Notes

- **CSRF Protection**: Disabled for API testing. Enable in production.
- **Password Strength**: Consider implementing password validation rules
- **Role Management**: Currently supports single or comma-separated roles
- **Product Storage**: Products are in-memory. Consider persisting to database for production use
- **Error Handling**: Add global exception handlers for better error responses

## 🔄 Future Enhancements

- JWT token-based authentication
- Refresh token mechanism
- User profile management endpoints
- Password reset functionality
- Role management API
- Audit logging
- Rate limiting
- API documentation with Swagger/OpenAPI

## 📄 License

This project is for educational purposes.

## 👤 Author

Jayhind Mishra Github: jmishra080722

---

**Happy Coding! 🚀**
