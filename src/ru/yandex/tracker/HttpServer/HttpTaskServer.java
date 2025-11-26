package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public class HttpTaskServer {
    private final TaskManager manager;
    private final Gson gson;
    private HttpServer server;

    public HttpTaskServer(TaskManager manager, Gson gson) throws IOException {
        this.manager = manager;
        this.gson = gson;
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
        createContexts();
    }

    private void createContexts() {
        server.createContext("/epics", exchange -> {
            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                try (var reader = exchange.getRequestBody()) {
                    Epic epic = gson.fromJson(reader.toString(), Epic.class);
                    manager.createEpicTask(epic);
                    exchange.sendResponseHeaders(201, 0);
                } catch (Exception e) {
                    exchange.sendResponseHeaders(400, 0);
                }
            } else if ("GET".equalsIgnoreCase(method)) {
                List<Epic> epics = manager.getAllEpicTasks();
                String json = gson.toJson(epics);
                exchange.sendResponseHeaders(200, json.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(json.getBytes());
                }
            }
            exchange.close();
        });

        server.createContext("/epics/subtasks", exchange -> {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                var query = exchange.getRequestURI().getQuery(); // id=...
                int epicId = Integer.parseInt(query.split("=")[1]);
                List<Subtask> subtasks = manager.getSubtasksOfEpic(epicId);
                String json = gson.toJson(subtasks);
                exchange.sendResponseHeaders(200, json.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(json.getBytes());
                }
            }
            exchange.close();
        });
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}