package server;

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
 * @version 1.0
 */
public class ClientThread extends Thread {

    private ServerDaemon server;
    private Socket client;
    private ObjectInputStream dis;
    private ObjectOutputStream dos;
    private String schatRoom;
    private int iclientId;
    public static final int NEW_CLIENT = 0;
    public static final int MESSAGE = 1;
    public static final int NEW_ROOM = 2;
    public static final int JOIN_ROOM = 3;
    public static final int ROOMS_INFO = 4;
    public static final int USERS_INFO = 5;
    public static final int DISCONNECT = 6;

    ClientThread(ServerDaemon server, Socket client) {
        this.server = server;
        this.client = client;

        try {
            //obtain input and output streams from the client... OH YEAH!
            dos = new ObjectOutputStream(client.getOutputStream());
            dis = new ObjectInputStream(client.getInputStream());

            //TODO alert new clients connection in the ROOM!!!! BOO!!!

        } catch (IOException ioe) {
            System.out.println("Error obtaining data streams: " + ioe + "\n");
        }
    }

    //Getters and Setters
    public int getIclientId() {
        return iclientId;
    }

    public void setIclientId(int iclientId) {
        this.iclientId = iclientId;
    }

    public String getSchatRoom() {
        return schatRoom;
    }

    void leaveRoom(String schatRoom) {
        if (!schatRoom.equals("")) {
            String message =
                    server.getHmclients().get(iclientId)
                    + " has left the room!";
            server.sendAll(message, MESSAGE, schatRoom);
        }
    }

    void closeCommunication() {
        try {
            if (dos != null) {
                dos.close();
            }
        } catch (Exception e) {
            System.out.println("Error closing Data output stream: " + e + "\n");
        }
        try {
            if (dis != null) {
                dis.close();
            }
        } catch (Exception e) {
            System.out.println("Error closing Data input stream: " + e + "\n");
        }
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            System.out.println("Error closing socket " + e + "\n");
        }
    }

    void sendMessage(String smessage, ArrayList<String> alchatRooms,
            ArrayList<String> alroomsInfo, ArrayList<String> alusersInfo,
            int itype) {
        JSONObject msg = new JSONObject();
        msg.put("clientId", new Integer(iclientId));
        msg.put("message", smessage);
        msg.put("chatRooms", new ArrayList(alchatRooms));
        msg.put("type", new Integer(itype));
        switch (itype) {
            case ROOMS_INFO:
                msg.put("roomsInfo", new ArrayList(alroomsInfo));
                break;
            case USERS_INFO:
                msg.put("usersInfo", new ArrayList(alusersInfo));
                break;
        }
        try {
            dos.writeObject(msg);
        } catch (IOException ioe) {
            System.out.println("Error sending message: " + ioe + "\n");
        }
    }

    @Override
    public void run() {
        boolean brunning = true;

        while (brunning) {
            try {
                //recieve and decode the JSON objects sent by the clients
                JSONObject msgD = (JSONObject) dis.readObject();
                String susername = msgD.get("username").toString();
                iclientId = Integer.parseInt(msgD.get("clientId").toString());
                String msg = msgD.get("message").toString();
                String schatRoomBefore = schatRoom;
                schatRoom = msgD.get("chatRoom").toString();
                int itype = Integer.parseInt(msgD.get("type").toString());
                switch (itype) {
                    case NEW_CLIENT:
                        server.setNewClient(this, susername);
                        System.out.println(iclientId);
                        server.fetchRooms(this);
                        break;
                    case MESSAGE:
                        msg = susername + ": " + msg;
                        server.sendAll(msg, itype, schatRoom);
                        break;
                    case NEW_ROOM:
                        server.createRoom(this, schatRoomBefore);
                        leaveRoom(schatRoomBefore);
                        server.sendAll(msg, MESSAGE, schatRoom);
                        break;
                    case JOIN_ROOM:
                        server.updateRooms(schatRoomBefore);
                        leaveRoom(schatRoomBefore);
                        server.sendAll(msg, MESSAGE, schatRoom);
                        break;
                    case ROOMS_INFO:
                        server.roomsInfo(this);
                        break;
                    case USERS_INFO:
                        server.usersInfo(this);
                        break;
                    case DISCONNECT:
                        //TODO
                        break;
                    default:
                        break;
                }

            } catch (IOException | ClassNotFoundException ioe) {
                System.out.println("error sending message: " + ioe + "\n");
                brunning = false;
            }
        }
        if (!brunning) {
            closeCommunication();
        }
    }
}
