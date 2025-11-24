import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerSubtasksTest extends BaseHttpTest {

    @Test
    void testCreateSubtask() throws Exception {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        manager.createEpicTask(epic);

        Subtask subtask = new Subtask(epic.getId(), "Subtask 1", "Sub Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        assertEquals(1, manager.getSubtasksOfEpic(epic.getId()).size());
        assertEquals("Subtask 1", manager.getSubtasksOfEpic(epic.getId()).get(0).getName());
    }

    @Test
    void testGetSubtaskById() throws Exception {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        manager.createEpicTask(epic);

        Subtask subtask = new Subtask(epic.getId(), "Subtask 1", "Sub Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks?id=" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask returned = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getName(), returned.getName());
    }

    @Test
    void testGetAllSubtasksOfEpic() throws Exception {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc2", Status.NEW,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/epic?id=" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    @Test
    void testDeleteSubtaskById() throws Exception {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        manager.createEpicTask(epic);

        Subtask subtask = new Subtask(epic.getId(), "Subtask 1", "Sub Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks?id=" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(manager.getSubtasksOfEpic(epic.getId()).isEmpty());
    }
}