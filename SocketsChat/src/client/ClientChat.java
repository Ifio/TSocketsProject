package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
    private DataOutputStream dos;
    private Socket client;
    
    ClientChat(ClientGUI clientGUI){
        this.clientGUI = clientGUI;
    }
    
    public void createConn(String shost,int iport){
        try{
            client = new Socket(shost, iport);
            dos = new DataOutputStream(client.getOutputStream());
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
    
    public void sendMessage(String msg){
        try{
            dos.writeUTF(msg);
        }catch(IOException ioe){
            System.out.println("Error sending message: " + ioe + "\n");
        }
    }
}
