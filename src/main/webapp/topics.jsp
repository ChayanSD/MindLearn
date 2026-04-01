<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MindLearn - Topics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <jsp:include page="nav.jsp" />
    
    <div class="container">
        <h1>Learning Topics</h1>
        <p>Explore our available topics and start your learning journey.</p>
        
        <div class="topics-grid">
            <div class="topic-card">
                <h3>Java Fundamentals</h3>
                <p>Learn the basics of Java programming including variables, loops, and methods.</p>
            </div>
            
            <div class="topic-card">
                <h3>Web Development</h3>
                <p>Master HTML, CSS, and JavaScript to build modern web applications.</p>
            </div>
            
            <div class="topic-card">
                <h3>Database Design</h3>
                <p>Understand database concepts, SQL, and data modeling techniques.</p>
            </div>
            
            <div class="topic-card">
                <h3>Object-Oriented Programming</h3>
                <p>Explore OOP principles: encapsulation, inheritance, polymorphism, and abstraction.</p>
            </div>
            
            <div class="topic-card">
                <h3>Data Structures</h3>
                <p>Learn essential data structures like arrays, lists, trees, and graphs.</p>
            </div>
            
            <div class="topic-card">
                <h3>Algorithms</h3>
                <p>Master sorting, searching, and optimization algorithms.</p>
            </div>
        </div>
    </div>
</body>
</html>