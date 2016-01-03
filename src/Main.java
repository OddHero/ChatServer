import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by joel on 02.01.2016.
 */

public class Main extends Thread{



    Main(){
        Reference.terminate = false;
    }

    public static void main(String[] args) {

        int port = 33456;
        if(args.length>0){
            try {
                port = Integer.parseInt(args[0]);
                if(port<=0){
                    System.err.println("Please enter a valid port number!");
                    System.exit(1);
                }
            }catch(NumberFormatException e){
                System.err.println("Please enter a valid port number!");
            }
        }

        Reference.port = port;

        Main listener = new Main();
        listener.start();

        while(!Reference.terminate){
            Scanner reader = new Scanner(System.in);
            String command = reader.nextLine();

            if (command.equals("q") || command.equals("Q")){
                System.out.println("debug!");
                for (User u: Reference.users){
                    u.stopNote("Server closed!");
                }
                for (User u: Reference.users){
                    Reference.users.remove(u);
                }

                Reference.terminate = true;

            }
        }

    }

    public static void removeUser(User u){
        System.out.println(Reference.users.size());
        Reference.users.remove(u);
        System.out.println(Reference.users.size());
    }

    @Override
    public void run(){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Reference.port);
        } catch (IOException e) {
            System.err.println("Server was not able to open on port: " + Reference.port + "\nPlease make sure the port is not used by any other Application");
        }

        if(serverSocket != null && serverSocket.isBound()){
            System.out.println("Server listening on port: " + Reference.port);

            while(!Reference.terminate){
                Socket client;


                try {
                    client = serverSocket.accept();
                    User user = new User(client);

                    Reference.users.add(user);

                    user.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
