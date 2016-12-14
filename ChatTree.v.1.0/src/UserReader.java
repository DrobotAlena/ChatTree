import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Alena on 22.11.2016.
 */
public class UserReader implements Runnable {

    NodeControl nodeControl;
    public UserReader(NodeControl nodeControl) {
        this.nodeControl = nodeControl;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String data;
            while (!Thread.currentThread().isInterrupted()) {
                    data = reader.readLine();
                if(data != null) {
                    nodeControl.sendUserText(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
