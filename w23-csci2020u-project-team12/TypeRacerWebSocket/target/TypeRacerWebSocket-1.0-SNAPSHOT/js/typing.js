let ws;

//openGameRoom();

const quotesInput = document.getElementById("quote")
const input = document.getElementById("input")

//import {getQuote} from "./typing.js"

//Attached to the button with ID createGameRoom
let gameRoomDict = {}

//Updates the list of available rooms
function upDateRoomList(key){

    //debugger;

    let roomList = document.getElementById('room-list')
    let roomEntry = document.createElement("li");

    roomEntry.setAttribute("id", "room");

    roomEntry.innerHTML = key + " : Quote" + gameRoomDict[key]

    roomList.appendChild(roomEntry);

}

//Updates the dictionary that tracks the creation of new game rooms
function refreshRoomDict(){

    //debugger;

    if (ws == null){
        //If connection is new, call backend to refresh the list of available rooms
        ws = new WebSocket("ws://localhost:8080/TypeRacerWebSocket-1.0-SNAPSHOT/ws/" + "Refresh");
    }
    else{
        let request = {"type":"Refresh", "msg":""}; //Otherwise send message request to do the same thing
        ws.send(JSON.stringify(request));
    }

    //Handle response messages from the server
    ws.onmessage = function (event) {

        let message = JSON.parse(event.data);

        let splitMessage = message.message.split(" ");

        //Add the room to the dictionary if it is new
        if (!gameRoomDict.hasOwnProperty(splitMessage[1])){
            gameRoomDict[splitMessage[1]] = splitMessage[2];

            upDateRoomList(splitMessage[1]);
        }

    }

}

//Join existing room
function joinRoom(){

    debugger;

    let roomID = document.getElementById("room-code").value;

    //Entry is only valid if the room name is in the dictionary of available rooms
    if (gameRoomDict.hasOwnProperty(roomID)){
        createRoom(roomID)
    }
}

//Create new or existing game room
function createRoom(joinID = undefined){

    debugger;

    let roomID = "";

    if (joinID !== undefined){
        roomID = joinID;
    }
    else{ //Get input from button
        roomID = document.getElementById("room-code-new").value;
    }

    if (ws == null){ //If connection is nwq, call the OnOpen function the backend
        ws = new WebSocket("ws://localhost:8080/TypeRacerWebSocket-1.0-SNAPSHOT/ws/" + roomID);
    }else{
        let request = {"type":"createRoom", "msg":roomID}; //Otherwise use the OnMessage function to accomplish the same task
        ws.send(JSON.stringify(request));
    }

    //Handle response messages from server
    ws.onmessage = function(event) {

        let message = JSON.parse(event.data);

        let splitMessage = message.message.split(" ");

        if (splitMessage.length == 3 && splitMessage[0] == "CreateNewRoom"){

            //Name of the room is first part of the response message
            let roomName = splitMessage[1];

            //Code of the quote associated with the room is the second
            let quoteID = splitMessage[2];

            //If name of room is not already in the dictionary, add it
            if (!gameRoomDict.hasOwnProperty(roomName)){
                gameRoomDict[roomName] = quoteID;

                upDateRoomList(roomName);
            }

            //Fetch the quote from the API, taking the quoteID as a parameter
            fetchQuote(quoteID);
        }



    }

}

//Call the API to retrieve the quote associated with the room
function fetchQuote(quoteID) {

    //debugger;

    //Call the API to fetch quote associated with room
    fetch("http://localhost:8080/TypeContestResource-1.0-SNAPSHOT/api/resources/json/" + quoteID, {
        method: "Get",
        headers: {
            "Accept": "application/json"
        }
    })
        .then(response => response.json())
        .then(response => getQuote(response)
            .catch((err) => {
                console.log("something went wrong: " + err);
            }));
}


//Field Declarartions
let quoteDisplayElement;
let quoteInputElement;
let timerElement;
let textarea;
let name;
let wpm;
let author;

//this is the quote
let quote = undefined

let startTime
let interval

