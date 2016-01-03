import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by joel on 02.01.2016.
 */
public class User extends Thread {

    private boolean _terminate;
    private Socket userSocket;
    PrintWriter out;
    BufferedReader in;

    String name;


    User(Socket userSocket) throws IOException {

        _terminate = false;
        this.userSocket = userSocket;
        out = new PrintWriter(userSocket.getOutputStream(),true);
        in = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
    }

    public void terminate(){
        _terminate = true;
    }

    private void register(String name){
        this.name = name;
        out.println("s");
        out.flush();
    }

    private void messageOut(String m) {
        String partner = m.substring(0,m.indexOf(' '));
        String messsage = m.substring(m.indexOf(' ')+1);

        for (User u:Reference.users) {
            if (u.name.equals(partner)){
                u.sendMessage("m " + this.name + " " + messsage);
                return;
            }
        }


    }

    public void sendMessage(String message){
        out.println(message);
        out.flush();
    }


    private void userRequest() {
        String s = "t";
        for (User u:Reference.users) {
            s = s + " " + u.name;
        }
        out.println(s);
        out.flush();
    }

    private void sendError(String err){
        out.println("e " + err);
        out.flush();
    }

    public void killUser(){
        Main.removeUser(this);
    }

    public void stopNote(String m){
        out.println("x " + m);
        try {
            this.userSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void run(){
         String message;
        while (! _terminate){
            try {
                message=in.readLine();
                switch (message.substring(0,1)) {
                    case "n" : register(message.substring(2).replace(' ', '_')); break;
                    case "m" : messageOut(message.substring(2)); break;
                    case "t" : userRequest(); break;
                    case "x" : killUser(); break;


                    default: sendError("Unknown Command"); break;

                }
            } catch (IOException e) {
                System.err.println("Trouble with your messege");

            }


        }

        Main.removeUser(this);
    }

}
