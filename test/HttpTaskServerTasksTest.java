package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Service.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpTaskServerTasksTest {

    private HttpTaskServer server;
    private InMemoryTaskManager manager; // конкретный класс
    private Gson gson;

    @BeforeAll
    void init() throws IOException {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new ru.yandex.tracker.HttpServer.LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new ru.yandex.tracker.HttpServer.DurationAdapter())
                .serializeNulls()
                .create();
        server = new HttpTaskServer(manager, gson);
    }

    @BeforeEach
    void setUp() {
        manager.getAllTasks().forEach(t -> manager.deleteTask(t.getId()));
        manager.getAllEpicTasks().forEach(e -> manager.deleteEpicTask(e.getId()));
        manager.getAllSubtasks().forEach(s -> manager.deleteSubtask(s.getId()));
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Task 1", tasks.get(0).getName());
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task t = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getName(), t.getName());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }
}