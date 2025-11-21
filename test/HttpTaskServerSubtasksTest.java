package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Subtask;
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

class HttpTaskServerSubtasksTest {

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
    void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpicTask(epic);

        Subtask subtask = new Subtask(epic.getId(), "Sub1", "SubDesc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subs = manager.getAllSubtasks();
        assertEquals(1, subs.size());
        assertEquals("Sub1", subs.get(0).getName());
    }

    @Test
    void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpicTask(epic);
        manager.createSubtask(new Subtask(epic.getId(), "S1", "D1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5)));
        manager.createSubtask(new Subtask(epic.getId(), "S2", "D2", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask[] subs = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subs.length);
    }

    @Test
    void testDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.createEpicTask(epic);
        Subtask sub = new Subtask(epic.getId(), "Sub1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.createSubtask(sub);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks?id=" + sub.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}