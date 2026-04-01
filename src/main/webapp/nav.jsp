<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% 
    String userEmail = (String) session.getAttribute("email");
    boolean isLoggedIn = userEmail != null;
%>
<nav class="navbar">
    <div class="nav-container">
        <a href="${pageContext.request.contextPath}/" class="nav-brand">MindLearn</a>
        <ul class="nav-menu">
            <li><a href="${pageContext.request.contextPath}/">Home</a></li>
            <% if (isLoggedIn) { %>
                <li><a href="${pageContext.request.contextPath}/topics">Topics</a></li>
                <li><span class="nav-user"><%= userEmail %></span></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-logout">Logout</a></li>
            <% } else { %>
                <li><a href="${pageContext.request.contextPath}/login">Login</a></li>
                <li><a href="${pageContext.request.contextPath}/register">Register</a></li>
            <% } %>
        </ul>
    </div>
</nav>