import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alena on 22.11.2016.
 */
public class MessageMaker {

    public Message createMessage(byte flag, InetAddress addr, int port, String data){
        UUID uuid = UUID.randomUUID();
        byte[] arr = new byte[Constants.UUID_SIZE + Constants.FLAG_SIZE + Constants.DATA_MAX_SIZE];
        System.arraycopy(uuid.toString().getBytes(), 0, arr, 0, Constants.UUID_SIZE);
        arr[Constants.FLAG_POSITION] = flag;
       // System.out.println("set flag =" +flag+" in "+uuid);
        if(data != null) {
            if (data.length() > Constants.DATA_MAX_SIZE) {
                //ошибка, что слишком большое сообщение
            }
            System.arraycopy(data.getBytes(), 0, arr, Constants.FLAG_POSITION + 1, data.length());
        }
        DatagramPacket datagramPacket = new DatagramPacket(arr, arr.length, addr, port);

        Message msg = new Message(uuid, datagramPacket);
        return msg;
    }


    public Message createMessage(Message msg){
        UUID uuid = UUID.randomUUID();
        Message newMsg = new Message(uuid, msg.getDatagramPacket());
        return newMsg;
    }
}
