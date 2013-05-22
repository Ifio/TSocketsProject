package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Camilo Velasquez
 * @author Jason Carcamo
 * @since 16/05/2013
 * @version 2.0
 */
public class ServerDaemon {

    private int iport;
    private boolean bwaiting;
    private ArrayList<ClientThread> alclients;
    private HashMap<Integer, String> hmclients;
    private ArrayList<String> alchatRooms;
    private int icurrentClientId = 0;

    //constructor
    ServerDaemon(int iport) {
        this.iport = iport;
        alclients = new ArrayList();
        alchatRooms = new ArrayList();
        hmclients = new HashMap();
    }

    public static void main(String[] args) {
        try {
            System.out.println("Please insert a port to start listening: ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            int iport = Integer.parseInt(reader.readLine());
            new ServerDaemon(iport).startDaemon();
        } catch (IOException | NumberFormatException ex) {
            System.out.println(ex);
        }

    }

    //Getters and Setters
    public HashMap<Integer, String> getHmclients() {
        return hmclients;
    }

    void startDaemon() {
        bwaiting = true;
        ServerSocket server;
        try {
            server = new ServerSocket(iport);
            System.out.println("Server is waiting for connections on port: " + 
                    iport);
            //wait and accept new connections
            while (bwaiting) {
                Socket client = server.accept();
                System.out.println("Connection accepted!, new client Id is " + 
                        icurrentClientId);
                ClientThread tclient = new ClientThread(this, client);
                tclient.setIclientId(icurrentClientId++);
                alclients.add(tclient);
                tclient.start();
                System.out.println("Server is waiting for more connections...");
            }

            closeCommunication();

        } catch (IOException ioe) {
            System.out.println("Sorry, failed to create server socket: " + ioe + "\n");
            bwaiting = false;
        }
    }

    void setNewClient(ClientThread tclient, String susername) {
        hmclients.put(tclient.getIclientId(), susername);
    }

    private void closeCommunication() {
        for (int i = 0; i < alclients.size(); i++) {
            alclients.get(i).closeCommunication();
        }
    }

    void sendAll(String message, int itype, String schatRoom) {
        for (int i = 0; i < alclients.size(); i++) {
            ClientThread client = alclients.get(i);
            if (client.getSchatRoom().equals(schatRoom)) {
                client.sendMessage(message, alchatRooms, null, null, itype);
            }
        }
    }

    void fetchRooms(ClientThread tclient) {
        if (alchatRooms.size() > 0) {
            tclient.sendMessage("", alchatRooms, null, null, ClientThread.NEW_ROOM);
        }
    }

    void createRoom(ClientThread client, String schatRoomBefore) {
        alchatRooms.add(client.getSchatRoom());
        if (schatRoomBefore.equals("")) {
            for (int i = 0; i < alclients.size(); i++) {
                alclients.get(i).sendMessage(
                        "", alchatRooms, null, null, ClientThread.NEW_ROOM);
            }
        } else {
            updateRooms(schatRoomBefore);
        }
    }

    void updateRooms(String schatRoom) {
        int inumClients = 0;
        for (int i = 0; i < alclients.size(); i++) {
            if (schatRoom.equals(alclients.get(i).getSchatRoom())) {
                inumClients++;
            }
        }
        //delete the room if there aren't clients in it
        if (inumClients == 0) {
            for (int i = 0; i < alchatRooms.size(); i++) {
                if (schatRoom.equals(alchatRooms.get(i))) {
                    alchatRooms.remove(i);
                }
            }
        }
        //update rooms
        for (int i = 0; i < alclients.size(); i++) {
            alclients.get(i).sendMessage(
                    "", alchatRooms, null, null, ClientThread.NEW_ROOM);
        }
    }

    void roomsInfo(ClientThread client) {
        ArrayList<String> alroomsInfo = new ArrayList();
        for (int i = 0; i < alchatRooms.size(); i++) {
            int iclients = 0;
            for (int j = 0; j < alclients.size(); j++) {
                if (alchatRooms.get(i).equals(alclients.get(j).getSchatRoom())) {
                    iclients++;
                }
            }
            alroomsInfo.add(alchatRooms.get(i) + " (" + iclients + " users)");
        }
        client.sendMessage("", null, alroomsInfo, null,
                ClientThread.ROOMS_INFO);
    }

    void usersInfo(ClientThread client) {
        ArrayList<String> alusersInfo = new ArrayList();
        for (int i = 0; i < alclients.size(); i++) {
            ClientThread tclient = alclients.get(i);
            String sclient = hmclients.get(tclient.getIclientId());
            if (!tclient.getSchatRoom().isEmpty()) {
                alusersInfo.add(sclient + " is in room " + tclient.getSchatRoom());
            } else {
                alusersInfo.add(sclient + " is pending for choosing room");
            }
        }
        client.sendMessage("", null, null, alusersInfo,
                ClientThread.USERS_INFO);
    }

    void removeClient(int iclientId, String schatRoom) {
        sendAll(hmclients.get(iclientId) + " has left the room!",
                ClientThread.MESSAGE, schatRoom);
        for (int i = 0; i < alclients.size(); i++) {
            if (alclients.get(i).getIclientId() == iclientId) {
                alclients.remove(i--);
                hmclients.remove(iclientId);
                updateRooms(schatRoom);
                System.out.println("Disconnected client with client Id: " + 
                        iclientId);
            }
        }
    }
}
