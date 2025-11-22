package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(Gson gson, TaskManager manager) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, gson.toJson(manager.getHistory()));
                return;
            }

            exchange.sendResponseHeaders(405, -1);
            exchange.close();
        } catch (Exception e) {
            sendServerError(exchange, "{\"error\":\"Internal Server Error\"}");
        }
    }
}