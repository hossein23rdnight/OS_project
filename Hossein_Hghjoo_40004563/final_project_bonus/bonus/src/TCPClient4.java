import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class TCPClient4 {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private static final Random random = new Random();

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            String serverResponse = in.readLine();
            System.out.println("Server response: " + serverResponse);
            String imeiCode = serverResponse.split(": ")[1];

            new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        sendHealthData(out, imeiCode);
                        Thread.sleep(30000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Interrupted during sending health data.");
                }
            }).start();

            new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        sendLocationData(out, imeiCode);
                        Thread.sleep(45000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Interrupted during sending location data.");
                }
            }).start();

            handleServerCommands(in, out, imeiCode);

            userMenu(scanner, in, out);

        } catch (IOException e) {
            System.out.println("Failed to connect or communicate with the server: " + e.getMessage());
        }
    }

    private static void sendHealthData(PrintWriter out, String imeiCode) {
        int heartRate = 60 + random.nextInt(40);
        int bpLow = 70 + random.nextInt(30);
        int bpHigh = 120 + random.nextInt(40);
        String healthData = String.format("[3G*%s*HEALTH*%d,%d,%d]", imeiCode, heartRate, bpLow, bpHigh);
        out.println(healthData);
    }

    private static void sendLocationData(PrintWriter out, String imeiCode) {
        float lat = -90 + random.nextFloat() * 180;
        float lon = -180 + random.nextFloat() * 360;
        String locationData = String.format("[3G*%s*UD,%f,%f]", imeiCode, lat, lon);
        out.println(locationData);
    }

    private static void handleServerCommands(BufferedReader in, PrintWriter out, String imeiCode) {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("[3G*" + imeiCode + "*POWEROFF]")) {
                        System.out.println("Received power off command from server. Sending DISCONNECT...");
                        out.println("DISCONNECT");
                        System.exit(0);
                    } else if (message.equals("[3G*" + imeiCode + "*FIND]")) {
                        System.out.println("Received FIND command. RINGING...");
                        for (int i = 0; i < 3; i++) {
                            System.out.println("RINGING...");
                            Thread.sleep(1000);
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Error handling server commands: " + e.getMessage());
            }
        }).start();
    }

    private static void userMenu(Scanner scanner, BufferedReader in, PrintWriter out) {
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("Menu:");
            System.out.println("1. Disconnect");
            System.out.println("2. Read all_health_data");
            System.out.println("3. Read all_location_data");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                out.println("DISCONNECT");
                System.out.println("Disconnected from the server.");
                break;
            } else if (choice == 2 || choice == 3) {
                String filename = (choice == 2) ? "all_health_data.log" : "all_location_data.log";
                out.println("read " + filename);
                readServerResponse(in);
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void readServerResponse(BufferedReader in) {
        try {
            String serverResponse;
            while (true) {
                serverResponse = in.readLine();
                if (serverResponse == null || serverResponse.equals("EOF")) {
                    break;
                }
                System.out.println(serverResponse);
            }
        } catch (IOException e) {
            System.out.println("Error reading server response: " + e.getMessage());
        }
    }
}
