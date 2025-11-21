package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Service.TaskManager;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
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
                if (id == null) sendText(exchange, gson.toJson(manager.getAllSubtasks()));
                else {
                    Subtask sub = manager.getSubtask(id);
                    if (sub == null) sendNotFound(exchange);
                    else sendText(exchange, gson.toJson(sub));
                }
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange.getRequestBody());
                Subtask sub = gson.fromJson(body, Subtask.class);
                if (sub == null) {
                    sendServerError(exchange, "{\"error\":\"Неверное тело\"}");
                    return;
                }
                if (sub.getId() == 0) {
                    if (manager.getEpicTask(sub.getEpicId()) == null) {
                        sendNotFound(exchange);
                        return;
                    }
                    try {
                        manager.createSubtask(sub);
                        sendCreated(exchange, gson.toJson(sub));
                    } catch (IllegalArgumentException e) {
                        sendHasInteractions(exchange, "{\"error\":\"" + e.getMessage() + "\"}");
                    }
                } else {
                    if (manager.getSubtask(sub.getId()) == null) {
                        sendNotFound(exchange);
                        return;
                    }
                    try {
                        manager.updateSubtask(sub);
                        sendText(exchange, gson.toJson(sub));
                    } catch (IllegalArgumentException e) {
                        sendHasInteractions(exchange, "{\"error\":\"" + e.getMessage() + "\"}");
                    }
                }
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                Integer id = getIdFromQuery(query);
                if (id == null) {
                    manager.deleteAllSubtasks();
                    sendText(exchange, "{\"result\":\"all subtasks deleted\"}");
                } else {
                    if (manager.getSubtask(id) == null) sendNotFound(exchange);
                    else {
                        manager.deleteSubtask(id);
                        sendText(exchange, "{\"result\":\"deleted\"}");
                    }
                }
                return;
            }

            exchange.sendResponseHeaders(405, -1);
            exchange.close();

        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception ex) {
            sendServerError(exchange, "{\"error\":\"Internal Server Error\"}");
        }
    }

    private String readBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
}