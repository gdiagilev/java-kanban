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

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerPrioritizedTest {

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
    void testPrioritizedTasksOrder() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Desc1", Status.NEW, LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(10));
        Task task2 = new Task("Task2", "Desc2", Status.NEW, LocalDateTime.now().plusMinutes(5), Duration.ofMinutes(15));
        Task task3 = new Task("Task3", "Desc3", Status.NEW, LocalDateTime.now().plusMinutes(10), Duration.ofMinutes(20));

        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(3, prioritized.length);

        // Проверяем порядок по времени начала задачи
        assertEquals("Task2", prioritized[0].getName());
        assertEquals("Task3", prioritized[1].getName());
        assertEquals("Task1", prioritized[2].getName());
    }
}