import java.net.*;
import java.io.*;

public class TestConn {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing connection to 192.168.0.18:5432...");
        System.setProperty("java.net.preferIPv4Stack", "true");
        Socket s = new Socket();
        s.connect(new InetSocketAddress("192.168.0.18", 5432), 5000);
        System.out.println("Connected!");
        s.close();
    }
}