package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpTaskServer {
    private final TaskManager manager;
    private final Gson gson;
    private final HttpServer server;

    public HttpTaskServer(TaskManager manager, Gson gson) throws IOException {
        this.manager = manager;
        this.gson = gson;
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        createContexts();
    }

    private void createContexts() {
        // ========= TASKS =========
        server.createContext("/tasks", exchange -> {
            try {
                switch (exchange.getRequestMethod()) {
                    case "GET" -> handleGetTask(exchange);
                    case "POST" -> handlePostTask(exchange);
                    case "DELETE" -> handleDeleteTask(exchange);
                    default -> send(exchange, 405, "");
                }
            } finally {
                exchange.close();
            }
        });

        // ========= SUBTASKS =========
        server.createContext("/subtasks", exchange -> {
            try {
                switch (exchange.getRequestMethod()) {
                    case "GET" -> handleGetSubtask(exchange);
                    case "POST" -> handlePostSubtask(exchange);
                    case "DELETE" -> handleDeleteSubtask(exchange);
                    default -> send(exchange, 405, "");
                }
            } finally {
                exchange.close();
            }
        });

        // ========= EPICS =========
        server.createContext("/epics", exchange -> {
            try {
                switch (exchange.getRequestMethod()) {
                    case "GET" -> handleGetEpic(exchange);
                    case "POST" -> handlePostEpic(exchange);
                    case "DELETE" -> handleDeleteEpic(exchange);
                    default -> send(exchange, 405, "");
                }
            } finally {
                exchange.close();
            }
        });

        // ========= SUBTASKS OF EPIC =========
        server.createContext("/epics/subtasks", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery(); // id=...
                int epicId = Integer.parseInt(query.split("=")[1]);
                List<Subtask> subtasks = manager.getSubtasksOfEpic(epicId);
                send(exchange, 200, gson.toJson(subtasks));
            } finally {
                exchange.close();
            }
        });

        // ========= HISTORY =========
        server.createContext("/history", exchange -> {
            try {
                send(exchange, 200, gson.toJson(manager.getHistory()));
            } finally {
                exchange.close();
            }
        });

        // ========= PRIORITIZED =========
        server.createContext("/prioritized", exchange -> {
            try {
                send(exchange, 200, gson.toJson(manager.getPrioritizedTasks()));
            } finally {
                exchange.close();
            }
        });
    }

    // ====================================
    // ============ TASK HANDLERS ==========
    // ====================================

    private void handleGetTask(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            send(exchange, 200, gson.toJson(manager.getAllTasks()));
            return;
        }

        int id = Integer.parseInt(query.split("=")[1]);
        Task task = manager.getTask(id);
        if (task == null) send(exchange, 404, "");
        else send(exchange, 200, gson.toJson(task));
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);

        if (task.getId() == 0) manager.createTask(task);
        else manager.updateTask(task);

        send(exchange, 201, gson.toJson(task));
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            manager.deleteAllTasks();
            send(exchange, 200, "");
            return;
        }

        int id = Integer.parseInt(query.split("=")[1]);
        manager.deleteTask(id);
        send(exchange, 200, "");
    }

    // ====================================
    // ============ SUBTASK HANDLERS =======
    // ====================================

    private void handleGetSubtask(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            send(exchange, 200, gson.toJson(manager.getAllSubtasks()));
            return;
        }

        int id = Integer.parseInt(query.split("=")[1]);
        Subtask subtask = manager.getSubtask(id);
        if (subtask == null) send(exchange, 404, "");
        else send(exchange, 200, gson.toJson(subtask));
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        if (subtask.getId() == 0) manager.createSubtask(subtask);
        else manager.updateSubtask(subtask);

        send(exchange, 201, gson.toJson(subtask));
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            manager.deleteAllSubtasks();
            send(exchange, 200, "");
            return;
        }

        int id = Integer.parseInt(query.split("=")[1]);
        manager.deleteSubtask(id);
        send(exchange, 200, "");
    }

    // ====================================
    // ============ EPIC HANDLERS ==========
    // ====================================

    private void handleGetEpic(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            send(exchange, 200, gson.toJson(manager.getAllEpicTasks()));
            return;
        }

        int id = Integer.parseInt(query.split("=")[1]);
        Epic epic = manager.getEpicTask(id);
        if (epic == null) send(exchange, 404, "");
        else send(exchange, 200, gson.toJson(epic));
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic.getId() == 0) manager.createEpicTask(epic);
        else manager.updateEpicTask(epic);

        send(exchange, 201, gson.toJson(epic));
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            manager.deleteAllEpicTasks();
            send(exchange, 200, "");
            return;
        }

        int id = Integer.parseInt(query.split("=")[1]);
        manager.deleteEpicTask(id);
        send(exchange, 200, "");
    }

    // ====================================
    // ============ UTIL ===================
    // ====================================

    private void send(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}