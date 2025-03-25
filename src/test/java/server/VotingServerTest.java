package server;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VotingServerTest {
    @Test
    void testServerLifecycle() {

        new Thread(() -> {
            try {
                VotingServer.startTcpServer();
            } catch (InterruptedException ignored) {}
        }).start();

        assertTrue(new File("server_state.json").exists());
    }
}