package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private HttpTaskServer server;
    private TaskManager manager;
    private Gson gson;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/tasks";

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder().serializeNulls().create();

        server = new HttpTaskServer(8080, manager, gson);
        server.start();

        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testCreateGetAndDeleteTask() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Test Task", "Test Description", Status.NEW);
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(LocalDateTime.now());
        String jsonTask = gson.toJson(task);

        // POST /tasks
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Создание задачи должно вернуть 201");

        // GET /tasks?id=1
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "?id=1"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(), "Получение задачи должно вернуть 200");

        Task returnedTask = gson.fromJson(getResponse.body(), Task.class);
        assertNotNull(returnedTask, "Возвращенная задача не должна быть null");
        assertEquals("Test Task", returnedTask.getName(), "Имя задачи не совпадает");

        // DELETE /tasks
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Удаление всех задач должно вернуть 200");

        List<Task> tasksAfterDelete = manager.getAllTasks();
        assertTrue(tasksAfterDelete.isEmpty(), "Список задач должен быть пуст после удаления");
    }
}