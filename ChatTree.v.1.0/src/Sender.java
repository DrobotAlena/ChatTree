import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * Created by Alena on 22.11.2016.
 */
public class Sender {
    private DatagramSocket datagramSocket;
    List<Message> messageList;
    Thread checkerThread;
    ConfirmationHandler confirmationHandler;
    MessageMaker messagoMaker;
    static final Object mutex = new Object();


    public Sender(DatagramSocket datagramSocket, MessageMaker messageMaker){
        this.datagramSocket = datagramSocket;
        this.messagoMaker = messageMaker;
        messageList = new LinkedList<Message>();
        Checker checker = new Checker();
        checkerThread = new Thread(checker, "checkerThread");
        checkerThread.start();
        confirmationHandler = new ConfirmationHandler();
    }

    public void send(Message msg){
        try{
            datagramSocket.send(msg.getDatagramPacket());
     //       System.out.println("send to "+msg.getDatagramPacket().getAddress()+" " + msg.getDatagramPacket().getPort()+" "+ msg.getUuid());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(msg.getFlag() != Constants.CONFIRMATION) {
            synchronized (mutex) {
                messageList.add(msg);
            }
        }

        if(messageList.size() > Constants.MAX_LIST_SIZE){
            for (int i = 0; i <messageList.size() ; i++) {
                if(messageList.get(i).getFlag() == Constants.TEXT_MSG){
                    messageList.remove(i);
                    return;
                }
            }

            //если не нашли текстовое сообщение
            messageList.remove(0);

        }
    }

    public boolean endWork(){
        return messageList.isEmpty();
    }

    public ConfirmationHandler getConfirmationHandler() {
        return confirmationHandler;
    }

    public class ConfirmationHandler{
        public void confirm(UUID uuid, InetAddress addr, int port){
//            System.out.println("ConfirmHandler starts working");
            synchronized (mutex) {
                Iterator<Message> it = messageList.iterator();
                while(it.hasNext()) {
                    Message message = it.next();
                    if(message.getUuid().compareTo(uuid) == 0){
                        it.remove();
                        break;
                    }
                }
            }
        }
    }


    private class Checker implements Runnable{
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Date currentDate = new Date();
                    List<Message> newMessageList = new LinkedList<>();
                    Message message = null;
                    synchronized (mutex) {
                        Iterator<Message> it = messageList.iterator();
                        while(it.hasNext()) {
                            message = it.next();
                            if (currentDate.getTime() - message.getWhenWasSend().getTime() > Constants.TIME_BEFORE_RESEND) {
           //                     System.out.println("find old msg " + message.getUuid());
                                newMessageList.add(message);
                                it.remove();
                            }
                        }
                        for (Message message1 :newMessageList) {
                            send(message1);
                        }

                    }
                    Thread.sleep(Constants.TIME_BEFORE_CHECK);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
