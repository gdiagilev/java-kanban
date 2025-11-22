package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(Gson gson, TaskManager manager) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            if ("GET".equalsIgnoreCase(method)) {
                if (path.endsWith("/subtasks")) {
                    Integer id = getIdFromQuery(query);
                    if (id == null) {
                        sendServerError(exchange, "{\"error\":\"Invalid id\"}");
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
                Epic epic = gson.fromJson(readBody(exchange), Epic.class);
                if (epic == null) {
                    sendServerError(exchange, "{\"error\":\"Invalid body\"}");
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
                    if (manager.getEpicTask(id) == null) sendNotFound(exchange);
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
}