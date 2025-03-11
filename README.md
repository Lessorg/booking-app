# Booking API 📘✨

Welcome to the **Booking API**— an online accommodation booking service. This project provides a straightforward digital system for managing accommodation bookings efficiently.

## About the Project 💻🏡
This API enables users to book accommodations, manage reservations, process payments, and receive notifications seamlessly. Built using **Java** and **Spring Boot**, it ensures smooth operations for both renters and administrators.

### Core Features ✅
- **User Authentication** 🔐 (JWT-based login & registration)
- **Accommodation Management** 🏠 (CRUD operations for listings)
- **Booking System** 📅 (Real-time availability checks, automated status updates)
- **Payment Processing** 💳 (Stripe integration for secure transactions)
- **Telegram Notifications** 📢 (Automated updates for bookings & payments)
- **Scheduled Task** ⏳ (Daily check for expired bookings)

---

## Domain Models 🏷️  

### 1. **User** 👤  
- **ID**: Unique identifier  
- **Email**: User's email (unique)  
- **First Name & Last Name**: User's full name  
- **Password**: Hashed password  
- **Role**: Defines access level _(Admin / Customer)_  

### 2. **Accommodation** 🏠  
- **ID**: Unique identifier  
- **Type**: Type of accommodation _(e.g., Apartment, House, Hotel)_  
- **Location**: Address or general location  
- **Size**: Description of the space (e.g., number of rooms)  
- **Amenities**: List of included amenities _(e.g., Wi-Fi, Pool)_  
- **Daily Rate**: Price per night (decimal)  
- **Availability**: Number of available units  

### 3. **Booking** 📅  
- **ID**: Unique identifier  
- **Check-in Date**: Start date of the booking  
- **Check-out Date**: End date of the booking  
- **Accommodation ID**: Reference to the booked accommodation  
- **User ID**: Reference to the user making the booking  
- **Status**: Current state of the booking _(Pending, Confirmed, Canceled, Expired)_  

### 4. **Payment** 💳  
- **ID**: Unique identifier  
- **Booking ID**: Reference to the associated booking  
- **Status**: Payment state _(Pending, Paid)_  
- **Amount**: Total payment amount (decimal)  
- **Stripe Session ID**: Unique identifier for the Stripe session  
- **Payment URL**: Link to complete the payment  

### **UML diagram**
![Payment](https://github.com/user-attachments/assets/8937d9e5-3523-4b9b-926a-540f97cf4a18)

---

## API Endpoints 📝
### Authentication Controller
- **POST** `/auth/register` - Register a new user  
- **POST** `/auth/login` - Login a user  

### Accommodation Controller
- **GET** `/accommodations` - Get all accommodations  
- **GET** `/accommodations/{id}` - Get accommodation by ID  
- **POST** `/accommodations` - Create a new accommodation (Admin only)  
- **PUT** `/accommodations/{id}` - Update accommodation details (Admin only)  
- **PATCH** `/accommodations/{id}` - Partially update accommodation details (Admin only)  
- **DELETE** `/accommodations/{id}` - Delete an accommodation (Admin only)  

### Booking Controller
- **POST** `/bookings` - Create a new booking  
- **GET** `/bookings` - Get bookings by user and status (Admin only)  
- **GET** `/bookings/my` - Get bookings of the current authenticated user  
- **GET** `/bookings/{id}` - Get booking by ID  
- **PUT** `/bookings/{id}` - Update a booking  
- **PATCH** `/bookings/{id}` - Partially update a booking  
- **DELETE** `/bookings/{id}` - Cancel a booking  

### Payment Controller
- **GET** `/payments` - Retrieve payments (Customers see own, Admins see all)  
- **POST** `/payments` - Initiate a payment for a booking (Customer only)  
- **GET** `/payments/success` - Handle successful payment  
- **GET** `/payments/cancel` - Handle payment cancellation  
- **PUT** `/payments/renew/{paymentId}` - Renew a payment session 

## User Controller
- **GET** `/users/me` - Get current user profile  
- **PATCH** `/users/me` - Update current user profile  
- **PUT** `/users/me` - Update current user profile  
- **PUT** `/users/{id}/role` - Update user roles (Admin only)  

### Health Check Controller
- **GET** `/health` - Check API health status  

---

## Technology Stack 🛠️
### Core Frameworks
- 🌱 **Spring Boot** - Backend framework
- 🔒 **Spring Security** - Authentication & authorization
- 🗂 **Spring Data JPA** - ORM for database interactions

### Database Management
- 🛢 **PostgreSQL** - Reliable data storage
- 🔄 **Liquibase** - Database migrations

### Communication & Data Transformation
- 🚀 **MapStruct** - DTO mapping
- 🔑 **JWT (JSON Web Tokens)** - User authentication
- 📢 **Telegram Bot API** - Notification service

### Development & Documentation
- 📜 **Swagger** - Interactive API documentation
- ⚠ **GlobalExceptionHandler** - Unified error handling

---

## Setting Up Locally 🏃‍♂️
### **Prerequisites**
- Java (JDK 17+)
- Docker & Docker Compose
- Maven (3.8+)
- PostgreSQL (optional, for local database setup)

### **Steps to Run**
1. Clone the repository:
## Getting Started 🚀
Clone the repository using the following command:
   ```bash
   git clone https://github.com/Lessorg/booking-app
   cd booking-app
   ```
2. Create `.env` file (based on `.env-template`) and update configurations.
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run with Docker:
   ```bash
   docker-compose up -d
   ```
5. Access API documentation: [Swagger UI](http://localhost:8083/swagger-ui.html)

---

## Scheduled Task ⏳
- Runs **daily** to check for **expired bookings**
- Updates expired bookings and **notifies users via Telegram**
- Sends a fallback message if no expired bookings are found

---

## 📜 Troubleshooting

- **Docker Issues**: Ensure Docker Desktop is running. Restart Docker if any containers fail to start.
- **Port Conflicts**: Check if the ports specified in the `.env` file (e.g., 8080 for the app, 3306 for PostgreSQL) are available. Modify them in `.env` if necessary.

Enjoy your journey with the Booking API! 📅🏡
