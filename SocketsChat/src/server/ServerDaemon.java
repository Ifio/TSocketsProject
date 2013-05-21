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
 * @version 2.0
 */
public class ServerDaemon {

    //global variables
    private int iport = 2500;
    private boolean bwaiting;
    private ArrayList<ClientThread> alclients;
    private HashMap<Integer, String> hmclients;
    private ArrayList<String> alchatRooms;
    private int icurrentClientId = 0;

    //constructor
    ServerDaemon() {
        alclients = new ArrayList();
        alchatRooms = new ArrayList();
        hmclients = new HashMap();
    }

    public static void main(String[] args) {
        new ServerDaemon().startDaemon();

    }

    //Getters and Setters
    public HashMap<Integer, String> getHmclients() {
        return hmclients;
    }

    public void startDaemon() {

        bwaiting = true;
        ServerSocket server;
        try {
            server = new ServerSocket(iport);
            //wait and accept new connections
            while (bwaiting) {
                System.out.println("Server is waiting for connections on port: " + iport);
                Socket client = server.accept();
                ClientThread tclient = new ClientThread(this, client);
                tclient.setIclientId(icurrentClientId++);
                alclients.add(tclient);
                tclient.start();

            }

            closeCommunication();

        } catch (IOException ioe) {
            System.out.println("Sorry, failed to create server socket: " + ioe + "\n");
            bwaiting = false;
        }
    }

    void setNewClient(ClientThread tclient, String susername) {
        hmclients.put(tclient.getIclientId(), susername);
        System.out.println(hmclients);
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
                System.out.println("Disconnected client!");
            }
        }
    }
}
