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
        System.out.println(hmclients);
        icurrentClientId++;
    }
    
    private void closeCommunication() {
        for(int i = 0; i < alclients.size();i++)
            alclients.get(i).closeCommunication();
    }
    
    void sendAll(String message, int itype, String schatRoom) {
        for(int i = 0; i < alclients.size(); i++){
            ClientThread client = alclients.get(i);
            if(client.getSchatRoom().equals(schatRoom)){
                client.sendMessage(message, alchatRooms, itype);
            }
        }
    }
    
    void fetchRooms(ClientThread client){
        if(alchatRooms.size() > 0)
            client.sendMessage("", alchatRooms, ClientThread.NEW_ROOM);
    }

    void createRoom(ClientThread client, String schatRoomBefore) {
        alchatRooms.add(client.getSchatRoom());
        if (schatRoomBefore.equals("")){
            for(int i = 0; i < alclients.size(); i++){
                alclients.get(i).sendMessage(
                        "", alchatRooms, ClientThread.NEW_ROOM);
            }
        }else{
             updateRooms(schatRoomBefore);
        }
   }
    
    void updateRooms(String schatRoomBefore) {
        int inumClients = 0;
        for(int i = 0; i < alclients.size(); i++){
            if(schatRoomBefore.equals(alclients.get(i).getSchatRoom()))
                inumClients++;
        }
        //delete the room if there aren't clients in it
        if (inumClients == 0){
            for(int i = 0; i < alchatRooms.size(); i++){
                if(schatRoomBefore.equals(alchatRooms.get(i)))
                    alchatRooms.remove(i);
            }
        }
        //update rooms
        for(int i = 0; i < alclients.size(); i++){
            alclients.get(i).sendMessage(
                    "", alchatRooms, ClientThread.NEW_ROOM);
        }
    }
}