//Parses the user's to match it with the quote on screen
//Correctly typed characters will turn green, incorrectly typed letters will turn red
function typeInput(){

    if (quote !== undefined){


        quoteInputElement.addEventListener('input', () => {

            const arrayQuote = quoteDisplayElement.querySelectorAll('span') //wraps quote in a span
            const arrayValue = quoteInputElement.value.split('') //creates an array for each character in the quote

            let correct = true

            //attempts at each character to see if it's correct, if character is correct, its added to the correct array and removed from the incorrect array, and vice-versa
            arrayQuote.forEach((characterSpan, index) => {
                const character = arrayValue[index]
                if (character == null) {
                    characterSpan.classList.remove('correct')
                    characterSpan.classList.remove('incorrect')
                    correct = false
                } else if (character === characterSpan.innerText) {
                    characterSpan.classList.add('correct')
                    characterSpan.classList.remove('incorrect')
                } else {
                    characterSpan.classList.remove('correct')
                    characterSpan.classList.add('incorrect')
                    correct = false
                }
            })

            if (correct) //flag
            {

                textarea.setAttribute('disabled', true); //disabled input
                wpm = Math.round(quote.split(" ").length / (getTimerTime() / 60)) //gets time in minutes, divides length of an array of the quotes at each space (word)
                const time = getTimerTime()

                let string = quote + "-" + author + "\n" + ". You typed at a speed of " + wpm + " words per minute!"
                quoteDisplayElement.innerHTML = string

                stopTimer();

                //Create score message that will be put onto the leader board
                let leaderBoardMessage = "Player: " + name + " Time(s): " + getTimerTime() + (" WPM: ") + wpm;

               // console.log(leaderBoardMessage);

                //Upon the user's succesful completion of the game, prompt the server to update the leader board
                let BoardRequest = {"type":"BoardRequest", "msg":leaderBoardMessage};
                ws.send(JSON.stringify(BoardRequest));

                //Handle server's response messages
                ws.onmessage = function (event) {

                    debugger;

                    let message = JSON.parse(event.data);


                    addToLeaderBoard(message);


                }

            }
        })


    }


}

//Add a new score to the leader board
function addToLeaderBoard(message) {

    debugger;

    let splitMessage = message.message.split(" ");

    let leaderBoard = document.getElementById("user-scores")

    //If requested in the server's message, wipe the leader board clean
    if (splitMessage[0] == "RefreshBoard"){

        leaderBoard.innerHTML = "";

    } else if (splitMessage[0] == "AddScore"){

        let sendMessage = splitMessage.slice(1);

        let newBoardEntry = document.createElement("li");
        newBoardEntry.setAttribute("id", "board");
        newBoardEntry.innerHTML = sendMessage.join(" ")

        //sendMessage = '<li id=' + 'board' + '>' + sendMessage.join(" ") + '</li>';

        leaderBoard.appendChild(newBoardEntry);
    }

}



function loadQuote() {

    //debugger;

    // splits a quote into its characters and wraps then in the span, then adds it for quoteDisplayElement for processing
    quote.split('').forEach(character => {
        const characterSpan = document.createElement('span')
        characterSpan.innerText = character
        quoteDisplayElement.appendChild(characterSpan)
    })
    quoteInputElement.value = null
    startTimer()
}

//starts a timer at 0 and updates it every second using getTimerTime()
function startTimer() {
    timerElement.innerText = 0
    startTime = new Date()

    interval = setInterval(() => {timer.innerText = getTimerTime()}, 1000)
}

//clears the setInterval and sets the time to the current time
function stopTimer() 
{
    //Prompt the user to enter their name
    //This name will appear on the leaderboard
    name = window.prompt("Enter your name:");

    clearInterval(interval);
    timerElement.innerText = getTimerTime();
}


//gets current time
function getTimerTime() 
{
    return Math.floor((new Date() - startTime) / 1000)
}

//Parses JSON response object received from API
function getQuote(jsonQuote){

    //debugger;

    author = jsonQuote.author;
    let quoteFull = jsonQuote.quoteText;

    quote = quoteFull.join("");

    loadVars();
}

//load the elements to display the quote
function loadVars(){

        // Code to execute on index.html
        quoteDisplayElement = document.getElementById('quoteDisplay');
        quoteInputElement = document.getElementById('quoteInput');
        timerElement = document.getElementById('timer');
        textarea = document.querySelector('#quoteInput');

        loadQuote();

        typeInput();

}

//Obsolete function
function openGameRoom() {

    debugger;

    window.open("/TypeRacerWebSocket-1.0-SNAPSHOT/gameRoom.html");

}