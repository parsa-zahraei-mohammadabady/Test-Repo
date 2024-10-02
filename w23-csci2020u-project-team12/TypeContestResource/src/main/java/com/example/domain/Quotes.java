package com.example.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

//Class representation of the entire Quotes.json file
public class Quotes {

    //List of quote objects
    @JsonProperty("quotes")
    public List<Quote> quoteList;

    //Returns quote with matching Id
    public Quote findQuoteId(String Id){
        if (quoteList != null){
            for (Quote q: quoteList){
                if (Objects.equals(q.getQuoteID(), Id)){
                    return q;
                }
            }
        }

        return null;
    }
}
