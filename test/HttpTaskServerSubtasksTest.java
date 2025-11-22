package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;
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
public class HttpTaskServerSubtasksTest {

    private HttpTaskServer server;
    private InMemoryTaskManager manager; // теперь конкретный класс
    private Gson gson;

    @BeforeAll
    void init() throws IOException {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new ru.yandex.tracker.HttpServer.LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new ru.yandex.tracker.HttpServer.DurationAdapter())
                .serializeNulls()
                .create();
        server = new HttpTaskServer(manager, gson); // конструктор HttpTaskServer с менеджером и Gson
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
    void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        manager.createEpicTask(epic);

        Subtask subtask = new Subtask(
                epic.getId(),
                "Subtask 1",
                "Desc Subtask",
                Status.NEW,
                LocalDateTime.now().plusMinutes(10),
                Duration.ofMinutes(30)
        );

        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("Subtask 1", subtasks.get(0).getName());
        assertEquals(epic.getId(), subtasks.get(0).getEpicId());
    }

    @Test
    void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc1", Status.NEW,
                LocalDateTime.now().plusMinutes(10), Duration.ofMinutes(20));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc2", Status.NEW,
                LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(20));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        manager.createEpicTask(epic);

        Subtask subtask = new Subtask(epic.getId(), "Subtask 1", "Desc", Status.NEW,
                LocalDateTime.now().plusMinutes(10), Duration.ofMinutes(30));
        manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks?id=" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}