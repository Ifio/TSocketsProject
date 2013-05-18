package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
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
    private int iclientId;
    private int iroomId;

    public int getIclientId() {
        return iclientId;
    }

    public void setIclientId(int iclientId) {
        this.iclientId = iclientId;
    }

    public int getIroomId() {
        return iroomId;
    }

    public void setIroomId(int iroomId) {
        this.iroomId = iroomId;
    }
    
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
    
    void sendMessage(String smessage){
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
                String schatRoom = msgD.get("chatRoom").toString();
                int itype = Integer.parseInt(msgD.get("type").toString());
                switch(itype){
                    case 1:
                        server.createRoom(schatRoom, this);
                        
                        break;
                    case 2:
                        
                        break;
                    default:
                        server.sendAll(susername + ": " + msg, schatRoom);
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

