package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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

        // ---------- TASKS ----------
        server.createContext("/tasks", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("/tasks".equals(path)) {
                    if ("GET".equals(method)) {
                        List<Task> tasks = manager.getAllTasks();
                        sendJson(exchange, tasks, 200);
                        return;
                    }

                    if ("POST".equals(method)) {
                        String body = readBody(exchange);
                        Task task = gson.fromJson(body, Task.class);
                        manager.createTask(task);
                        sendStatus(exchange, 201);
                        return;
                    }

                    if ("DELETE".equals(method)) {
                        manager.deleteAllTasks();
                        sendStatus(exchange, 200);
                        return;
                    }
                }

                // /tasks/{id}
                if (path.startsWith("/tasks/")) {
                    int id = Integer.parseInt(path.substring("/tasks/".length()));

                    if ("GET".equals(method)) {
                        Task task = manager.getTask(id);
                        sendJson(exchange, task, 200);
                        return;
                    }

                    if ("DELETE".equals(method)) {
                        manager.deleteTask(id);
                        sendStatus(exchange, 200);
                        return;
                    }

                    if ("POST".equals(method)) {
                        String body = readBody(exchange);
                        Task task = gson.fromJson(body, Task.class);
                        task.setId(id);
                        manager.updateTask(task);
                        sendStatus(exchange, 200);
                        return;
                    }
                }

                sendStatus(exchange, 404);
            } catch (Exception e) {
                sendStatus(exchange, 500);
            }
        });


        // ---------- SUBTASKS ----------
        server.createContext("/subtasks", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("/subtasks".equals(path)) {
                    if ("GET".equals(method)) {
                        List<Subtask> list = manager.getAllSubtasks();
                        sendJson(exchange, list, 200);
                        return;
                    }

                    if ("POST".equals(method)) {
                        String body = readBody(exchange);
                        Subtask sub = gson.fromJson(body, Subtask.class);
                        manager.createSubtask(sub);
                        sendStatus(exchange, 201);
                        return;
                    }

                    if ("DELETE".equals(method)) {
                        manager.deleteAllSubtasks();
                        sendStatus(exchange, 200);
                        return;
                    }
                }

                if (path.startsWith("/subtasks/")) {
                    int id = Integer.parseInt(path.substring("/subtasks/".length()));

                    if ("GET".equals(method)) {
                        Subtask sub = manager.getSubtask(id);
                        sendJson(exchange, sub, 200);
                        return;
                    }

                    if ("DELETE".equals(method)) {
                        manager.deleteSubtask(id);
                        sendStatus(exchange, 200);
                        return;
                    }

                    if ("POST".equals(method)) {
                        String body = readBody(exchange);
                        Subtask sub = gson.fromJson(body, Subtask.class);
                        sub.setId(id);
                        manager.updateSubtask(sub);
                        sendStatus(exchange, 200);
                        return;
                    }
                }

                sendStatus(exchange, 404);
            } catch (Exception e) {
                sendStatus(exchange, 500);
            }
        });


        // ---------- EPICS ----------
        server.createContext("/epics", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("/epics".equals(path)) {
                    if ("GET".equals(method)) {
                        sendJson(exchange, manager.getAllEpicTasks(), 200);
                        return;
                    }

                    if ("POST".equals(method)) {
                        String body = readBody(exchange);
                        Epic epic = gson.fromJson(body, Epic.class);
                        manager.createEpicTask(epic);
                        sendStatus(exchange, 201);
                        return;
                    }

                    if ("DELETE".equals(method)) {
                        manager.deleteAllEpicTasks();
                        sendStatus(exchange, 200);
                        return;
                    }
                }

                if (path.startsWith("/epics/")) {
                    int id = Integer.parseInt(path.substring("/epics/".length()));

                    if ("GET".equals(method)) {
                        sendJson(exchange, manager.getEpicTask(id), 200);
                        return;
                    }

                    if ("DELETE".equals(method)) {
                        manager.deleteEpicTask(id);
                        sendStatus(exchange, 200);
                        return;
                    }

                    if ("POST".equals(method)) {
                        String body = readBody(exchange);
                        Epic epic = gson.fromJson(body, Epic.class);
                        epic.setId(id);
                        manager.updateEpicTask(epic);
                        sendStatus(exchange, 200);
                        return;
                    }
                }

                sendStatus(exchange, 404);
            } catch (Exception e) {
                sendStatus(exchange, 500);
            }
        });


        // ---------- SUBTASKS OF EPIC ----------
        server.createContext("/epics/subtasks", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String query = exchange.getRequestURI().getQuery();

                if (!"GET".equals(method) || query == null) {
                    sendStatus(exchange, 400);
                    return;
                }

                int epicId = Integer.parseInt(query.split("=")[1]);
                List<Subtask> list = manager.getSubtasksOfEpic(epicId);
                sendJson(exchange, list, 200);
            } catch (Exception e) {
                sendStatus(exchange, 500);
            }
        });


        // ---------- HISTORY ----------
        server.createContext("/history", exchange -> {
            try {
                List<Task> history = manager.getHistory();
                sendJson(exchange, history, 200);
            } catch (Exception e) {
                sendStatus(exchange, 500);
            }
        });


        // ---------- PRIORITIZED ----------
        server.createContext("/prioritized", exchange -> {
            try {
                sendJson(exchange, manager.getPrioritizedTasks(), 200);
            } catch (Exception e) {
                sendStatus(exchange, 500);
            }
        });
    }


    // ====================== UTILS =======================

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes());
    }

    private void sendStatus(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, 0);
        exchange.getResponseBody().close();
    }

    private void sendJson(HttpExchange exchange, Object obj, int status) throws IOException {
        String json = gson.toJson(obj);
        byte[] bytes = json.getBytes();
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public void start() {
        server.start();
        System.out.println("HTTP server started on 8080");
    }

    public void stop() {
        server.stop(0);
    }
}