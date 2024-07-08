import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient1 {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;
    
    // ANSI escape code for red text
    private static final String RED_TEXT = "\033[0;31m";
    private static final String RESET_TEXT = "\033[0m";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            String serverResponse = in.readLine();
            System.out.println("Server: " + RED_TEXT + serverResponse + RESET_TEXT);

            if ("ACK".equals(serverResponse)) {
                while (true) {
                    System.out.println("Menu:");
                    System.out.println("1. Send a message to the server");
                    System.out.println("2. Disconnect");
                    System.out.println("3. Create a file");
                    System.out.println("4. Edit a file");
                    System.out.println("5. Delete a file");
                    System.out.println("6. Read a file");
                    System.out.print("Enter your choice: ");
                    int choice = Integer.parseInt(scanner.nextLine());

                    switch (choice) {
                        case 1:
                            System.out.print("Enter message: ");
                            String message = scanner.nextLine();
                            out.println("messaging " + message);
                            System.out.println("Client: " + message);
                            serverResponse = in.readLine();
                            System.out.println("Server: " + RED_TEXT + serverResponse + RESET_TEXT);
                            break;
                        case 2:
                            out.println("disconnect");
                            System.out.println("Client: disconnect");
                            return;
                        case 3:
                            System.out.print("Enter filename to create: ");
                            String createFilename = scanner.nextLine();
                            out.println("create " + createFilename);
                            serverResponse = in.readLine();
                            System.out.println("Server: " + RED_TEXT + serverResponse + RESET_TEXT);
                            break;
                        case 4:
                            System.out.print("Enter filename to edit: ");
                            String editFilename = scanner.nextLine();
                            out.println("edit " + editFilename);
                            while (!(serverResponse = in.readLine()).equals("EOF")) {
                                System.out.println(RED_TEXT + serverResponse + RESET_TEXT);
                                if (serverResponse.equals("File is currently being edited by another client.")) {
                                    // Return to menu if the file is being edited by another client
                                    break;
                                }
                            }
                            if (!serverResponse.equals("File is currently being edited by another client.")) {
                                System.out.println("Enter the new content for the file (end with EOF):");
                                StringBuilder newContent = new StringBuilder();
                                while (scanner.hasNextLine()) {
                                    String line = scanner.nextLine();
                                    if (line.equals("EOF")) break;
                                    newContent.append(line).append("\n");
                                }
                                out.print(newContent.toString());
                                out.println("EOF");
                                serverResponse = in.readLine();
                                System.out.println("Server: " + RED_TEXT + serverResponse + RESET_TEXT);
                            }
                            break;
                        case 5:
                            System.out.print("Enter filename to delete: ");
                            String deleteFilename = scanner.nextLine();
                            out.println("delete " + deleteFilename);
                            serverResponse = in.readLine();
                            System.out.println("Server: " + RED_TEXT + serverResponse + RESET_TEXT);
                            break;
                        case 6:
                            System.out.print("Enter filename to read: ");
                            String readFilename = scanner.nextLine();
                            out.println("read " + readFilename);
                            while (!(serverResponse = in.readLine()).equals("EOF")) {
                                System.out.println(RED_TEXT + serverResponse + RESET_TEXT);
                            }
                            break;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
