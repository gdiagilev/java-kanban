import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicsHttpTest extends BaseTestServer {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void testCreateAndGetEpic() throws Exception {
        Epic epic = new Epic("Epic1", "Desc");
        String json = gson.toJson(epic);

        HttpRequest post = HttpRequest.newBuilder(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> postResp = client.send(post, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResp.statusCode());

        HttpRequest get = HttpRequest.newBuilder(URI.create("http://localhost:8080/epics")).GET().build();
        HttpResponse<String> getResp = client.send(get, HttpResponse.BodyHandlers.ofString());
        Epic[] epics = gson.fromJson(getResp.body(), Epic[].class);
        assertEquals(1, epics.length);
        assertEquals("Epic1", epics[0].getName());
    }
}