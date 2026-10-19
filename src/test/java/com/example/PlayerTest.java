package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

class PlayerTest {

    @Test
    void playerWritesOutputFileWithItsUniqueId() throws Exception {
        AtomicInteger winner = new AtomicInteger(1);

        Deck pickupDeck = new Deck(1);
        Deck discardDeck = new Deck(2);

        Player player = new Player(1, winner, pickupDeck, discardDeck);

        for (int i = 0; i < 4; i++) {
            player.setHandCard(new Card(1));
        }

        Path playerFile = Path.of("player1_output.txt"); //Expected player file path

        try {
            Files.deleteIfExists(playerFile);

            player.writeToFile();

            assertTrue(Files.exists(playerFile), "Player should create an output file using its ID"); //Checks that the file exists

            String contents = Files.readString(playerFile);

            //Checks the contents in the file match what is expected
            assertTrue(contents.contains("player 1 wins"));
            assertTrue(contents.contains("player 1 exits"));
            assertTrue(contents.contains("player 1 final hand: 1 1 1 1"));

        } finally {
            Files.deleteIfExists(playerFile);
        }
    }
    
    @Test
    void playerWinsImmediatelyWithFourIdenticalCards() {
        AtomicInteger winner = new AtomicInteger(0);

        Deck pickupDeck = new Deck(1);
        Deck discardDeck = new Deck(2);

        //A card is available to draw if the player incorrectly draws
        pickupDeck.addCard(new Card(9));

        Player player = new Player(1, winner, pickupDeck, discardDeck);

        for (int i = 0; i < 4; i++) {
            player.setHandCard(new Card(5));
        }

        player.run();

        //Checks everything is as expected
        assertEquals(1, winner.get());
        assertEquals(1, pickupDeck.size());
        assertEquals(0, discardDeck.size());
        assertEquals(4, player.getHandSize());
    }

    @Test
    void playerDoesNotDiscardPreferredDenomination() {
        int playerId = 2;

        Deck pickupDeck = new Deck(1);
        Deck discardDeck = new Deck(2);

        //The player draws this card
        pickupDeck.addCard(new Card(10));

        Player player = new Player(playerId, new AtomicInteger(0), pickupDeck, discardDeck);

        player.setHandCard(new Card(2));
        player.setHandCard(new Card(2));
        player.setHandCard(new Card(4));
        player.setHandCard(new Card(5));

        assertTrue(player.playerActions());

        //Exactly one card should have been discarded.
        Card discarded = discardDeck.takeCard();

        assertNotNull(discarded);
        assertNotEquals(playerId, discarded.getValue()); //Makes sure discarded card is not player's preferred

        //The player's hand must still contain four cards.
        assertEquals(4, player.getHandSize());
    }


    @Test
    void playerKeepsExactlyFourCardsAfterAnAction() {
        Deck pickupDeck = new Deck(1);
        Deck discardDeck = new Deck(2);

        //Give the player a card to draw during playerActions()
        pickupDeck.addCard(new Card(10));

        Player player = new Player(1, new AtomicInteger(0), pickupDeck, discardDeck);

        player.setHandCard(new Card(2));
        player.setHandCard(new Card(2));
        player.setHandCard(new Card(4));
        player.setHandCard(new Card(5));

        assertTrue(player.playerActions()); //Checks playerActions runs successfully
        assertEquals(4, player.getHandSize()); //Checks hand size remains the same
    }
}