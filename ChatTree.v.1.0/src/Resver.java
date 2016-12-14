import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * Created by Alena on 22.11.2016.
 */
public class Resver implements Runnable {
    List<UUID> resvMsgUUID;
    private DatagramSocket datagramSocket;
    private NodeControl nodeControl;
    private Random random = new Random();
    private int lossPersentage;
    boolean closeFlag = false;

    public Resver(DatagramSocket datagramSocket, NodeControl nodeControl){
        this.datagramSocket = datagramSocket;
        this.nodeControl = nodeControl;
        resvMsgUUID = new LinkedList<>();
        lossPersentage = nodeControl.getLossPersentage();
    }
    @Override
    public void run() {
        int i;
        byte flag = 0;
        while(!closeFlag){
            try {
                byte[] arr = new byte[Constants.UUID_SIZE + Constants.FLAG_SIZE + Constants.DATA_MAX_SIZE];
                DatagramPacket resvPacket = new DatagramPacket(arr, arr.length);
     //           System.out.println("ready to recieve");
                datagramSocket.receive(resvPacket);
           //     System.out.println("have new packet");
                i = random.nextInt(99);
                if(i > lossPersentage) {
                    int senderPort = resvPacket.getPort();
                    InetAddress senderIP = resvPacket.getAddress();
                    byte[] uuidArr = new byte[Constants.UUID_SIZE];
                    System.arraycopy(arr, 0, uuidArr, 0, Constants.UUID_SIZE);
                    String uuidString = new String(uuidArr);
                    UUID uuid = UUID.fromString(uuidString);
     //               System.out.println("UUID = "+ uuid + ", flag = "+arr[Constants.FLAG_POSITION]);
                    flag = 0;
                    for (UUID uuidWasResv : resvMsgUUID) {
                        if(uuidWasResv.compareTo(uuid) == 0) {
                            flag = 1;
                            break;
                        }
                    }

                    if(flag == 0) {
                        resvMsgUUID.add(uuid);
                        if(resvMsgUUID.size() > Constants.MAX_RECV_LIST_SIZE){
                            for (int j = 0; j <Constants.HOW_MANY_DEL; j++) {
                                resvMsgUUID.remove(0);
                            }
                        }
                        //parsing data and decide that msg is it
                        if (arr[Constants.FLAG_POSITION] != Constants.CONFIRMATION) {
                            switch (arr[Constants.FLAG_POSITION]) {
                                case Constants.TEXT_MSG: {
                                    byte[] textArr = new byte[Constants.DATA_MAX_SIZE];
                                    System.arraycopy(arr, Constants.FLAG_POSITION+1, textArr, 0, Constants.DATA_MAX_SIZE);
                                    nodeControl.handleText(textArr);
                                    nodeControl.sendToOther(textArr, senderIP, senderPort);
                                    break;
                                }
                                case Constants.HAVE_NEW_CHILD: {
                                    nodeControl.addChild(senderIP, senderPort);
                                    break;
                                }
                                case Constants.HAVE_NEW_PARENT: {
                                    nodeControl.newParent(senderIP, senderPort);
                                    break;
                                }
                                case Constants.NODE_DEAD: {
       //                             System.out.println("Node dead " + senderIP+" "+ senderPort);
                                    byte[] textArr = new byte[Constants.DATA_MAX_SIZE];
                                    System.arraycopy(arr, Constants.FLAG_POSITION+1, textArr, 0, Constants.DATA_MAX_SIZE);
                                    nodeControl.nodeDead(textArr, senderIP, senderPort);
                                    break;
                                }
                            }
                            //послать подтверждение
                            Message msg = nodeControl.getMessageMaker().createMessage(Constants.CONFIRMATION, senderIP, senderPort, uuidString);
         //                   System.out.println("send confirmation "+ msg.getUuid());
                            nodeControl.getSender().send(msg);
                        } else {
                            byte[] uuidConfirmArr = new byte[Constants.UUID_SIZE];
                            System.arraycopy(arr, Constants.FLAG_POSITION+1, uuidConfirmArr, 0, Constants.UUID_SIZE);
                            String uuidConfirmString = new String(uuidConfirmArr);
           //                 System.out.println("Confirmation uuid = " + uuidConfirmString);
                            UUID uuidConfirm = UUID.fromString(uuidConfirmString);
                            nodeControl.getSender().getConfirmationHandler().confirm(uuidConfirm, senderIP, senderPort);
                        }
                    }
                    else{
             //           System.out.println("send confirmation one more time "+ uuidString);
                        Message msg = nodeControl.getMessageMaker().createMessage(Constants.CONFIRMATION, senderIP, senderPort, uuidString);
                        nodeControl.getSender().send(msg);
                    }
                }
                else{
        //            System.out.println("packet lost");
                }
            }
            catch (SocketException e){
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        while(true){
            try {
                byte[] arr = new byte[Constants.UUID_SIZE + Constants.FLAG_SIZE + Constants.DATA_MAX_SIZE];
                DatagramPacket resvPacket = new DatagramPacket(arr, arr.length);
                datagramSocket.receive(resvPacket);
                int senderPort = resvPacket.getPort();
                InetAddress senderIP = resvPacket.getAddress();
                byte[] uuidArr = new byte[Constants.UUID_SIZE];
                System.arraycopy(arr, 0, uuidArr, 0, Constants.UUID_SIZE);
                String uuidString = new String(uuidArr);
                UUID uuid = UUID.fromString(uuidString);
                i = random.nextInt(99);
                if (i > lossPersentage) {
               //     System.out.println("have new packet");
                    if(arr[Constants.FLAG_POSITION] == Constants.CONFIRMATION){
     //                   System.out.println("it was confirmation");
                        byte[] uuidConfirmArr = new byte[Constants.UUID_SIZE];
                        System.arraycopy(arr, Constants.FLAG_POSITION+1, uuidConfirmArr, 0, Constants.UUID_SIZE);
                        String uuidConfirmString = new String(uuidConfirmArr);
       //                 System.out.println("Confirmation uuid = " + uuidConfirmString);
                        UUID uuidConfirm = UUID.fromString(uuidConfirmString);
                        nodeControl.getSender().getConfirmationHandler().confirm(uuidConfirm, senderIP, senderPort);
                    }
                    else{
         //               System.out.println("it was not confirmation");
                        Message msg = nodeControl.getMessageMaker().createMessage(Constants.CONFIRMATION, senderIP, senderPort, uuidString);
                        nodeControl.getSender().send(msg);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startClosing(){
     closeFlag = true;
    }

}
