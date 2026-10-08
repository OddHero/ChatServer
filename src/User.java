import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by joel on 02.01.2016.
 */
public class User implements Runnable {

    private volatile boolean _terminate;
    private final Socket userSocket;
    private final PrintWriter out;
    private final BufferedReader in;

    volatile String name;

    User(Socket userSocket) throws IOException {
        this.userSocket = userSocket;
        out = new PrintWriter(userSocket.getOutputStream(), true, StandardCharsets.UTF_8);
        in = new BufferedReader(new InputStreamReader(userSocket.getInputStream(), StandardCharsets.UTF_8));
    }

    public void terminate() {
        _terminate = true;
    }

    private void register(String name) {
        this.name = name;
        out.println("s");
    }

    private void messageOut(String m) {
        int space = m.indexOf(' ');
        if (space < 0) {
            sendError("Invalid message");
            return;
        }
        String partner = m.substring(0, space);
        String message = m.substring(space + 1);

        for (User u : Reference.users) {
            if (partner.equals(u.name)) {
                u.sendMessage("m " + this.name + " " + message);
                return;
            }
        }
        sendError("Unknown user " + partner);
    }

    public void sendMessage(String message) {
        synchronized (out) {
            out.println(message);
        }
    }

    private void userRequest() {
        StringBuilder s = new StringBuilder("t");
        for (User u : Reference.users) {
            if (u.name != null) {
                s.append(' ').append(u.name);
            }
        }
        sendMessage(s.toString());
    }

    private void sendError(String err) {
        sendMessage("e " + err);
    }

    public void killUser() {
        terminate();
        Main.removeUser(this);
    }

    public void stopNote(String m) {
        terminate();
        sendMessage("x " + m);
        close();
    }

    private void close() {
        try {
            userSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!_terminate) {
                String message = in.readLine();
                if (message == null) {
                    break; // client disconnected
                }
                if (message.isEmpty()) {
                    continue;
                }
                String arg = message.length() > 2 ? message.substring(2) : "";
                switch (message.substring(0, 1)) {
                    case "n" -> {
                        if (arg.isBlank()) {
                            sendError("Name must not be empty");
                        } else {
                            register(arg.replace(' ', '_'));
                        }
                    }
                    case "m" -> {
                        if (name == null) {
                            sendError("Register first");
                        } else {
                            messageOut(arg);
                        }
                    }
                    case "t" -> userRequest();
                    case "x" -> killUser();
                    default -> sendError("Unknown Command");
                }
            }
        } catch (IOException e) {
            if (!_terminate) {
                System.err.println("Connection error: " + e.getMessage());
            }
        } finally {
            Main.removeUser(this);
            close();
        }
    }
}
