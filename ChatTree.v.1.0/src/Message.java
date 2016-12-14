/**
 * Created by Alena on 24.11.2016.
 */
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;

import java.net.DatagramPacket;
import java.util.*;

public class Message {
    UUID uuid;
    Date whenWasSend;
    DatagramPacket datagramPacket;

    public Message(UUID uuid, DatagramPacket datagramPacket) {
        whenWasSend = new Date();
        this.uuid = uuid;
        this.datagramPacket = datagramPacket;
    }


    public byte getFlag(){
        return datagramPacket.getData()[Constants.FLAG_POSITION];

    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setWhenWasSend(Date whenWasSend) {
        this.whenWasSend = whenWasSend;
    }

    public void setDatagramPacket(DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;
    }

    public UUID getUuid() {

        return uuid;
    }

    public Date getWhenWasSend() {
        return whenWasSend;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }


}
