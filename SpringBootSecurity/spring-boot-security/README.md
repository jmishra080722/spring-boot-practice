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
