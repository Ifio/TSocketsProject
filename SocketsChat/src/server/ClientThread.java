package server;

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
public class ClientThread extends Thread {
    
    private ServerDaemon server;
    private Socket client;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    ClientThread(ServerDaemon server, Socket client){
        this.server = server;
        this.client = client;
        
        try{
            //obtain input and output streams from the client... OH YEAH!
            dos = new DataOutputStream(client.getOutputStream());
            dis = new DataInputStream(client.getInputStream());
            
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
                server.sendAll(dis.readUTF());
            }catch(IOException ioe){
                System.out.println("error sending message: " + ioe + "\n");
            }
        }
        if(!bRunning)
            closeCommunication();
    }
}
