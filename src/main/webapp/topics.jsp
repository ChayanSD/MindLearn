<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.mindlearn.dao.MindmapDao.MindmapRecord" %>
<%
    String error = (String) request.getAttribute("error");
    Boolean loading = (Boolean) request.getAttribute("loading");
    String topic = (String) request.getAttribute("topic");
    String mindmapJson = (String) request.getAttribute("mindmapJson");
    
    // Get history from request attribute
    List<MindmapRecord> mindmapHistory = (List<MindmapRecord>) request.getAttribute("mindmapHistory");
    if (mindmapHistory == null) {
        mindmapHistory = new ArrayList<>();
    }
    
    JSONObject mindmap = null;
    String topicTitle = "";
    JSONArray subtopics = new JSONArray();
    
    if (mindmapJson != null && !mindmapJson.isEmpty()) {
        try {
            mindmap = new JSONObject(mindmapJson);
            topicTitle = mindmap.optString("topic", topic);
            subtopics = mindmap.optJSONArray("subtopics");
        } catch (Exception e) {
            error = "Failed to parse mindmap data";
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MindLearn - AI Mindmap Generator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <link type="text/css" rel="stylesheet" href="https://cdn.jsdelivr.net/npm/jsmind@0.7.5/style/jsmind.css" />
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
    <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/jsmind@0.7.5/es6/jsmind.js"></script>
    <style>
        .generator-container {
            max-width: 1000px;
            margin: 0 auto;
            padding: 2rem;
            text-align: center;
        }
        .generator-form {
            background: #f8f9fa;
            padding: 2rem;
            border-radius: 8px;
            margin-bottom: 2rem;
        }
        .generator-form input[type="text"] {
            width: 70%;
            padding: 12px 16px;
            font-size: 16px;
            border: 2px solid #ddd;
            border-radius: 4px;
            margin-right: 10px;
        }
        .generator-form button {
            padding: 12px 24px;
            font-size: 16px;
            background: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .generator-form button:hover {
            background: #0056b3;
        }
        .generator-form button:disabled {
            background: #aaa;
            cursor: not-allowed;
        }
        .loading-spinner {
            display: inline-block;
            width: 40px;
            height: 40px;
            border: 4px solid #f3f3f3;
            border-top: 4px solid #007bff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 2rem 0;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        .loading-message {
            color: #666;
            font-size: 18px;
            margin-top: 1rem;
        }
        .loading-container {
            text-align: center;
            padding: 2rem;
        }
        .error-message {
            background: #f8d7da;
            color: #721c24;
            padding: 12px;
            border-radius: 4px;
            margin-bottom: 1rem;
        }
        .mindmap-display {
            text-align: left;
            background: white;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            margin-bottom: 2rem;
        }
        #jsmind_container {
            width: 100%;
            height: 600px;
            background-color: #f8f9fa;
            border-radius: 8px;
            border: 1px solid #e9ecef;
        }
        .mindmap-topic-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
            border-bottom: 2px solid #007bff;
            padding-bottom: 0.5rem;
        }
        .mindmap-topic-header h2 {
            font-size: 24px;
            color: #333;
            margin: 0;
        }
        .download-btn {
            padding: 10px 20px;
            background: #28a745;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            transition: background 0.2s, transform 0.1s;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .download-btn:hover {
            background: #218838;
            transform: translateY(-1px);
        }
        .download-btn:active {
            transform: translateY(0);
        }
        /* History Section Styles */
        .history-section {
            margin-top: 3rem;
            text-align: left;
        }
        .history-section h2 {
            color: #333;
            margin-bottom: 1rem;
            padding-bottom: 0.5rem;
            border-bottom: 2px solid #007bff;
        }
        .history-list {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 1rem;
        }
        .history-item {
            background: white;
            padding: 1rem;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .history-item:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.15);
        }
        .history-item-info {
            flex: 1;
        }
        .history-item-topic {
            font-size: 18px;
            font-weight: 600;
            color: #333;
            margin-bottom: 0.25rem;
        }
        .history-item-date {
            font-size: 13px;
            color: #888;
        }
        .history-item-actions {
            display: flex;
            gap: 0.5rem;
            border-top: 1px solid #eee;
            padding-top: 0.75rem;
        }
        .history-btn {
            flex: 1;
            padding: 8px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 13px;
            text-align: center;
            font-weight: 500;
        }
        .view-btn {
            background: #e7f1ff;
            color: #007bff;
        }
        .view-btn:hover {
            background: #007bff;
            color: white;
        }
        .delete-btn {
            background: #fff5f5;
            color: #dc3545;
        }
        .delete-btn:hover {
            background: #dc3545;
            color: white;
        }
        .no-history {
            color: #666;
            font-style: italic;
            padding: 1rem;
            text-align: center;
        }
        @media print {
            body * {
                visibility: hidden;
            }
            .mindmap-display, .mindmap-display * {
                visibility: visible !important;
            }
            .mindmap-display {
                position: absolute;
                left: 0;
                top: 0;
                width: 100%;
                box-shadow: none !important;
                padding: 0 !important;
                margin: 0 !important;
            }
            #jsmind_container {
                border: none !important;
                height: auto !important;
                min-height: 800px !important;
                overflow: visible !important;
            }
            .jsmind-inner {
                overflow: visible !important;
            }
            canvas {
                max-width: 100% !important;
                height: auto !important;
            }
            .download-btn, .navbar, .generator-form, .history-section {
                display: none !important;
            }
        }
    </style>
    <script>
        var _jm = null;

        function downloadMindmap() {
            const container = document.getElementById('jsmind_container');
            if (!_jm || !container) return;
            
            Swal.fire({
                title: 'Exporting Mindmap',
                text: 'Preparing your high-resolution image...',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                    
                    // Add a tiny delay to ensure everything is rendered
                    setTimeout(() => {
                        html2canvas(container, {
                            scale: 2, // High clarity
                            backgroundColor: '#f8f9fa',
                            logging: false,
                            useCORS: true
                        }).then(canvas => {
                            const link = document.createElement('a');
                            const topic = document.querySelector('.mindmap-topic-header h2')?.textContent || 'mindmap';
                            link.download = 'MindLearn-' + topic.replace(/\s+/g, '-') + '.png';
                            link.href = canvas.toDataURL('image/png');
                            link.click();
                            Swal.close();
                        }).catch(err => {
                            console.error('Download failed:', err);
                            Swal.fire('Export Failed', 'Unable to capture the mindmap. Try refreshing.', 'error');
                        });
                    }, 100);
                }
            });
        }

        function renderJsMind(jsonData) {
            if (!jsonData) return;
            
            try {
                const data = typeof jsonData === 'string' ? JSON.parse(jsonData) : jsonData;
                
                const mind = {
                    "meta": { "name": "MindLearn", "author": "MindLearn AI", "version": "1.0" },
                    "format": "node_tree",
                    "data": {
                        "id": "root",
                        "topic": data.topic || "Topic",
                        "children": []
                    }
                };

                if (data.subtopics) {
                    data.subtopics.forEach((sub, sIdx) => {
                        const subnode = {
                            "id": "sub_" + sIdx,
                            "topic": sub.title,
                            "direction": sIdx % 2 === 0 ? "right" : "left",
                            "children": []
                        };
                        
                        if (sub.items) {
                            sub.items.forEach((item, iIdx) => {
                                subnode.children.push({
                                    "id": "item_" + sIdx + "_" + iIdx,
                                    "topic": item
                                });
                            });
                        }
                        mind.data.children.push(subnode);
                    });
                }

                const options = {
                    container: 'jsmind_container',
                    editable: false,
                    theme: 'primary',
                    view: {
                        line_width: 2,
                        line_color: '#007bff'
                    },
                    layout: {
                        hspace: 30,
                        vspace: 20,
                        pspace: 13
                    }
                };
                
                if (_jm) {
                    _jm.show(mind);
                } else {
                    _jm = new jsMind(options);
                    _jm.show(mind);
                }
                
                // Center the mindmap
                setTimeout(() => {
                    if (_jm) _jm.view.center_root();
                }, 100);
                
            } catch (e) {
                console.error("Failed to render jsMind:", e);
            }
        }

        function viewHistory(mindmapId) {
            window.location.href = '${pageContext.request.contextPath}/topics?view=' + mindmapId;
        }
        
        function deleteHistory(mindmapId) {
            Swal.fire({
                title: 'Are you sure?',
                text: "You won't be able to revert this mindmap!",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#007bff',
                cancelButtonColor: '#dc3545',
                confirmButtonText: 'Yes, delete it!',
                heightAuto: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '${pageContext.request.contextPath}/topics?delete=' + mindmapId;
                }
            })
        }
        
        document.addEventListener('DOMContentLoaded', function() {
            // Render initial mindmap if exists
            const initialData = <%= mindmapJson != null ? mindmapJson : "null" %>;
            if (initialData) {
                renderJsMind(initialData);
            }

            const form = document.querySelector('.generator-form form');
            const submitBtn = form?.querySelector('button[type="submit"]');
            const topicInput = form?.querySelector('input[name="topic"]');
            
            if (form) {
                form.addEventListener('submit', function(e) {
                    e.preventDefault();
                    const topic = topicInput.value.trim();
                    if (!topic) return;
                    
                    submitBtn.disabled = true;
                    submitBtn.textContent = 'Generating...';
                    topicInput.disabled = true;
                    
                    document.querySelector('.mindmap-display')?.remove();
                    document.querySelector('.error-message')?.remove();
                    
                    const loadingDiv = document.createElement('div');
                    loadingDiv.className = 'loading-container';
                    loadingDiv.innerHTML = '<div class="loading-spinner"></div><p class="loading-message">AI is brain-storming "' + topic + '"...</p>';
                    form.parentElement.appendChild(loadingDiv);
                    
                    const params = new URLSearchParams();
                    params.append('topic', topic);
                    
                    fetch('${pageContext.request.contextPath}/topics', {
                        method: 'POST',
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        body: params
                    })
                    .then(response => response.text())
                    .then(html => {
                        loadingDiv.remove();
                        submitBtn.disabled = false;
                        submitBtn.textContent = 'Generate Mindmap';
                        topicInput.disabled = false;
                        
                        const parser = new DOMParser();
                        const doc = parser.parseFromString(html, 'text/html');
                        
                        const errorElement = doc.querySelector('.error-message');
                        const mindmapElement = doc.querySelector('.mindmap-display');
                        const formContainer = form.closest('.generator-form');
                        
                        if (errorElement) {
                            formContainer.after(errorElement);
                        } else if (mindmapElement) {
                            formContainer.after(mindmapElement);
                            
                            const jsonData = mindmapElement.dataset.mindmap;
                            if (jsonData) {
                                renderJsMind(jsonData);
                            }

                            const historySection = doc.querySelector('.history-section');
                            const existingHistory = document.body.querySelector('.history-section');
                            if (historySection && existingHistory) {
                                existingHistory.innerHTML = historySection.innerHTML;
                            }
                        }
                    })
                    .catch(error => {
                        loadingDiv.remove();
                        submitBtn.disabled = false;
                        topicInput.disabled = false;
                        console.error('Fetch error:', error);
                    });
                });
            }
        });
    </script>
