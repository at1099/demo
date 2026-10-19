package com.example;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @TempDir
    Path tempDir;

    @Test
    void acceptsValidPack() throws Exception {
        int n = 2;

        Path packFile = tempDir.resolve("pack.txt");
        List<String> values = new ArrayList<>();

        for (int i = 0; i < 8 * n; i++) { //Creates a valid pack
            values.add(String.valueOf(i));
        }

        Files.write(packFile, values); //Writes valid values to pack

        //Scanner provides the filepath to readPack()
        try (Scanner input = new Scanner(packFile.toString() + System.lineSeparator())) {
            ArrayList<Card> pack = Main.readPack(input, n);

            assertEquals(8 * n, pack.size());

            for (int i = 0; i < pack.size(); i++) { //Checks each returned value is the same as the inputted value
                assertEquals(i, pack.get(i).getValue());
            }
        }
    }

    @Test
    void rejectsInvalidPlayerCounts() {
        //Should all be rejected for various reasons
        assertFalse(Main.isValidPlayerCount("0")); //Cannot have 0 players
        assertFalse(Main.isValidPlayerCount("-1")); //Cannot have nergative players
        assertFalse(Main.isValidPlayerCount("letters")); //Cannot convert string to integer
        assertFalse(Main.isValidPlayerCount("")); //Nothing inputted
        assertFalse(Main.isValidPlayerCount("2.5")); //Cannot have a float number of players

        //Tests a few inputs that should be accurate
        assertTrue(Main.isValidPlayerCount("1"));
        assertTrue(Main.isValidPlayerCount("3"));
        assertTrue(Main.isValidPlayerCount(" 4 "));
    }

    @Test
    void rejectsPackWithNegativeValue() throws Exception {
        Path invalidPack = tempDir.resolve("negativePack.txt");
        Path validPack = tempDir.resolve("validPack.txt");

        Files.write(invalidPack, List.of("1", "2", "-3", "4", "5", "6", "7", "8")); //Writes an invalid pack to a file

        Files.write(validPack, List.of("1", "2", "3", "4", "5", "6", "7", "8")); //Writes a valid pack to a file

        String paths = invalidPack + System.lineSeparator() + validPack + System.lineSeparator();

        try (Scanner input = new Scanner(paths)) {
            ArrayList<Card> pack = Main.readPack(input, 1);

            assertEquals(8, pack.size()); //Checks pack returned is the correct length
            assertTrue(pack.stream().allMatch(card -> card.getValue() >= 0)); //Checks all cards in pack match each line in file
        }
    }

    @Test
    void rejectsPackWithNonIntegerValue() throws Exception {
        Path invalidPack = tempDir.resolve("nonIntegerPack.txt");
        Path validPack = tempDir.resolve("validPack.txt");

        Files.write(invalidPack, List.of("1", "2", "letters", "4", "5", "6", "7", "8")); //Creates an invalid pack with a string in

        Files.write(validPack, List.of("1", "2", "3", "4", "5", "6", "7", "8")); //Creates valid pack

        String paths = invalidPack + System.lineSeparator() + validPack + System.lineSeparator();

        try (Scanner input = new Scanner(paths)) {
            ArrayList<Card> pack = Main.readPack(input, 1);

            assertEquals(8, pack.size()); //Checks the pack is the correct length
            assertTrue(pack.stream().allMatch(card -> card.getValue() >= 0)); //Checks each card matches each line in the file
        }
    }

    @Test
    void rejectsPackWithTooFewCards() throws Exception {
        Path invalidPack = tempDir.resolve("shortPack.txt");
        Path validPack = tempDir.resolve("validPack.txt");

        Files.write(invalidPack, List.of("1", "2", "3", "4", "5", "6", "7")); //Creates a pack thats too short

        Files.write(validPack, List.of("1", "2", "3", "4", "5", "6", "7", "8")); //Creates a correct length pack

        String paths = invalidPack + System.lineSeparator() + validPack + System.lineSeparator();

        try (Scanner input = new Scanner(paths)) {
            ArrayList<Card> pack = Main.readPack(input, 1);

            assertEquals(8, pack.size()); //Ensures the length is correct
        }
    }

    @Test
    void rejectsPackWithTooManyCards() throws Exception {
        Path invalidPack = tempDir.resolve("longPack.txt");
        Path validPack = tempDir.resolve("validPack.txt");

        Files.write(invalidPack, List.of("1", "2", "3", "4", "5", "6", "7", "8", "9")); //Creates a pack that is too long

        Files.write(validPack, List.of("1", "2", "3", "4", "5", "6", "7", "8")); //Creates a correct length pack

        String paths = invalidPack + System.lineSeparator() + validPack + System.lineSeparator();

        try (Scanner input = new Scanner(paths)) {
            ArrayList<Card> pack = Main.readPack(input, 1);

            assertEquals(8, pack.size()); //Ensures length is correct
        }
    }

    @Test
    void retriesUntilValidPackIsProvided() throws Exception {
        Path validPack = tempDir.resolve("validPack.txt");

        Files.write(validPack, List.of("1", "2", "3", "4", "5", "6", "7", "8")); //Creates valid pack

        Path missingPack = tempDir.resolve("missingPack.txt"); //Creates a path to nonexistent file

        String paths = missingPack + System.lineSeparator() + validPack + System.lineSeparator();

        try (Scanner input = new Scanner(paths)) {
            ArrayList<Card> pack = Main.readPack(input, 1); //Provides both packs to the readPack function, it should not be able to find the first one and ask for the second

            assertEquals(8, pack.size());
            assertEquals(1, pack.get(0).getValue());
            assertEquals(8, pack.get(7).getValue());
        }
    }

   @Test
    void eachPlayerWritesToTheirOwnOutputFile() throws Exception {
        AtomicInteger winner = new AtomicInteger(1);

        Deck deck1 = new Deck(1);
        Deck deck2 = new Deck(2);

        Player player1 = new Player(1, winner, deck1, deck2);
        Player player2 = new Player(2, winner, deck2, deck1);

        for (int i = 0; i < 4; i++) {
            player1.setHandCard(new Card(1));
            player2.setHandCard(new Card(2));
        }

        player1.writeToFile(); //Has player write to their file
        player2.writeToFile(); //Has player write to their file

        Path player1File = Path.of("player1_output.txt");
        Path player2File = Path.of("player2_output.txt");

         //Checks the players wrote to their file
        assertTrue(Files.exists(player1File));
        assertTrue(Files.exists(player2File));  
        assertNotEquals(player1File, player2File);

        String player1Contents = Files.readString(player1File);
        String player2Contents = Files.readString(player2File);

        //Checks the file contents
        assertTrue(player1Contents.contains("player 1 wins"));
        assertTrue(player2Contents.contains("player 1 has informed player 2"));
    }
}