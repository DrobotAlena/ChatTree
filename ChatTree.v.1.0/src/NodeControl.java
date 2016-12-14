import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by Alena on 22.11.2016.
 */
public class NodeControl {
    private Node node;
    private Sender sender;
    private Resver resver;
    Thread resverThread;
    private MessageMaker messageMaker;
    private UserReader userReader;
    private DatagramSocket datagramSocket;

    public NodeControl(Node node){
        this.node = node;
        try {
            datagramSocket = new DatagramSocket(node.getMyPort(), node.getIPaddr());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        messageMaker = new MessageMaker();
        sender = new Sender(datagramSocket, messageMaker);
        resver = new Resver(datagramSocket, this);
        resverThread = new Thread(resver, "resverThread");
        resverThread.start();
        userReader = new UserReader(this);
        Thread userThread = new Thread(userReader, "userThread");
        userThread.start();

        if(node.getParentNode() != null){ //node isn't a root
    //        System.out.println(getNodeName()+" send parent request ");
            Message msg = messageMaker.createMessage(Constants.HAVE_NEW_CHILD, node.getParentNode().getIPaddr(), node.getParentNode().getMyPort(), null);
            sender.send(msg);
        }
        try {
            Runtime.getRuntime().addShutdownHook(new Closer(this));
            resverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<Node> getNodes(){
        List<Node> list = new LinkedList<>();
        if(node.getParentNode() != null) {
            list.add(node.getParentNode());
        }
        if(node.getChildNodes() != null) {
            list.addAll(node.getChildNodes());
        }
        return list;
    }

    public void sendUserText(String data){
        for (Node node1 :getNodes()) {
            Message msg = messageMaker.createMessage(Constants.TEXT_MSG, node1.getIPaddr(), node1.getMyPort(), data);
             sender.send(msg);
        }
        //создать много сообщений для каждой нужной ноды
    }

    public void nodeDead(byte[] arr, InetAddress senderIP, int senderPort){
        String s = new String(arr);
        String[] dataParse = s.split(" ");
        for (int i = 0; i <dataParse.length ; i++) {
            System.out.println(dataParse[i]);
        }
        int flag = Integer.parseInt(dataParse[0]);
        try {
            InetAddress addr = InetAddress.getByAddress(dataParse[1].getBytes());
            int port = Integer.parseInt(dataParse[2]);
                if (flag == Constants.HAVE_NEW_CHILD) {
                    if(node.getParentNode() != null) {
                        if (senderIP.equals(node.getParentNode().getIPaddr()) && senderPort == node.getParentNode().getMyPort()) {
                            System.out.println("now have no parent");
                            node.setParentNode(null);
                        }
                    }
                    Iterator<Node> iterator = node.getChildNodes().iterator();
                    while(iterator.hasNext()){
                        Node node = iterator.next();
                        if(node.getIPaddr().equals(senderIP) && node.getMyPort() == senderPort){
                            iterator.remove();
                        }
                    }
                    if(!(addr.equals(senderIP)) || !(port == senderPort)) {
                        addChild(addr, port);
                    }
                }
                else {
                    if(!(addr.equals(senderIP)) || !(port == senderPort)) {
                        newParent(addr, port);
                    }
                    else{
                        node.setParentNode(null);
                    }
                    }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void handleText(byte[] textArr){
        System.out.println(node.getName()+": " + new String(textArr));
    }

    public Node getNode() {
        return node;
    }

    public void sendToOther(byte[] data, InetAddress senderAddr, int senderPort){
        String text = new String(data);
        for (Node node1 :getNodes()) {
            if(!node1.getIPaddr().equals(senderAddr) || node1.getMyPort() != senderPort) {
                Message msg = messageMaker.createMessage(Constants.TEXT_MSG, node1.getIPaddr(), node1.getMyPort(), text);
                sender.send(msg);
            }
        }

    }

    public void goToDead(){
        resver.startClosing();
 //       System.out.println("resver in new role");
        while(!sender.endWork()){
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }
    public void addChild(InetAddress childIP, int childPort){
        System.out.println(getNodeName() + " Have new child " + childIP + " " + childPort);
        Node newChildNode = new Node(childIP, childPort);
        node.getChildNodes().add(newChildNode);
    }

    public void newParent(InetAddress parentIP, int parentPort){
        System.out.println(getNodeName() + " Have new parent " + parentIP + " " + parentPort);
        Node parentNode = new Node(parentIP, parentPort);
        node.setParentNode(parentNode);

    }

    public String getNodeName(){
        return node.getName();
    }

    public Sender getSender() {
        return sender;
    }


    public int getLossPersentage(){
        return node.getLossPersentage();
    }

    public MessageMaker getMessageMaker() {
        return messageMaker;
    }
}
