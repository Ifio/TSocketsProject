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
    private ArrayList<ClientThread> alclients;
    private HashMap<Integer,String> hmclients;
    private ArrayList<String> alchatRooms;
    private int icurrentClientId = 0;
    
    //constructor
    ServerDaemon(){
        alclients = new ArrayList();
        alchatRooms = new ArrayList();
        hmclients = new HashMap();
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
                ClientThread ctclient  = new ClientThread(this,client);
                alclients.add(ctclient);
                ctclient.start();
                
            }
            
            closeCommunication();
            
        }catch(IOException ioe){
            System.out.println("Sorry, failed to create server socket: " + ioe + "\n");
            bwaiting = false;
        }
    }
    
    void setNewClient(ClientThread ctclient, String susername){
        hmclients.put(icurrentClientId, susername);
        ctclient.setIclientId(icurrentClientId);
        System.out.println(hmclients.get(icurrentClientId));
        icurrentClientId++;
    }
    
    private void closeCommunication() {
        for(int i = 0; i < alclients.size();i++)
            alclients.get(i).closeCommunication();
    }
    
    void sendAll(String message,String schatRoom) {
        for(int i = 0; i < alclients.size(); i++){
            ClientThread client = alclients.get(i);
            System.out.println(client.getSchatRoom());
            System.out.println(schatRoom);
            if(client.getSchatRoom().equals(schatRoom)){
                client.sendMessage(message);
            }
        }
    }

    void createRoom(String schatRoom, ClientThread client) {
        alchatRooms.add(schatRoom);
        client.sendMessage("1");
    }
}
