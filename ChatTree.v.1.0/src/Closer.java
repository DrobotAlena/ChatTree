import java.util.List;

/**
 * Created by Alena on 01.12.2016.
 */
public class Closer extends Thread {
    NodeControl nodeControl;

    public Closer(NodeControl nodeControl){
        this.nodeControl = nodeControl;
    }
    @Override
    public void run() {
        System.out.println("\n go to dead \n");
            if (nodeControl.getNode().getParentNode() != null) { //node not a root
                //System.out.println(nodeControl.getNode().getChildNodes().size());
                if(nodeControl.getNode().getChildNodes().size() != 0) {
                    String parentIP = new String(nodeControl.getNode().getParentNode().getIPaddr().getAddress());
                    String parentAddrPort = Constants.HAVE_NEW_PARENT + " "+ parentIP + " " + nodeControl.getNode().getParentNode().getMyPort()+" ";
                    for (Node node : nodeControl.getNode().getChildNodes()) {
                        //посылаем детям родителя
                        Message msg = nodeControl.getMessageMaker().createMessage(Constants.NODE_DEAD, node.getIPaddr(), node.getMyPort(), parentAddrPort);
                        nodeControl.getSender().send(msg);
                        //послать родителю детей
                        String childAddrPort = Constants.HAVE_NEW_CHILD + " " + new String(node.getIPaddr().getAddress()) + " " + node.getMyPort() + " ";
                        msg = nodeControl.getMessageMaker().createMessage(Constants.NODE_DEAD, nodeControl.getNode().getParentNode().getIPaddr(),
                                nodeControl.getNode().getParentNode().getMyPort(), childAddrPort);
                        nodeControl.getSender().send(msg);
                    }
                }
                else{
                    String childAddrPort = Constants.HAVE_NEW_CHILD + " " + new String(nodeControl.getNode().getIPaddr().getAddress()) + " " +
                            nodeControl.getNode().getMyPort() + " ";
                    Message msg = nodeControl.getMessageMaker().createMessage(Constants.NODE_DEAD, nodeControl.getNode().getParentNode().getIPaddr(),
                            nodeControl.getNode().getParentNode().getMyPort(), childAddrPort);
                    nodeControl.getSender().send(msg);
                }
            } else { //node is a root
                List<Node> list = nodeControl.getNode().getChildNodes();
                if(list.size() != 0){
                    Node newRootnode = null;
                    if(list.size() == 1){
                        newRootnode = nodeControl.getNode();
                    }
                    else{
                        newRootnode = nodeControl.getNode().getChildNodes().get(0);
                        //System.out.println("new root " +newRootnode.getIPaddr().toString().split("/")[0]);
                        String s = new String(newRootnode.getIPaddr().getAddress());
                        String parentAddrPort = Constants.HAVE_NEW_PARENT + " " + s + " " + newRootnode.getMyPort()+" ";
                        for (int i = 1; i < list.size(); i++) {
                            //посылаем детям родителя
                            Message msg = nodeControl.getMessageMaker().createMessage(Constants.NODE_DEAD, list.get(i).getIPaddr(), list.get(i).getMyPort(), parentAddrPort);
                            nodeControl.getSender().send(msg);
                            //послать родителю детей
                            String childAddrPort = Constants.HAVE_NEW_CHILD + " " + new String(list.get(i).getIPaddr().getAddress()) + " " + list.get(i).getMyPort() + " ";
                            msg = nodeControl.getMessageMaker().createMessage(Constants.NODE_DEAD, newRootnode.getIPaddr(),
                                    newRootnode.getMyPort(), childAddrPort);
                            nodeControl.getSender().send(msg);
                        }
                    }
                }
            }
            nodeControl.goToDead();
        System.out.println("by, brother");
        //умирать, только когда все подтверждения пришли
    }
}
