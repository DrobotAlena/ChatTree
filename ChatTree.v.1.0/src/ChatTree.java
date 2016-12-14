import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Alena on 23.11.2016.
 */
public class ChatTree {
    public static void main(String[] args) {
        if(args.length < 3){
            System.err.println("Error - not enough arguments");
            System.exit(1);
        }
        String name = args[0];
        int lossPersentage = Integer.parseInt(args[1]);
        int myPort = Integer.parseInt(args[2]);


        Node node = null;
        if(args.length != 5){
            node = new Node(name, lossPersentage, myPort);
        }
        else{
            try {
                InetAddress parentIP = InetAddress.getByName(args[3]);
                int parentPort = Integer.parseInt(args[4]);
                node = new Node(name, lossPersentage, myPort, parentIP, parentPort);
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        NodeControl nodeControl = new NodeControl(node);
    }
}
