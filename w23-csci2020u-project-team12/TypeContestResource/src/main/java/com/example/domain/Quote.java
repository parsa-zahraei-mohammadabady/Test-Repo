package com.example.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

//Class Representation of a Quote Json object
public class Quote {

    public Quote() {}

    //The quotes' number
    @JsonProperty("quoteID")
    private String quoteID;

    //Author of the quote
    @JsonProperty("author")
    private String author;

    //Piece of writing from which the quote originates
    @JsonProperty("from")
    private String from;

    //Text of the quote
    @JsonProperty("quoteText")
    private String[] quoteText;

    //Getters
    public String getQuoteID(){return this.quoteID;}

    public String getAuthor() {return this.author;}

    public String getFrom() {return this.from;}

    public String[] getQuoteText() {return this.quoteText;}



}
