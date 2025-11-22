package ru.yandex.tracker.HttpServer;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager manager;
    private final Gson gson;

    public HttpTaskServer(TaskManager manager, Gson gson) throws IOException {
        this.manager = manager;
        this.gson = gson;

        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TaskHandler(this.gson, manager));
        server.createContext("/epics", new EpicHandler(this.gson, manager));
        server.createContext("/subtasks", new SubtaskHandler(this.gson, manager));
        server.createContext("/history", new HistoryHandler(this.gson, manager));
        server.createContext("/prioritized", new PrioritizedHandler(this.gson, manager));
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this(manager, createDefaultGson());
    }

    public static Gson createDefaultGson() {
        GsonBuilder builder = new GsonBuilder().serializeNulls();

        builder.registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
            @Override
            public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.toSeconds());
            }
        });
        builder.registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
            @Override
            public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return Duration.ofSeconds(json.getAsLong());
            }
        });

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        builder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(formatter));
            }
        });
        builder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return LocalDateTime.parse(json.getAsString(), formatter);
            }
        });

        return builder.create();
    }

    public void start() {
        server.start();
        System.out.println("HTTP Task Server started on port " + PORT);
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
        System.out.println("HTTP Task Server stopped");
    }

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = ru.yandex.tracker.Service.Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }
}