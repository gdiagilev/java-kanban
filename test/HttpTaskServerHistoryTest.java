package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerHistoryTest {

    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();

        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testHistoryAfterGettingTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Desc1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Task2", "Desc2", Status.NEW, LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(20));
        manager.createTask(task1);
        manager.createTask(task2);

                client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=" + task1.getId()))
                .GET().build(), HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=" + task2.getId()))
                .GET().build(), HttpResponse.BodyHandlers.ofString());

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, history.length);
        assertEquals("Task1", history[0].getName());
        assertEquals("Task2", history[1].getName());
    }
}