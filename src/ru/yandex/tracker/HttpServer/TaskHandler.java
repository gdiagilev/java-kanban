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
                    List<Task> tasks = manager.getAllTasks();
                    sendText(exchange, gson.toJson(tasks));
                } else {
                    Task task = manager.getTask(id);
                    if (task == null) sendNotFound(exchange);
                    else sendText(exchange, gson.toJson(task));
                }
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                Task task = gson.fromJson(readBody(exchange), Task.class);
                if (task.getId() == 0) {
                    manager.createTask(task);
                    sendCreated(exchange, gson.toJson(task));
                } else {
                    manager.updateTask(task);
                    sendText(exchange, gson.toJson(task));
                }
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                Integer id = getIdFromQuery(query);
                if (id == null) {
                    manager.deleteAllTasks();
                    sendText(exchange, "{\"result\":\"all tasks deleted\"}");
                } else {
                    manager.deleteTask(id);
                    sendText(exchange, "{\"result\":\"deleted\"}");
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