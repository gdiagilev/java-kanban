package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(Gson gson, TaskManager manager) {
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
                    List<Subtask> subtasks = manager.getAllSubtasks();
                    sendText(exchange, gson.toJson(subtasks));
                } else {
                    Subtask sub = manager.getSubtask(id);
                    if (sub == null) sendNotFound(exchange);
                    else sendText(exchange, gson.toJson(sub));
                }
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                Subtask subtask = gson.fromJson(readBody(exchange), Subtask.class);
                if (subtask.getId() == 0) {
                    manager.createSubtask(subtask);
                    sendCreated(exchange, gson.toJson(subtask));
                } else {
                    manager.updateSubtask(subtask);
                    sendText(exchange, gson.toJson(subtask));
                }
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                Integer id = getIdFromQuery(query);
                if (id == null) {
                    manager.deleteAllSubtasks();
                    sendText(exchange, "{\"result\":\"all subtasks deleted\"}");
                } else {
                    manager.deleteSubtask(id);
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