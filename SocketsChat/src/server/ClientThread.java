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
    private int id;
    
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
                String message = msgD.get("susername").toString() + ": " + 
                        msgD.get("message").toString();
                server.sendAll(message);
                
            }catch(IOException | ClassNotFoundException ioe){
                System.out.println("error sending message: " + ioe + "\n");
            }
        }
        if(!bRunning)
            closeCommunication();
    }
    
    
}

