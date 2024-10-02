package com.example.typecontestresource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.example.domain.Quote;
import com.example.domain.Quotes;

@Path("/resources")
public class TypeResource {

    //Quotes field
    Quotes quoteList;

    ObjectMapper quoteMapper = new ObjectMapper();

    //Loads Quotes from Quotes.json
    private String loadQuotes(){

        try {
            java.nio.file.Path file = java.nio.file.Path.of(
                    String.valueOf(
                                    TypeResource.class.getResource("/Quotes/Quotes.json"))
                            .substring(6));
            return Files.readString(file);
        } catch (IOException e){
                return "Did you forget to create the file?\n" +
                        "Is the file in the right location?\n" +
                        e.toString();
            }
    }

    //Initializes the quoteList field
    private void initQuotes(){
        try{
            this.quoteList = quoteMapper.readValue(loadQuotes(), Quotes.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    //Returns a response object containing all the quotes
    @GET
    @Produces("application/json")
    @Path("/json")
    public Response getJSON(){
        String quotes = this.loadQuotes();

        return Response.status(200)
                .entity(quotes)
                .build();
    }

    //Returns quote Json object with Id matching the path parameter
    @GET
    @Produces("application/json")
    @Path("/json/{ID}")
    public Response getJSON(@PathParam("ID") String quoteID) {

        initQuotes();

        String returnQuote = "Error";

        try{
            returnQuote = quoteMapper.writeValueAsString(quoteList.findQuoteId(quoteID));
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }

        Response quoteResponse = Response.status(200)
                .entity(returnQuote)
                .build();

        return quoteResponse;
    }
}