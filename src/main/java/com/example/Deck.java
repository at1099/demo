package com.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Deck {
    private final int id; //Deck id
    private Queue<Card> cards = new LinkedList<>(); //linked list queue with data type of Card

    public Deck(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public synchronized void addCard(Card card) {
        //Add a card to the deck    
        cards.add(card);
    }

    public synchronized Card takeCard() {
        //Remove a card from the deck
        return cards.poll();
    }

    public int size(){
        //Returns number of cards in a deck
        return cards.size();
    }

    public synchronized String contentsToString() { //LinkedList isnt thread safe so this must be synchronized
        String contents = "deck" + id + " contents:";
        for (Card card : cards) {
            contents = contents + " " + card.getValue();
        }
        return contents;
    }

    public void writeToFile() {
        String line = contentsToString(); //Get the deck contents as a string

        try {
            FileWriter myWriter = new FileWriter("deck" + id + "_output.txt"); //Creates/Replaces file
            myWriter.write(line); //Writes contents to the file
            myWriter.close();
        }
        catch (IOException e) { //Catches any file handling errors
            System.out.println("An error occured");
        }
    }
}