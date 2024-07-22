import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

public class creating_command_test {

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
    public void testCreateFileCommand() throws IOException {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String response = in.readLine();
            assertEquals("ACK", response);

            String filename = "testfile.txt";
            out.println("create " + filename);

            response = in.readLine();
            assertEquals("File created: " + filename, response);

            File testFile = new File(filename);
            if (testFile.exists()) {
                testFile.delete();
            }
        }
    }
}
