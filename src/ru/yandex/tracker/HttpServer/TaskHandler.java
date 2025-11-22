package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(Gson gson, TaskManager manager) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            if ("GET".equalsIgnoreCase(method)) {
                Integer id = getIdFromQuery(query);
                if (id == null) {
                    List<Task> all = manager.getAllTasks();
                    sendText(exchange, gson.toJson(all));
                } else {
                    Task t = manager.getTask(id);
                    if (t == null) sendNotFound(exchange);
                    else sendText(exchange, gson.toJson(t));
                }
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                Task task = gson.fromJson(readBody(exchange), Task.class);
                if (task == null) {
                    sendServerError(exchange, "{\"error\":\"Invalid body\"}");
                    return;
                }

                try {
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
                } catch (IllegalArgumentException e) {
                    sendHasInteractions(exchange, "{\"error\":\"" + e.getMessage() + "\"}");
                }
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                Integer id = getIdFromQuery(query);
                if (id == null) {
                    manager.deleteAllTasks();
                    sendText(exchange, "{\"result\":\"all tasks deleted\"}");
                } else {
                    if (manager.getTask(id) == null) sendNotFound(exchange);
                    else {
                        manager.deleteTask(id);
                        sendText(exchange, "{\"result\":\"deleted\"}");
                    }
                }
                return;
            }

            exchange.sendResponseHeaders(405, -1);
            exchange.close();

        } catch (Exception e) {
            sendServerError(exchange, "{\"error\":\"Internal Server Error\"}");
        }
    }
}