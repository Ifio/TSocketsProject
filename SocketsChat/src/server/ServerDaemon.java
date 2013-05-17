package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Camilo Velasquez
 * @author Jason Carcamo
 * @since 16/05/2013
 * @version 1.0
 */
public class ServerDaemon {
    
    //global variables
    private int iport = 2500;
    private boolean bwaiting;
    private ArrayList<ClientThread> alClients;
    //constructor
    ServerDaemon(){
       alClients = new ArrayList();
    }
    
    
    public static void main(String [] args ){
        new ServerDaemon().startDaemon();
    }
    
    public void startDaemon(){
        
        bwaiting = true;
        ServerSocket server;
        
        try{
            server = new ServerSocket(iport);
            //wait and accept new connections
            while(bwaiting){
                System.out.println("Server is waiting for connections on port: " + iport);
                Socket client = server.accept();
                ClientThread tClient  = new ClientThread(this,client);
                alClients.add(tClient);
                tClient.start();
            }
            
            closeCommunication();
            
        }catch(IOException ioe){
            System.out.println("Sorry, failed to create server socket: " + ioe + "\n");
            
        }
    }

    private void closeCommunication() {
        for(int i = 0; i < alClients.size();i++)
            alClients.get(i).closeCommunication();
    }

    void sendAll(String message) {
        for(int i = 0; i < alClients.size(); i++)
            alClients.get(i).sendMessage(message);
   }
}
