import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.HttpServer.DurationAdapter;
import ru.yandex.tracker.HttpServer.LocalDateTimeAdapter;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;
import ru.yandex.tracker.Model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTasksTest {
    private TaskManager manager;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        client = HttpClient.newHttpClient();
    }

    @Test
    void testCreateAndGetTask() throws Exception {
        // создаём таску с уникальным временем
        Task task = new Task("Task1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task returned = gson.fromJson(response.body(), Task.class);

        assertEquals(task.getName(), returned.getName());
    }

    @Test
    void testCreateAndGetEpicWithSubtasks() throws Exception {
        Epic epic = new Epic("Epic1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(1));
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask("Sub1", "D1", Status.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask("Sub2", "D2", Status.NEW, epic.getId(),
                sub1.getEndTime().plusMinutes(1), Duration.ofMinutes(15));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        // GET эпика
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics?id=" + epic.getId()))
                .GET()
                .build();
        HttpResponse<String> epicResp = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        Epic returnedEpic = gson.fromJson(epicResp.body(), Epic.class);

        assertEquals(epic.getName(), returnedEpic.getName());

        // GET подзадач эпика
        HttpRequest subRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/subtasks?id=" + epic.getId()))
                .GET()
                .build();
        HttpResponse<String> subResp = client.send(subRequest, HttpResponse.BodyHandlers.ofString());
        Subtask[] returnedSubs = gson.fromJson(subResp.body(), Subtask[].class);

        assertEquals(2, returnedSubs.length);
        assertEquals("Sub1", returnedSubs[0].getName());
        assertEquals("Sub2", returnedSubs[1].getName());
    }

    @Test
    void testPrioritizedTasksOrder() {
        LocalDateTime now = LocalDateTime.now();

        Task t1 = new Task("T1", "D1", Status.NEW, now, Duration.ofMinutes(30));
        Task t2 = new Task("T2", "D2", Status.NEW, t1.getEndTime().plusMinutes(1), Duration.ofMinutes(45));
        Subtask s1 = new Subtask("S1", "D3", Status.NEW, 0, t2.getEndTime().plusMinutes(1), Duration.ofMinutes(15));

        manager.createTask(t1);
        manager.createTask(t2);
        manager.createSubtask(s1);

        // Проверяем, что порядок приоритетных задач корректный
        assertEquals(t1.getId(), manager.getPrioritizedTasks().get(0).getId());
        assertEquals(t2.getId(), manager.getPrioritizedTasks().get(1).getId());
        assertEquals(s1.getId(), manager.getPrioritizedTasks().get(2).getId());
    }
}