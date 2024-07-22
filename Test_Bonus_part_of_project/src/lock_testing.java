import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public class lock_testing
{
    private TCPServer2 server;
    private ExecutorService executor;

    @Before
    public void setUp() throws IOException {
        server = new TCPServer2();
        executor = Executors.newFixedThreadPool(5);
        executor.submit(() -> {
            TCPServer2.main(new String[]{});
        });

        Files.createFile(Paths.get("all_health_data.log"));
        Files.createFile(Paths.get("all_location_data.log"));
    }

    @After
    public void tearDown() throws IOException {
        executor.shutdownNow();
        Files.deleteIfExists(Paths.get("all_health_data.log"));
        Files.deleteIfExists(Paths.get("all_location_data.log"));
    }

    @Test
    public void testLogDataLocking() throws IOException, InterruptedException, ExecutionException {
        Callable<Boolean> clientTask = () -> {
            try (Socket socket = new Socket("localhost", 8080);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String serverResponse = in.readLine();
                if (serverResponse == null) {
                    System.out.println("Client did not receive any response from server");
                    return false;
                }
                System.out.println("Server response: " + serverResponse);
                String imeiCode = serverResponse.split(": ")[1];

                for (int i = 0; i < 10; i++) {
                    String healthData = "[3G*" + imeiCode + "*HEALTH*" + i + "]";
                    out.println(healthData);
                    System.out.println("Sent: " + healthData);
                    Thread.sleep(100); 
                }
                return true;
            } catch (IOException | InterruptedException e) {
                System.out.println("Client encountered an error: " + e.getMessage());
                return false;
            }
        };

        Future<Boolean> clientFuture1 = executor.submit(clientTask);
        Future<Boolean> clientFuture2 = executor.submit(clientTask);
        Future<Boolean> clientFuture3 = executor.submit(clientTask);
        Future<Boolean> clientFuture4 = executor.submit(clientTask);
        Future<Boolean> clientFuture5 = executor.submit(clientTask);

        assertTrue(clientFuture1.get());
        assertTrue(clientFuture2.get());
        assertTrue(clientFuture3.get());
        assertTrue(clientFuture4.get());
        assertTrue(clientFuture5.get());

        TimeUnit.SECONDS.sleep(1);

        try (BufferedReader fileReader = new BufferedReader(new FileReader("all_health_data.log"))) {
            String line;
            int logCount = 0;
            while ((line = fileReader.readLine()) != null) {
                logCount++;
                System.out.println("Log entry: " + line);
            }
            assertTrue("Expected log count does not match", logCount == 50); 
        }
    }
}
