package com.mindlearn.servlet;

import com.mindlearn.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "All fields are required");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        
        // Hash the password using BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO users (email, password) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, hashedPassword);
                stmt.executeUpdate();
            }
            
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
            
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Duplicate entry") || errorMessage.contains("UNIQUE")) {
                request.setAttribute("error", "Email already exists");
            } else {
                request.setAttribute("error", "Database error: " + errorMessage);
            }
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}