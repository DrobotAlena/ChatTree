import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alena on 20.11.2016.
 */
public class Node {
    private String name;
    private int myPort;
    private InetAddress IPaddr;
    private int lossPersentage;
    private Node parentNode = null;
    private List<Node> childNodes = new LinkedList<>();

    public Node(InetAddress IPaddr, int myPort) {
        this.myPort = myPort;
        this.IPaddr = IPaddr;
    }

    public Node(String name, int lossPersentage, int myPort) {
        this.name = name;
        this.lossPersentage = lossPersentage;
        this.myPort = myPort;
        try {
            this.IPaddr = InetAddress.getLocalHost();
            System.out.println(IPaddr);
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    public Node(String name, int lossPersentage, int myPort, InetAddress parentIP, int parentPort) {
        this.name = name;
        this.myPort = myPort;
        parentNode = new Node(parentIP, parentPort);
        this.lossPersentage = lossPersentage;
        try {
            this.IPaddr = InetAddress.getLocalHost();
            System.out.println(IPaddr);
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }

    }


    public Node getParentNode() {

        return parentNode;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMyPort() {
        return myPort;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public void setMyPort(int myPort) {
        this.myPort = myPort;
    }

    public InetAddress getIPaddr() {
        return IPaddr;
    }

    public void setIPaddr(InetAddress IPaddr) {
        this.IPaddr = IPaddr;
    }

    public int getLossPersentage() {
        return lossPersentage;
    }

    public void setLossPersentage(int lossPersentage) {
        this.lossPersentage = lossPersentage;
    }

    public List<Node> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<Node> childNodes) {
        this.childNodes = childNodes;
    }
}
