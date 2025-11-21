package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

        public TaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange, query);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange, query);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                exchange.close();
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendServerError(exchange, "{\"error\":\"Internal Server Error\"}");
        }
    }

    private void handleGet(HttpExchange exchange, String query) throws IOException {
        Integer id = getIdFromQuery(query);
        if (id == null) {
            List<Task> all = manager.getAllTasks();
            sendText(exchange, gson.toJson(all));
        } else {
            Task task = manager.getTask(id);
            if (task == null) sendNotFound(exchange);
            else sendText(exchange, gson.toJson(task));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange.getRequestBody());
        Task task = gson.fromJson(body, Task.class);
        if (task == null) {
            sendServerError(exchange, "{\"error\":\"Неверное тело запроса\"}");
            return;
        }

        if (task.getId() == 0) {
            manager.createTask(task);
            sendCreated(exchange, gson.toJson(task));
        } else {
            if (manager.getTask(task.getId()) == null) {
                sendNotFound(exchange);
                return;
            }
            manager.updateTask(task);
            sendText(exchange, gson.toJson(task));
        }
    }

    private void handleDelete(HttpExchange exchange, String query) throws IOException {
        Integer id = getIdFromQuery(query);
        if (id == null) {
            manager.deleteAllTasks();
            sendText(exchange, "{\"result\":\"all tasks deleted\"}");
        } else {
            Task task = manager.getTask(id);
            if (task == null) sendNotFound(exchange);
            else {
                manager.deleteTask(id);
                sendText(exchange, "{\"result\":\"deleted\"}");
            }
        }
    }

    private String readBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
}