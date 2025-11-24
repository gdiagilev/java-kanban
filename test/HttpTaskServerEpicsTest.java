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

public class HttpTaskServerEpicsTest extends BaseHttpTest {

    @Test
    void testCreateEpic() throws Exception {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        String json = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        assertEquals(1, manager.getAllEpicTasks().size());
    }

    @Test
    void testGetSubtasksOfEpic() throws Exception {
        Epic epic = new Epic("Epic 1", "Desc");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc2", Status.NEW,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/subtasks?id=" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }
}