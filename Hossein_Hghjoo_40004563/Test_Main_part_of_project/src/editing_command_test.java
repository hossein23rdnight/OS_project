import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class editing_command_test{
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
    public void testCreateAndEditFileCommand() throws IOException {
        String filename = "testfile.txt";
        String fileContent = "Hello, World!";
        
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String response = in.readLine();
            assertEquals("ACK", response);

            out.println("create " + filename);
            response = in.readLine();
            assertEquals("File created: " + filename, response);

            out.println("edit " + filename);
            while (!(response = in.readLine()).equals("EOF")) {
                if (response.equals("File is currently being edited by another client.")) {
                    break;
                }
            }
            if (!response.equals("File is currently being edited by another client.")) {
                out.println(fileContent);
                out.println("EOF");
                response = in.readLine();
                assertEquals("File edited: " + filename, response);
            }

            File testFile = new File(filename);
            assertTrue(testFile.exists());

            try (BufferedReader fileReader = new BufferedReader(new FileReader(testFile))) {
                String line = fileReader.readLine();
                assertEquals(fileContent, line);
            }

            if (testFile.exists()) {
                testFile.delete();
            }
        }
    }
}
