package com.example.typeracerwebsocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;
import jakarta.websocket.OnOpen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.domain.GameRoom;

@ServerEndpoint(value="/ws/{gameRoom}")
public class TypeServer {

    //List of active gameRooms
    static List<GameRoom> gameRooms = new ArrayList<>();

    //Field that represents the current gameRoom client is connected to
    static GameRoom currentRoom;

    //Generates random number to be used to call API
    private int randomWithRange(int min, int max){
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    //Updates a client's list of rooms
    private void updateGameRooms(Session session) throws IOException{
        for (GameRoom room: gameRooms){

            String gameRoomString = String.format("{\"type\": \"instruct\", \"message\":\"AddRoom %s %s\"}", room.getRoomName(), room.getQuoteID());

            session.getBasicRemote().sendText(gameRoomString);

        }
    }

    //Updates the leader board in a game room
    private void refreshBoard(Session session) throws IOException {

        String refreshMessage = String.format("{\"type\": \"leaderBoard\", \"message\":\"RefreshBoard \"}");

        //Send message to front end to wipe leaderboard
        for (Session peer: session.getOpenSessions()){

            if (currentRoom.getUserMap().contains(peer.getId())){

                peer.getBasicRemote().sendText(refreshMessage);
            }
        }

        //Create a list of entries of the score messages and corresponding speed of completion
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>();

        for (Map.Entry<String, Integer> entry: currentRoom.getBestScore().entrySet()){

            sortedEntries.add(entry);

        }

        //Rank the meassges from lowest time of completion to highest
        Collections.sort(sortedEntries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        //Update the leaderboard of every client in the same room with score messages, in the order of least speed of completion
        //to highest
        for (Map.Entry<String, Integer> entry: sortedEntries){

            String messageKey = entry.getKey();

            String boardMessage = String.format("{\"type\": \"leaderBoard\", \"message\":\"AddScore %s\"}", messageKey);

            for (Session peer: session.getOpenSessions()){

                if (currentRoom.getUserMap().contains(peer.getId())){

                    peer.getBasicRemote().sendText(boardMessage);
                }
            }
        }

    }

    //Add user's score the leaderboard
    private void updateBoard(Session session, String message) throws IOException{

        String[] splitMessage = message.split(" ");

        int speed = Integer.parseInt(splitMessage[3]);

        currentRoom.getBestScore().put(message, speed);

        refreshBoard(session);
    }

    //Create a new gameRoom
    public void createRoom(String gameRoom, Session session) throws IOException{

        boolean newChatRoom = true;

        //Check if client is joining existing room
        for (GameRoom room: gameRooms){

            if (gameRoom.equals(room.getRoomName())){
                newChatRoom = false;

                //Assign current room to room matching the provided name
                currentRoom = room;

                //Add the user to the existing GameRoom
                currentRoom.getUserMap().add(session.getId());

                String joinGameRoomString = String.format("{\"type\": \"instruct\", \"message\":\"CreateNewRoom %s %s\"}", currentRoom.getRoomName(), currentRoom.getQuoteID());

                //Send message to front end to update client's list of rooms
                session.getBasicRemote().sendText(joinGameRoomString);
            }
        }

        //Create new room if room name is novel
        if (newChatRoom){

            //Randomly generate the number of the quote that will be associated with the room
            //This field will be used in the front end to call the API
            String quoteID = Integer.toString((randomWithRange(0,14)));

            currentRoom = new GameRoom(gameRoom, quoteID);

            //Add the user to the new GameRoom
            currentRoom.getUserMap().add(session.getId());

            //Add new GameRoom to the list
            gameRooms.add(currentRoom);

            //Broadcast the creation of a new Game Room to peers
            for (Session peer: session.getOpenSessions()){

                String newGameRoomString = String.format("{\"type\": \"instruct\", \"message\":\"CreateNewRoom %s %s\"}", gameRoom, quoteID);

                peer.getBasicRemote().sendText(newGameRoomString);
            }
        }

    }

    @OnClose
    public void onClose(Session session){

        //Upon client's exist, remove them from any room they were in
        for (GameRoom room: gameRooms){
            if (room.getUserMap().contains(session.getId())){
                room.getUserMap().remove(session.getId());
            }
        }

    }

    @OnOpen
    public void onOpen(@PathParam("gameRoom") String gameRoom, Session session) throws IOException {

        //Allow new user to refresh their list of rooms
        if (gameRoom.equals("Refresh")){
            updateGameRooms(session);
        }
        else{
                //For other messages, create new GameRoom
                createRoom(gameRoom, session);
        }

    }

    @OnMessage
    public void interpretMessage(String comm, Session session) throws IOException{

        String userId = session.getId();

        JSONObject jsonmsg = new JSONObject(comm);

        String type = (String) jsonmsg.get("type");

        String message = (String) jsonmsg.get("msg");

        //Refresh connected user's list of game rooms
        if (type.equals("Refresh")){
            updateGameRooms(session);
        } else if (type.equals("createRoom")){
            createRoom(message, session);
        }else if (type.equals("BoardRequest")){ //Reload the users leader board
            updateBoard(session, message);
        }
    }

}
