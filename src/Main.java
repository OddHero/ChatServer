import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by joel on 02.01.2016.
 */
public class Main {

    static final int DEFAULT_PORT = 33456;

    private static ServerSocket serverSocket;

    static void main(String[] args) {

        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = -1;
            }
            if (port <= 0 || port > 65535) {
                System.err.println("Please enter a valid port number!");
                System.exit(1);
            }
        }

        Reference.port = port;
        Reference.terminate = false;

        Thread listener = Thread.ofPlatform().name("listener").start(Main::listen);

        try (Scanner reader = new Scanner(System.in)) {
            while (!Reference.terminate && reader.hasNextLine()) {
                String command = reader.nextLine().trim();
                if (command.equalsIgnoreCase("q")) {
                    shutdown();
                }
            }
        }

        try {
            listener.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void shutdown() {
        System.out.println("Shutting down server...");
        Reference.terminate = true;
        for (User u : Reference.users) {
            u.stopNote("Server closed!");
        }
        Reference.users.clear();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void removeUser(User u) {
        if (Reference.users.remove(u)) {
            System.out.println("User left: " + u.name + " (" + Reference.users.size() + " online)");
        }
    }

    private static void listen() {
        try (ServerSocket socket = new ServerSocket(Reference.port)) {
            serverSocket = socket;
            System.out.println("Server listening on port: " + Reference.port + " (type 'q' to quit)");

            while (!Reference.terminate) {
                try {
                    Socket client = socket.accept();
                    User user = new User(client);
                    Reference.users.add(user);
                    // One lightweight virtual thread per connected client.
                    Thread.ofVirtual().name("user-" + client.getRemoteSocketAddress()).start(user);
                } catch (IOException e) {
                    if (!Reference.terminate) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server was not able to open on port: " + Reference.port
                    + "\nPlease make sure the port is not used by any other Application");
            Reference.terminate = true;
            System.exit(1);
        }
    }
}
