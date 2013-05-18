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
public class ClientChat extends Thread{
    
    private ClientGUI clientGUI;
    private DataInputStream dis;
    private ObjectOutputStream dos;
    private Socket client;
    private String susername;
    
    ClientChat(ClientGUI clientGUI){
        this.clientGUI = clientGUI;
    }
    
    public void createConn(String shost,int iport, String susername){
        try{
            client = new Socket(shost, iport);
            this.susername = susername;
            dos = new ObjectOutputStream(client.getOutputStream());
            dis = new DataInputStream(client.getInputStream());
            
        }catch(IOException ioe){
            System.out.println("Error connecting to the server: " + ioe + "\n");
        }
    }
    
    @Override
    public void run(){
        boolean bRunning = true;
        
        while(bRunning){
            try{
                clientGUI.recieveMessage(dis.readUTF());
            }catch(IOException ioe){
                System.out.println("Error recieving message: " + ioe + "\n");
                bRunning = false;
            }
        }
    }
    
    public void sendMessage(String message){
        JSONObject msg = new JSONObject();
        msg.put("susername", susername);
        msg.put("messageType", new Integer(1));
        msg.put("message", message);
        
        try{
            dos.writeObject(msg);
        }catch(IOException ioe){
            System.out.println("Error sending message: " + ioe + "\n");
        }
    }
}
