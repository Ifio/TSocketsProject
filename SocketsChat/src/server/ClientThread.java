package server;

import client.ClientChat;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
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
    private DataOutputStream dos;
    private String schatRoom;
    private int iclientId;

    ClientThread(ServerDaemon server, Socket client){
        this.server = server;
        this.client = client;
        
        try{
            //obtain input and output streams from the client... OH YEAH!
            dos = new DataOutputStream(client.getOutputStream());
            dis = new ObjectInputStream(client.getInputStream());
            
            //TODO alert new clients connection in the ROOM!!!! BOO!!!
            
        }catch(IOException ioe){
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
    
    void sendMessage(String smessage){
//        JSONObject msg = new JSONObject();
//        msg.put("susername", susername);
//        msg.put("chatRoom", schatRoom);
//        msg.put("message", message);
//        msg.put("type", new Integer(itype));
        try{
            dos.writeUTF(smessage);
        }catch(IOException ioe){
            System.out.println("Error sending message: " + ioe + "\n");
        }
    }
    
    void closeCommunication(){
        try {
            if(dos != null) dos.close();
        }
        catch(Exception e) {
            System.out.println("Error closing Data output stream: " + e + "\n");
        }
        try {
            if(dis != null) dis.close();
        }
        catch(Exception e) {
            System.out.println("Error closing Data input stream: " + e + "\n");
        }
        try {
            if(client != null) client.close();
        }
        catch (Exception e) {
            System.out.println("Error closing socket " + e + "\n");
        }
    }
    
    @Override
    public void run(){
        boolean bRunning = true;
        
        while(bRunning){
            try{
                //recieve and decode the JSON objects sent by the clients
                JSONObject msgD = (JSONObject) dis.readObject();
                String susername = msgD.get("susername").toString();
                String msg = msgD.get("message").toString();
                schatRoom = msgD.get("chatRoom").toString();
                int itype = Integer.parseInt(msgD.get("type").toString());
                switch(itype){
                    case ClientChat.NEW_CLIENT:
                        server.setNewClient(this, susername);
                        break;
                    case ClientChat.MESSAGE:
                        server.sendAll(susername + ": " + msg, schatRoom);
                        break;
                    case ClientChat.NEW_ROOM:
                        server.createRoom(schatRoom, this);
                        break;
                    default:
                        
                        break;
                 }
                
            }catch(IOException | ClassNotFoundException ioe){
                System.out.println("error sending message: " + ioe + "\n");
            }
        }
        if(!bRunning)
            closeCommunication();
    }
    
    
}

