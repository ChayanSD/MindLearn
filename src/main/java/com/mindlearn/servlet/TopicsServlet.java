package com.mindlearn.servlet;

import com.mindlearn.dao.MindmapDao;
import com.mindlearn.dao.MindmapDao.MindmapRecord;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TopicsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        
        // Handle delete request
        String deleteParam = request.getParameter("delete");
        if (deleteParam != null && !deleteParam.isEmpty()) {
            try {
                int deleteId = Integer.parseInt(deleteParam);
                boolean deleted = MindmapDao.deleteMindmap(deleteId, userId);
                if (deleted) {
                    LOGGER.info("Mindmap deleted: " + deleteId);
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid delete ID", e);
            }
        }
        
        // Handle view request (load specific mindmap)
        String viewParam = request.getParameter("view");
        if (viewParam != null && !viewParam.isEmpty()) {
            try {
                int viewId = Integer.parseInt(viewParam);
                MindmapRecord record = MindmapDao.getMindmapById(viewId, userId);
                if (record != null) {
                    request.setAttribute("topic", record.getTopic());
                    request.setAttribute("mindmapJson", record.getMindmapData());
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid view ID", e);
            }
        }

        // Load user's mindmap history
        List<MindmapRecord> history = MindmapDao.getUserMindmaps(userId);
        request.setAttribute("mindmapHistory", history);

        request.getRequestDispatcher("/topics.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String topic = request.getParameter("topic");
        if (topic == null || topic.trim().isEmpty()) {
            request.setAttribute("error", "Please enter a topic");
            request.getRequestDispatcher("/topics.jsp").forward(request, response);
            return;
        }

        // Check if this is an AJAX request
        String ajaxHeader = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);

        try {
            request.setAttribute("topic", topic.trim());
            
            String mindmapJson = com.mindlearn.util.GeminiService.generateMindmap(topic.trim());
            request.setAttribute("mindmapJson", mindmapJson);

            // Save mindmap to database
            int userId = (int) session.getAttribute("userId");
            int mindmapId = MindmapDao.saveMindmap(userId, topic.trim(), mindmapJson);
            LOGGER.info("Mindmap saved with ID: " + mindmapId);

            // Refresh history for display
            List<MindmapRecord> history = MindmapDao.getUserMindmaps(userId);
            request.setAttribute("mindmapHistory", history);

            if (isAjax) {
                // For AJAX, render just the mindmap result and history
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                
                // Parse mindmap for rendering
                org.json.JSONObject mindmap = new org.json.JSONObject(mindmapJson);
                String topicTitle = mindmap.optString("topic", topic.trim());
                org.json.JSONArray subtopics = mindmap.optJSONArray("subtopics");
                
                StringBuilder html = new StringBuilder();
                
                // Build mindmap display HTML
                html.append("<div class='mindmap-display' data-mindmap='").append(mindmapJson.replace("'", "&#39;")).append("'>\n");
                html.append("<div class='mindmap-topic-header'>\n");
                html.append("<h2>").append(escapeHtml(topicTitle)).append("</h2>\n");
                html.append("<button class='download-btn' onclick='downloadMindmap()'>\n");
                html.append("<svg width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><path d='M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4'></path><polyline points='7 10 12 15 17 10'></polyline><line x1='12' y1='15' x2='12' y2='3'></line></svg>\n");
                html.append("Download PNG</button>\n");
                html.append("</div>\n");
                html.append("<div id='jsmind_container'></div>\n");
                html.append("</div>");
                
                // Build history HTML
                html.append("<div class='history-section'>\n");
                html.append("<h2>Your Mindmaps</h2>\n");
                if (history != null && !history.isEmpty()) {
                    html.append("<div class='history-list'>\n");
                    for (MindmapRecord record : history) {
                        html.append("<div class='history-item'>\n");
                        html.append("<div class='history-item-info'>\n");
                        html.append("<div class='history-item-topic'>").append(escapeHtml(record.getTopic())).append("</div>\n");
                        html.append("<div class='history-item-date'>").append(record.getCreatedAt()).append("</div>\n");
                        html.append("</div>\n");
                        html.append("<div class='history-item-actions'>\n");
                        html.append("<button class='history-btn view-btn' onclick='viewHistory(").append(record.getId()).append(")'>View</button>\n");
                        html.append("<button class='history-btn delete-btn' onclick='deleteHistory(").append(record.getId()).append(")'>Delete</button>\n");
                        html.append("</div>\n");
                        html.append("</div>\n");
                    }
                    html.append("</div>\n");
                } else {
                    html.append("<p class='no-history'>No mindmaps generated yet</p>\n");
                }
                html.append("</div>");
                
                response.getWriter().write(html.toString());
            } else {
                request.getRequestDispatcher("/topics.jsp").forward(request, response);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating mindmap", e);
            
            if (isAjax) {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                StringBuilder html = new StringBuilder();
                html.append("<div class='error-message'>");
                html.append("Failed to generate mindmap: ").append(escapeHtml(e.getMessage()));
                html.append("</div>");
                response.getWriter().write(html.toString());
            } else {
                request.setAttribute("error", "Failed to generate mindmap: " + e.getMessage());
                
                // Still load history on error
                try {
                    int userId = (int) session.getAttribute("userId");
                    List<MindmapRecord> history = MindmapDao.getUserMindmaps(userId);
                    request.setAttribute("mindmapHistory", history);
                } catch (Exception historyEx) {
                    LOGGER.log(Level.WARNING, "Error loading history after generation failure", historyEx);
                }
                
                request.getRequestDispatcher("/topics.jsp").forward(request, response);
            }
        }
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&': sb.append("\u0026\u0026\u0061\u006D\u0070\u003B"); break;
                case '<': sb.append("\u0026\u006C\u0074\u003B"); break;
                case '>': sb.append("\u0026\u0067\u0074\u003B"); break;
                case '"': sb.append("\u0026\u0071\u0075\u006F\u0074\u003B"); break;
                case '\'': sb.append("\u0026\u0023\u0033\u0039\u003B"); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }
}