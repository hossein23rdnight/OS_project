import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TCPServer {
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 2;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                if (((ThreadPoolExecutor) threadPool).getActiveCount() < THREAD_POOL_SIZE) {
                    threadPool.execute(new ClientHandler(socket));
                } else {
                    try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                        out.println("Please try again later.");
                    }
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("ACK");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);

                String[] tokens = message.split(" ", 2);
                String command = tokens[0];
                String argument = tokens.length > 1 ? tokens[1] : "";

                switch (command.toLowerCase()) {
                    case "messaging":
                        out.println(argument);
                        break;
                    case "create":
                        createFile(argument, out);
                        break;
                    case "edit":
                        sendFileContent(argument, out);
                        editFile(argument, in, out);
                        break;
                    case "delete":
                        deleteFile(argument, out);
                        break;
                    case "disconnect":
                        out.println("Goodbye!");
                        return;
                    case "read":
                        sendFileContent(argument, out);
                        break;    
                    default:
                        out.println("Unknown command");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Connection with client closed");
        }
    }

    private void createFile(String filename, PrintWriter out) {
        try {
            File file = new File(filename);
            if (file.createNewFile()) {
                out.println("File created: " + filename);
            } else {
                out.println("File already exists: " + filename);
            }
        } catch (IOException e) {
            out.println("Error creating file: " + e.getMessage());
        }
    }

    private void sendFileContent(String filename, PrintWriter out) {
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    out.println(line);
                }
                out.println("EOF");
            } catch (IOException e) {
                out.println("Error reading file: " + e.getMessage());
            }
        } else {
            out.println("File not found: " + filename);
        }
    out.println("EOF");

    }

    private void editFile(String filename, BufferedReader in, PrintWriter out) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            String line;
            while (!(line = in.readLine()).equals("EOF")) {
                writer.write(line);
                writer.newLine();
            }
            out.println("File edited: " + filename);
        } catch (IOException e) {
            out.println("Error editing file: " + e.getMessage());
        }
    }

    private void deleteFile(String filename, PrintWriter out) {
        File file = new File(filename);
        if (file.delete()) {
            out.println("File deleted: " + filename);
        } else {
            out.println("Failed to delete file: " + filename);
        }
    }
}
