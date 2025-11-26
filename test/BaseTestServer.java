import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.tracker.HttpServer.HttpTaskServer;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;

public class BaseTestServer {
    protected TaskManager manager;
    protected HttpTaskServer server;
    protected Gson gson;

    @BeforeEach
    void startServer() throws IOException {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder().setPrettyPrinting().create();
        server = new HttpTaskServer(manager, gson);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }
}