import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient1 {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            String serverResponse = in.readLine();
            System.out.println("Server: " + serverResponse);

            if ("ACK".equals(serverResponse)) {
                while (true) {
                    System.out.println("Menu:");
                    System.out.println("1. Send a message to the server");
                    System.out.println("2. Disconnect");
                    System.out.print("Enter your choice: ");
                    int choice = Integer.parseInt(scanner.nextLine());

                    if (choice == 1) {
                        System.out.print("Enter message: ");
                        String message = scanner.nextLine();
                        out.println(message);
                        System.out.println("Client: " + message);

                        serverResponse = in.readLine();
                        System.out.println("Server: " + serverResponse);
                    } else if (choice == 2) {
                        out.println("disconnect");
                        System.out.println("Client: disconnect");
                        break;
                    } else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