</head>
<body>
    <jsp:include page="nav.jsp" />
    
    <div class="container">
        <div class="generator-container">
            <h1>AI Mindmap Generator</h1>
            <p>Visualize your learning journey with interactive mindmaps</p>
            
            <% if (error != null) { %>
                <div class="error-message"><%= error %></div>
            <% } %>
            
            <div class="generator-form">
                <form method="post" action="${pageContext.request.contextPath}/topics">
                    <input type="text" name="topic" placeholder="What do you want to learn today?" 
                           value="<%= topic != null ? topic : "" %>" required>
                    <button type="submit">Generate Mindmap</button>
                </form>
            </div>
            
            <% if (mindmap != null) { %>
                <div class="mindmap-display" data-mindmap='<%= mindmapJson %>'>
                    <div class="mindmap-topic-header">
                        <h2><%= topicTitle %></h2>
                        <button class="download-btn" onclick="downloadMindmap()">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                            Download PNG
                        </button>
                    </div>
                    <div id="jsmind_container"></div>
                </div>
            <% } %>
            
            <div class="history-section">
                <h2>Your Mindmaps</h2>
                <% if (mindmapHistory != null && !mindmapHistory.isEmpty()) { %>
                    <div class="history-list">
                        <% for (MindmapRecord record : mindmapHistory) { %>
                            <div class="history-item">
                                <div class="history-item-info">
                                    <div class="history-item-topic"><%= record.getTopic() %></div>
                                    <div class="history-item-date">Created on <%= record.getCreatedAt() %></div>
                                </div>
                                <div class="history-item-actions">
                                    <button class="history-btn view-btn" onclick="viewHistory(<%= record.getId() %>)">View Map</button>
                                    <button class="history-btn delete-btn" onclick="deleteHistory(<%= record.getId() %>)">Delete</button>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } else { %>
                    <p class="no-history">Start by generating a mindmap above!</p>
                <% } %>
            </div>
        </div>
    </div>
</body>
</html>
