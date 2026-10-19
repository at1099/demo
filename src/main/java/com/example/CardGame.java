package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public Deck[] decks;
    public Player[] players;
    public List<Thread> threads;
    public AtomicInteger winner = new AtomicInteger(0); //A variable that can be shared across several threads and accessed, but still keep its integrity

    public void gameSetup(int n, ArrayList<Card> pack){
        //Create players list and decks list
        players = new Player[n];
        decks = new Deck[n];
        threads = new ArrayList<>();
        

        //Iteratively create n decks
        for (int i = 0; i < n; i++) {
            decks[i] = new Deck(i + 1);
        }

        //Create players and threads
        for (int i = 0; i < n; i++){
            Deck drawDeck = decks[i];
            Deck discardDeck;
            if (i == n-1){
                discardDeck = decks[0];
            } else{
                discardDeck = decks[i+1];
            }

            players[i] = new Player(i + 1, winner, drawDeck, discardDeck);

            threads.add(new Thread(players[i], "player-" + (i+1)));
        }

        //Deal four cards to each player in a round-robin fashion
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < n; j++){
                int currentIndex = (i * n) + j;
                players[j].setHandCard(pack.get(currentIndex)); //SetHandCard will be a player function that takes a hand index and Card object as inputs
            }
        }

        //Distribute the remaining cards to the decks (round-robin)
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < n; j++){
                int currentIndex = (4 * n) + (i * n) + j;
                decks[j].addCard(pack.get(currentIndex));
            }
        }
    }

    public static void main(String[] args) {
        //User input
        Scanner input = new Scanner(System.in); 
        int n = 0;

        while (n <= 0) { //While loop to keep asking number of players until input is valid
            System.out.println("Enter a positive number of players: ");

            String playerInput = input.nextLine();

            if (isValidPlayerCount(playerInput)) {
                n = Integer.parseInt(playerInput.trim());
            } else {
                System.out.println("Invalid Input");
            }
        }


        ArrayList<Card> pack = readPack(input, n); //Creates pack variable
        input.close();

        //Set up the game
        Main game = new Main(); //Creates a new instance of Main() class to run non-static functions
        game.gameSetup(n, pack);

        System.out.println("Game setup complete.");

        //Start all player threads
        for (Thread thread : game.threads) {
            thread.start();
        }  

         //Wait for every player thread to finish
        for (Thread thread : game.threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        for (Deck deck : game.decks) { //Writes final deck contents
            deck.writeToFile();
        }

        System.out.println("Player " + game.winner.get() + " wins!");      
    }

    public static ArrayList<Card> readPack(Scanner input, int n) {
        //A function that takes a scanner input and a number of players and gets the player to input a valid pack file
        ArrayList<Card> pack = null;

        //Keep requesting a pack until a valid one is provided
        while (pack == null) {
            System.out.println("Enter the pack filepath:");
            String path = input.nextLine().trim();

            ArrayList<Card> possiblePack = new ArrayList<>(); //Stores the result in possiblePack until it is verified
            boolean valid = true;

            try (Scanner fileScanner = new Scanner(new File(path))) {

                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    try {
                        int value = Integer.parseInt(line);

                        if (value < 0) {
                            valid = false;
                            break; //Stops the loop if a card is negative
                        }

                        possiblePack.add(new Card(value));

                    } catch (NumberFormatException e) { //If the file contents cannot be converted into an integer
                        valid = false; 
                        break;
                    }
                }

                if (possiblePack.size() != 8 * n) {
                    valid = false; //Pack not valid if its size does not match the required number of card (8n)
                }

                if (valid) {
                    pack = possiblePack;
                } else {
                    System.out.println("Invalid pack, it must contain exactly " + (8 * n) + " non-negative integers, one per line");
                }

            } catch (FileNotFoundException e) {
                System.out.println("File not found. Please try again.");
            }
        }

        return pack;
    }

    static boolean isValidPlayerCount(String input) {
        try {
            return Integer.parseInt(input.trim()) > 0; //Checks if number of players inputted is non negative
        } catch (NumberFormatException e) { //Ensures number of players inputted can be converted to an integer
            return false;
        }
    }
}