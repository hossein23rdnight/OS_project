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
                out.println(message);
                if (message.equalsIgnoreCase("disconnect")) {
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
}
