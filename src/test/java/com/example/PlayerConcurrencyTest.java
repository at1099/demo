
package com.example;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PlayerConcurrencyTest {

    @Test
    void concurrentPlayerActionsPreserveCardsAndMaintainFourCardHands() throws Exception {

        AtomicInteger winner = new AtomicInteger(0); //Creates shared atomic variable

        Deck deck1 = new Deck(1);
        Deck deck2 = new Deck(2);

        //Gives each deck 100 unique cards
        for (int i = 0; i < 100; i++) {
            deck1.addCard(new Card(100 + i));
            deck2.addCard(new Card(300 + i));
        }

        //Create the players
        Player player1 = new Player(1, winner, deck1, deck2);
        Player player2 = new Player(2, winner, deck2, deck1);

        //Use unique values that do not overlap with either deck
        for (int i = 0; i < 4; i++) {
            player1.setHandCard(new Card(1000 + i));
            player2.setHandCard(new Card(2000 + i));
        }

        int actionsPerPlayer = 50; //Each player will perform 50 actions (to ensure one thread doesnt do more than another)

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            //Run both players concurrently
            Future<?> task1 = executor.submit(() -> { //Future lets the test wait for completion and detect exceptions thrown inside the task
                for (int i = 0; i < actionsPerPlayer; i++) {
                    if (!player1.playerActions()) { //Check player 1's action succeeds
                        throw new AssertionError("Player 1 action failed");
                    }

                    if (player1.getHandSize() != 4) { //Checks each player has 4 cards in hand
                        throw new AssertionError("Player 1 does not have four cards");
                    }
                }
            });

            //Repeats all for player 2
            Future<?> task2 = executor.submit(() -> {
                for (int i = 0; i < actionsPerPlayer; i++) {
                    if (!player2.playerActions()) {
                        throw new AssertionError(
                                "Player 2 action failed");
                    }

                    if (player2.getHandSize() != 4) {
                        throw new AssertionError(
                                "Player 2 does not have four cards");
                    }
                }
            });

            //Fail if either task ends or throws an exception
            task1.get(5, TimeUnit.SECONDS);
            task2.get(5, TimeUnit.SECONDS);

        } finally { //Tries to close threads even if test fails
            executor.shutdownNow();
        }

        //Tests if each player still has 4 cards in their hand
        assertEquals(4, player1.getHandSize());
        assertEquals(4, player2.getHandSize());

        //Collect every card still in the players' hands
        Set<Integer> actualCards = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            assertTrue(actualCards.add(
                    player1.getCard(i).getValue()),
                    "Duplicate card found");
            assertTrue(actualCards.add(
                    player2.getCard(i).getValue()),
                    "Duplicate card found");
        }

        //Collect every card still in the decks
        Card card;
        while ((card = deck1.takeCard()) != null) {
            assertTrue(actualCards.add(card.getValue()),
                    "Duplicate card found");
        }

        while ((card = deck2.takeCard()) != null) {
            assertTrue(actualCards.add(card.getValue()),
                    "Duplicate card found");
        }

        //Reconstruct the set of all 208 cards originally dealt
        Set<Integer> expectedCards = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            expectedCards.add(100 + i);
            expectedCards.add(300 + i);
        }

        for (int i = 0; i < 4; i++) {
            expectedCards.add(1000 + i);
            expectedCards.add(2000 + i);
        }

        //Checks the cards that exist now still match the original card dealt out
        assertEquals(expectedCards, actualCards);
        assertEquals(208, actualCards.size());

        //Checks both decks are empty
        assertNull(deck1.takeCard());
        assertNull(deck2.takeCard());
    }
}
