package NetWork;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;

/**
 * Socket client class to connect to RPI
 */
public class NetMgr {

    private String ip;
    private int port;
    private static Socket socket = null;
    private String prevMsg = null;

    private BufferedWriter out;
    private BufferedReader in;
    private int msgCounter = 0;

    private Timer wait = new Timer();


    private static NetMgr netMgr = null;

    public NetMgr(String ip, int port) {
        this.ip = ip;
        this.port = port;
        initConn();
    }

    public static NetMgr getInstance(String ip, int port) {
        if (netMgr == null) {
            netMgr = new NetMgr(ip, port);
        }
        return netMgr;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Initiate a connection with RPI if there isn't already one
     * @return true if connection established with RPI
     */
    public boolean initConn() {
        if(isConnect()) {
            System.out.println("Already connected with RPI");
            return true;
        }
        else {
            try {
                System.out.println("Initiating Connection with RPI...");
                socket = new Socket(ip, port);
                out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                System.out.println("Connection with RPI established!");
                return true;
            } catch (UnknownHostException e) {
                System.out.println("Connection Failed: UnknownHostException\n" + e.toString());
                return false;
            } catch (IOException e) {
                System.out.println("Connection Failed: IOException\n" + e.toString());
                return false;
            } catch (Exception e) {
                System.out.println("Connection Failed!\n" + e.toString());
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Close the connection with RPI
     * @return True if there is no more connection with RPI
     */
    public boolean closeConn() {
        if(!isConnect()) {
            System.out.println("No connection with RPI");
            return true;
        }
        else {
            try {
                System.out.println("Closing connection... ");
                socket.close();
                out.close();
                in.close();
                socket = null;
                return true;
            } catch (IOException e) {
                System.out.println("Unable to close connection: IOException\n" + e.toString());
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Sending a String type msg through socket
     * @param msg
     * @return true if the message is sent out successfully
     */
    public boolean send(String msg) {
        try {
            System.out.println("Sending Message...");
            out.write(msg);
            out.newLine();
            out.flush();
            msgCounter++;
            System.out.println(msgCounter +" Message Sent: " + msg);
            prevMsg = msg;
            return true;
        } catch (IOException e) {
            System.out.println("Sending Message Failed (IOException)!");
            if(socket.isConnected())
                System.out.println("Connection still Established!");
            else {
                while(true)
                {
                    System.out.println("Connection disrupted! Trying to Reconnect!");
                    if(netMgr.initConn()) {
                        break;
                    }
                }
            }
            return netMgr.send(msg);
        } catch (Exception e) {
            System.out.println("Sending Message Failed!");
            e.printStackTrace();
            return false;
        }
    }

    public String receive() {
        try {
            System.out.println("Receving Message...");
            String receivedMsg = in.readLine();
            System.out.println("Before if: ");
            if(receivedMsg != null && receivedMsg.length() > 0) {
                System.out.println("Received in receive(): " + receivedMsg);
                return receivedMsg;
            }
        } catch(IOException e) {
            System.out.println("Receiving Message Failed (IOException)!");
        } catch(Exception e) {
            System.out.println("Receiving Message Failed!");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check if there are existing connection with RPI
     * @return
     */
    public boolean isConnect() {
        if(socket == null) {
            return false;
        }
        else {
            return true;
        }
    }

    public static void main(String[] args) {
        String ip = "127.0.0.1";
        int port = 8080;
        String data;
        NetMgr netMgr = new NetMgr(ip, port);
        while(netMgr.msgCounter <= 10){
            netMgr.send(Integer.toString(netMgr.msgCounter));
            data = netMgr.receive();
            if(data == null) {
                System.out.println("Null string received.");

            }
        }
        netMgr.closeConn();
        return;

    }

}
