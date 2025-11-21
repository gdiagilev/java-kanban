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

class HttpTaskServerTasksTest {

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
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task",
                Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 при создании");

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size(), "Должна быть одна задача в менеджере");
        assertEquals("Test Task", tasks.get(0).getName(), "Имя задачи не совпадает");
    }

    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2", "Desc 2", Status.DONE, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.createTask(task1);
        manager.createTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 при GET");

        Task[] tasksFromServer = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasksFromServer.length, "Должны вернуться две задачи");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.createTask(task);
        int id = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task t = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getName(), t.getName());
    }

    @Test
    void testDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.createTask(task);
        int id = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty(), "Задача должна быть удалена");
    }

    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("T1", "D1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5)));
        manager.createTask(new Task("T2", "D2", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }
}