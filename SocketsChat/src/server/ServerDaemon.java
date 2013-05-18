package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

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
    private HashMap<String,Integer> hmchatRooms;
    private int ihereWeGo = 0;
    
    //constructor
    ServerDaemon(){
        alClients = new ArrayList();
        hmchatRooms = new HashMap();
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
            bwaiting = false;
        }
    }
    
    private void closeCommunication() {
        for(int i = 0; i < alClients.size();i++)
            alClients.get(i).closeCommunication();
    }
    
    void sendAll(String message,String sroom) {
        for(int i = 0; i < alClients.size(); i++){
            ClientThread client = alClients.get(i);
            if(client.getIroomId() == hmchatRooms.get(sroom)){
                client.sendMessage(message);
            }
        }
            
    }

    void createRoom(String schatRoom, ClientThread client) {
        hmchatRooms.put(schatRoom, ihereWeGo);
        client.setIclientId(ihereWeGo);
        ihereWeGo++;
        client.sendMessage("1");
    }
}
