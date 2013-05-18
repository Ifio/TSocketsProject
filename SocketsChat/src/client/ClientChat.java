package client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.json.simple.JSONObject;

/**
 *
 * @author Camilo Velasquez
 * @author Jason Carcamo
 * @since 16/05/2013
 * @version 1.0
 */
public class ClientChat extends Thread {

    private ClientGUI clientGUI;
    private DataInputStream dis;
    private ObjectOutputStream dos;
    private Socket client;
    private String susername;
    private String schatRoom = "";

    ClientChat(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    public void createConn(String shost, int iport, String susername) {
        try {
            client = new Socket(shost, iport);
            this.susername = susername;
            dos = new ObjectOutputStream(client.getOutputStream());
            dis = new DataInputStream(client.getInputStream());

        } catch (IOException ioe) {
            System.out.println("Error connecting to the server: " + ioe + "\n");
        }
    }

    public void sendMessage(String message, int itype) {
        JSONObject msg = new JSONObject();
        msg.put("susername", susername);
        msg.put("chatRoom", schatRoom);
        msg.put("message", message);
        msg.put("type", new Integer(itype));

        try {
            dos.writeObject(msg);
        } catch (IOException ioe) {
            System.out.println("Error sending message: " + ioe + "\n");
        }
    }

    void createRoom(String schatRoom) {
        this.schatRoom = schatRoom;
        sendMessage("",1);
    }

    void getRooms() {
        //TODO
    }

    @Override
    public void run() {
        boolean bRunning = true;

        while (bRunning) {
            try {
                String smsg = dis.readUTF();
                switch(smsg){
                     
                    case "1":
                        clientGUI.addRoom("qwerty");  
                        break;
                    default:    
                }
                clientGUI.recieveMessage(smsg);
                
            } catch (IOException ioe) {
                System.out.println("Error recieving message: " + ioe + "\n");
                bRunning = false;
            }
        }
    }
}
