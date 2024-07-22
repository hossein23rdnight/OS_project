import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class mutex_lock_test
{
    private TCPServer server;

    @Before
    public void setUp() throws IOException {
        server = new TCPServer();
        server.start();
    }

    @After
    public void tearDown() throws IOException {
        server.stop();
    }

    @Test
    public void testFileLocking() throws IOException, InterruptedException, ExecutionException {
        String filename = "testfile_lock.txt";
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String response = in.readLine();
            assertEquals("ACK", response);

            out.println("create " + filename);
            response = in.readLine();
            assertEquals("File created: " + filename, response);
        }

        Callable<String> clientTask1 = () -> {
            try (Socket socket = new Socket("localhost", 8080);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String response = in.readLine();
                assertEquals("ACK", response);

                out.println("edit " + filename);
                StringBuilder result = new StringBuilder();
                while (!(response = in.readLine()).equals("EOF")) {
                    result.append(response).append("\n");
                    if (response.equals("File is currently being edited by another client.")) {
                        return result.toString();
                    }
                }
                out.println("Client 1: Writing to the file.");
                out.println("EOF");
                response = in.readLine();
                result.append(response).append("\n");
                return result.toString();
            }
        };

        Callable<String> clientTask2 = () -> {
            try (Socket socket = new Socket("localhost", 8080);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String response = in.readLine();
                assertEquals("ACK", response);

                out.println("edit " + filename);
                StringBuilder result = new StringBuilder();
                while (!(response = in.readLine()).equals("EOF")) {
                    result.append(response).append("\n");
                    if (response.equals("File is currently being edited by another client.")) {
                        return result.toString();
                    }
                }
                out.println("Client 2: Writing to the file.");
                out.println("EOF");
                response = in.readLine();
                result.append(response).append("\n");
                return result.toString();
            }
        };

        Future<String> future1 = executor.submit(clientTask1);
        Future<String> future2 = executor.submit(clientTask2);

        String result1 = future1.get();
        String result2 = future2.get();

        executor.shutdown();

        assertTrue(result1.contains("File edited: " + filename) || result2.contains("File edited: " + filename));
        assertTrue(result1.contains("File is currently being edited by another client.") || result2.contains("File is currently being edited by another client."));
    }
}
