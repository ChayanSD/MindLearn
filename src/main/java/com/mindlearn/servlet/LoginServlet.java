package com.mindlearn.servlet;

import com.mindlearn.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            response.sendRedirect(request.getContextPath() + "/topics");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Email and password are required");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT id, email, password FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        
                        // Check if password looks like a BCrypt hash (starts with $2a$, $2b$, or $2y$)
                        if (storedPassword != null && storedPassword.length() >= 7 
                                && storedPassword.startsWith("$2")) {
                            // Verify using BCrypt
                            try {
                                if (BCrypt.checkpw(password, storedPassword)) {
                                    HttpSession session = request.getSession();
                                    session.setAttribute("userId", rs.getInt("id"));
                                    session.setAttribute("email", rs.getString("email"));
                                    response.sendRedirect(request.getContextPath() + "/topics");
                                    return;
                                }
                            } catch (IllegalArgumentException e) {
                                // Invalid BCrypt hash, treat as legacy
                            }
                        } else {
                            // For legacy plain-text passwords, verify directly
                            if (password.equals(storedPassword)) {
                                // Update to BCrypt hash for future logins
                                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                                String updateSql = "UPDATE users SET password = ? WHERE id = ?";
                                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                    updateStmt.setString(1, hashedPassword);
                                    updateStmt.setInt(2, rs.getInt("id"));
                                    updateStmt.executeUpdate();
                                }
                                
                                HttpSession session = request.getSession();
                                session.setAttribute("userId", rs.getInt("id"));
                                session.setAttribute("email", rs.getString("email"));
                                response.sendRedirect(request.getContextPath() + "/topics");
                                return;
                            }
                        }
                    }
                }
            }
            
            request.setAttribute("error", "Invalid email or password");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            
        } catch (SQLException e) {
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}