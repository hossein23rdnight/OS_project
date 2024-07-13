import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPServer {
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 5;
    private static final AtomicInteger imeiCounter = new AtomicInteger(1000000);
    private static final Map<String, Socket> clientMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        startServerMenu();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startServerMenu() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Server Menu:");
                System.out.println("1. Send power off command");
                System.out.println("2. Send find command");
                System.out.println("3. Send custom command");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1 || choice == 2) {
                    System.out.print("Enter IMEI: ");
                    String imei = scanner.nextLine();
                    String command = (choice == 1) ? "*POWEROFF" : "*FIND";
                    sendCommandToClient(imei, command);
                } else if (choice == 3) {
                    System.out.print("Enter IMEI: ");
                    String imei = scanner.nextLine();
                    System.out.print("Enter custom command: ");
                    String command = scanner.nextLine();
                    sendCommandToClient(imei, "*" + command);
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            }
        }).start();
    }

    private static void sendCommandToClient(String imei, String command) {
        Socket socket = clientMap.get(imei);
        if (socket != null && !socket.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("[3G*" + imei + command + "]");
                System.out.println(command + " command sent to IMEI: " + imei);
            } catch (IOException e) {
                System.out.println("Failed to send command to client: " + e.getMessage());
            }
        } else {
            System.out.println("No client connected with IMEI: " + imei);
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String imeiCode;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                imeiCode = generateIMEICode();
                clientMap.put(imeiCode, socket);
                out.println("ACK: " + imeiCode);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("[3G*") && message.contains("*HEALTH*")) {
                        logData(imeiCode + "_health_log.txt", message);
                    } else if (message.startsWith("[3G*") && message.contains("*UD,")) {
                        logData(imeiCode + "_location_log.txt", message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client " + imeiCode + ": " + e.getMessage());
            } finally {
                clientMap.remove(imeiCode);
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket for client " + imeiCode + ": " + e.getMessage());
                }
            }
        }

        private void logData(String filename, String data) {
            try (PrintWriter log = new PrintWriter(new FileOutputStream(new File(filename), true))) {
                log.println(data);
            } catch (FileNotFoundException e) {
                System.out.println("Error logging data: " + e.getMessage());
            }
        }

        private String generateIMEICode() {
            return "IMEI-" + imeiCounter.getAndIncrement();
        }
    }
}
