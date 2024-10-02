package com.example.domain;

import java.util.*;

//Class that represents the room where the type racer is played
public class GameRoom {

    //Constructor
    public GameRoom(String roomName, String quoteID){
        this.roomName = roomName;
        this.quoteID = quoteID;
    }

    //This field and it's getter are obsolete
    private int playerCount = 0;

    public int iterCount() {return playerCount++;}

    //The name of the room
    private String roomName;

    //Getter for name
    public String getRoomName(){return this.roomName;}

    //The ID of the quote displayed in the room
    private String quoteID;

    //Getter
    public String getQuoteID(){return this.quoteID;}

    //List of client session IDs
    private List<String> userMap = new ArrayList<>();

    //Getter
    public List<String> getUserMap() {return this.userMap;}

    //Map of a client's score as a textual map to the amount of time it took the client
    //to successfully type out the quote
    private Map<String, Integer> bestScore = new HashMap<>();

    //Getter
    public Map<String, Integer> getBestScore() {return this.bestScore;}


}
