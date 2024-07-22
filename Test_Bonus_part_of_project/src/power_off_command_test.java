import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public class power_off_command_test {

    private TCPServer2 server;
    private ExecutorService executor;

    @Before
    public void setUp() throws IOException {
        server = new TCPServer2();
        executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            TCPServer2.main(new String[]{});
        });
    }

    @After
    public void tearDown() throws IOException {
        executor.shutdownNow();
    }

    @Test
    public void testPowerOffCommand() throws IOException, InterruptedException, ExecutionException {
        Future<Boolean> clientFuture = executor.submit(() -> {
            try (Socket socket = new Socket("localhost", 8080);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String serverResponse = in.readLine();
                System.out.println("Server response: " + serverResponse);
                String imeiCode = serverResponse.split(": ")[1];

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("[3G*" + imeiCode + "*POWEROFF]")) {
                        System.out.println("Received power off command from server. Sending DISCONNECT...");
                        out.println("DISCONNECT");
                        return true;
                    }
                }
            } catch (IOException e) {
                System.out.println("Client encountered an error: " + e.getMessage());
                return false;
            }
            return false;
        });

        TimeUnit.SECONDS.sleep(1);

        server.sendCommandDirectly("IMEI-1000000", "*POWEROFF");

        assertTrue(clientFuture.get());
    }
}
