# MindLearn

A simple learning management system built with JSP, Servlet, and MySQL.

## Tech Stack
- Java 17
- JSP & Servlets
- MySQL Database
- BCrypt for password hashing

## Setup

1. **Clone the repository**
2. **Configure database:**
   - Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties`
   - Update with your MySQL credentials
3. **Start MySQL** and create the database:
   ```sql
   CREATE DATABASE mindlearn_db;
   ```
4. **Run the application** using Tomcat or `mvn tomcat7:run`

## Project Structure
- `src/main/java/com/mindlearn/servlet/` - Java Servlets
- `src/main/webapp/` - JSP files and static resources
- `src/main/resources/` - Database configuration and schema

## Features
- User Registration & Login
- Password hashing with BCrypt
- Session management
- Topics page (protected)