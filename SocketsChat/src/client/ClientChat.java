package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import org.json.simple.JSONObject;

/**
 *
 * @author Camilo Velasquez
 * @author Jason Carcamo
 * @since 16/05/2013
 * @version 2.0
 */
public class ClientChat extends Thread {

    private ClientGUI clientGUI;
    private ObjectInputStream dis;
    private ObjectOutputStream dos;
    private Socket client;
    private String susername;
    private String schatRoom = "";
    private ArrayList<String> alchatRooms;
    public static final int NEW_CLIENT = 0;
    public static final int MESSAGE = 1;
    public static final int NEW_ROOM = 2;
    public static final int JOIN_ROOM = 3;
    public static final int ROOMS_INFO = 4;
    public static final int USERS_INFO = 5;
    public static final int DISCONNECT = 6;

    ClientChat(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
        alchatRooms = new ArrayList();
    }

    //Getters and Setters
    public String getSusername() {
        return susername;
    }
    
    public void setSusername(String susername){
        this.susername = susername;
    }

    public String getSchatRoom() {
        return schatRoom;
    }

    public void setSchatRoom(String schatRoom) {
        this.schatRoom = schatRoom;
    }

    public ArrayList<String> getAlchatRooms() {
        return alchatRooms;
    }

    public void createConn(String shost, int iport, String susername) {
        try {
            client = new Socket(shost, iport);
            this.susername = susername;
            dos = new ObjectOutputStream(client.getOutputStream());
            dis = new ObjectInputStream(client.getInputStream());
        } catch (IOException ioe) {
            System.out.println("Error connecting to the server: " + ioe + "\n");
        }
    }
    
    public void deConn(String susername){
        try{
            closeComm(dis,dos);
        }catch(Exception e){
            System.out.println("Trouble disconnecting:" + e + "\n");
        }
    }
    
    public void closeComm(ObjectInputStream ois, ObjectOutputStream oos){
        try{
          ois.close();
          oos.close();
        }catch(IOException ioe){
            System.out.println("Failed closing communication: " + ioe + "\n");
        }
        
    }

    void createRoom(String schatRoom) {
        this.schatRoom = schatRoom;
        sendMessage(susername + " has joined to the room!",
                ClientChat.NEW_ROOM);
    }

    public void sendMessage(String message, int itype) {
        JSONObject msg = new JSONObject();
        msg.put("username", susername);
        msg.put("chatRoom", schatRoom);
        msg.put("message", message);
        msg.put("type", new Integer(itype));
        try {
            dos.writeObject(msg);
        } catch (IOException ioe) {
            System.out.println("Error sending message: " + ioe + "\n");
        }
    }
    
    private void typeMessageHandler(JSONObject msgD, String msg, int itype) throws IOException {

        switch (itype) {
            case MESSAGE:
                clientGUI.recieveMessage(msg);
                break;
            case NEW_ROOM:
                clientGUI.addRooms(alchatRooms);
                break;
            case ROOMS_INFO:
                ArrayList<String> alroomsInfo =
                        (ArrayList<String>) msgD.get("roomsInfo");
                clientGUI.appendInfo("Rooms", alroomsInfo);
                break;
            case USERS_INFO:
                ArrayList<String> alusersInfo =
                        (ArrayList<String>) msgD.get("usersInfo");
                clientGUI.appendInfo("Users", alusersInfo);
                break;
            case DISCONNECT:
                //TODO
                break;
            default:
                
                break;
        }
    }

    @Override
    public void run() {
        boolean brunning = true;

        while (brunning) {
            try {
                //recieve and decode the JSON objects sent by the clients
                JSONObject msgD = (JSONObject) dis.readObject();
                String msg = msgD.get("message").toString();
                alchatRooms = (ArrayList<String>) msgD.get("chatRooms");
                int itype = Integer.parseInt(msgD.get("type").toString());

                typeMessageHandler(msgD, msg, itype);

            } catch (IOException | ClassNotFoundException ioe) {
                System.out.println("Error recieving message: " + ioe + "\n");
                brunning = false;
            }
        }
    }
}
