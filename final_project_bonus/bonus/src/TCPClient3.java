import java.io.*;
import java.net.*;

public class TCPClient3 {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String serverResponse = in.readLine();
            System.out.println("Server response: " + serverResponse);

            // Handle further communication with the server if needed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
