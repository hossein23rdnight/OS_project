import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class messaging_command_test {

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
    public void testMessagingCommand() throws IOException {
        String randomMessage = generateRandomSentence();


        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String response = in.readLine();
            assertEquals("ACK", response);

            out.println("messaging " + randomMessage);

            response = in.readLine();
            assertEquals(randomMessage, response);
        }
    }

    private String generateRandomSentence() {
        String[] words = {"apple", "banana", "orange", "grape", "kiwi", "pear", "melon", "berry", "peach"};
        Random random = new Random();
        int sentenceLength = random.nextInt(5) + 5; 
        StringBuilder sentence = new StringBuilder();

        for (int i = 0; i < sentenceLength; i++) {
            sentence.append(words[random.nextInt(words.length)]).append(" ");
        }

        sentence.deleteCharAt(sentence.length() - 1);

        return sentence.toString();
    }
}
