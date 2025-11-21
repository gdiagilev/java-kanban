package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equalsIgnoreCase(method)) {
                if (path.endsWith("/subtasks")) {
                    Integer id = getIdFromQuery(query);
                    if (id == null) {
                        sendServerError(exchange, "{\"error\":\"Некорректный id\"}");
                        return;
                    }
                    List<Subtask> subs = manager.getAllSubTasksByEpicId(id);
                    sendText(exchange, gson.toJson(subs));
                    return;
                }

                Integer id = getIdFromQuery(query);
                if (id == null) {
                    sendText(exchange, gson.toJson(manager.getAllEpicTasks()));
                } else {
                    Epic epic = manager.getEpicTask(id);
                    if (epic == null) sendNotFound(exchange);
                    else sendText(exchange, gson.toJson(epic));
                }
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange.getRequestBody());
                Epic epic = gson.fromJson(body, Epic.class);
                if (epic == null) {
                    sendServerError(exchange, "{\"error\":\"Неверное тело\"}");
                    return;
                }
                if (epic.getId() == 0) {
                    manager.createEpicTask(epic);
                    sendCreated(exchange, gson.toJson(epic));
                } else {
                    if (manager.getEpicTask(epic.getId()) == null) {
                        sendNotFound(exchange);
                        return;
                    }
                    manager.updateEpicTask(epic);
                    sendText(exchange, gson.toJson(epic));
                }
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                Integer id = getIdFromQuery(query);
                if (id == null) {
                    manager.deleteAllEpicTasks();
                    sendText(exchange, "{\"result\":\"all epics deleted\"}");
                } else {
                    Epic e = manager.getEpicTask(id);
                    if (e == null) sendNotFound(exchange);
                    else {
                        manager.deleteEpicTask(id);
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