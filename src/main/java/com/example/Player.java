package com.example;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Player implements Runnable {
    private int id;
    private List<Card> hand = new ArrayList<>();
    private Deck pickupDeck;
    private Deck discardDeck;
    private AtomicInteger winner;
    private Random r = new Random(); //initialise once at the top so it doesn't need to be recreated each time
    private String outputLog = "";

    public Player(int id, AtomicInteger winner, Deck pickupDeck, Deck discardDeck) {
        this.winner = winner;
        this.id = id;
        this.pickupDeck = pickupDeck;
        this.discardDeck = discardDeck;
    }

    public void writeToFile() {
        String winningLine = "";
        if (winner.get() != id) { //If another player wins
         winningLine = "player " + winner.get() + " has informed player " + id + " that player "+winner.get() + " has won";
        }
        else { //If this player wins
            winningLine = "player " + id + " wins";
        }
        String exitLine = "player " + id + " exits"; //Line output after player wins
        String finalHandLine = "player " + id + " final hand: " + handToString(); //Final hand
        outputLog = outputLog + winningLine + '\n'+exitLine + '\n' + finalHandLine; //add the final output lines to the log before writing to file

        try {
            FileWriter myWriter = new FileWriter("player" + id + "_output.txt"); //Creates/Replaces file
            myWriter.write(outputLog); //Writes output to the file
            myWriter.close();
        }
        catch (IOException e) { //Catches any file handling errors
            System.out.println("An error occured");
        }
    }

    public void run() {
        String writeLineInitialHand = "player " + id + " initial hand " + handToString();
        outputLog=outputLog+writeLineInitialHand+"\n";
        if (checkWinningHand()) { //Initial hand check
            winner.compareAndSet(0, id);
            writeToFile();
            return;
        }
        

        while (winner.get() == 0) { //Keep trying to run playerActions until it succesfully completes a turn
            playerActions();
        }

        writeToFile();
        return;
    }

    public boolean playerActions() {
        Deck firstAction;
        Deck secondAction;

        if (pickupDeck.getId() < discardDeck.getId()) {
            firstAction = pickupDeck;
            secondAction = discardDeck;
        } else {
            firstAction = discardDeck;
            secondAction = pickupDeck;
        }

            synchronized (firstAction) {
                synchronized (secondAction) {

        Card cardPickedUp=pickupDeck.takeCard(); //Pickup card from deck
        if (cardPickedUp == null){
            return false; //If an error occurs
        }
        addCard(cardPickedUp); //Add card to hand
        String writeLinePickup = "player " + id + " draws a " + cardPickedUp.getValue() + " from deck " + pickupDeck.getId();
        outputLog = outputLog + writeLinePickup + "\n"; //Writes each hand pickup to the output file

        int discardIndex = selectCardIndexToDiscard();
        Card removedCard = removeCard(discardIndex); //Remove card from hand
        discardDeck.addCard(removedCard); //Add card to deck

        String writeLineDiscard = "player " + id + " discards a " + removedCard.getValue() + " to deck " + discardDeck.getId();
        outputLog = outputLog + writeLineDiscard + "\n"; //Writes each discard to the output file

        String writeCurrentHand = "player " + id + " current hand is " + handToString();
        outputLog = outputLog + writeCurrentHand + "\n";
        }
        }
        if (checkWinningHand()) {
            winner.compareAndSet(0, id); //Checks hand against winning criteria
        }
            return true;
    }
        
    //All these must have synchronized because ArrayList is not thread safe 
    public synchronized int getHandSize(){
        return hand.size();
    } 

    public synchronized Card getCard(int position) {
        return hand.get(position);
    }

    public synchronized void setHandCard(Card card) {
        hand.add(card);
    }   

    public synchronized void addCard(Card card) {
        hand.add(card);
    } 

    public synchronized Card removeCard(int position) {
        Card removedCard = hand.get(position);
        hand.remove(position);
        return removedCard;
    }

    public synchronized boolean checkWinningHand() {
        int checkValue = hand.get(0).getValue();
        for (int i = 1; i < 4; i++) {
            if (hand.get(i).getValue() != checkValue) { //Checks each value against the first value in the hand
                return false;
            }
        }
        return true;
    }

    public synchronized String handToString() {
        //Translates cards in hand into a string
        String handString = "";
        for (int i = 0; i < hand.size(); i++) {
            handString = handString + hand.get(i).getValue() + " ";
        }
        return handString.trim();
    }

    public synchronized int selectCardIndexToDiscard(){
        ArrayList<Integer> listOfDiscardableIndexes = new ArrayList<Integer>(); 
        for (int i = 0; i < 5; i++) { //Create list of card positions that are non-preferred
            if (hand.get(i).getValue() != id) {
                listOfDiscardableIndexes.add(i);
            }
        }

        return (listOfDiscardableIndexes.get(r.nextInt(listOfDiscardableIndexes.size()))); //Randomly choose card to discard
    }
}