package com.example;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameSetupTest {

    @Test
    void dealsFourCardsPerPlayerAndDeck() {
        int n = 2;
        ArrayList<Card> pack = new ArrayList<>();

        for (int i = 0; i < 8 * n; i++) {
            pack.add(new Card(i));
        }

        Main game = new Main();
        game.gameSetup(n, pack);

        assertEquals(n, game.players.length); //Checks n players created
        assertEquals(n, game.decks.length); //Checks n decks created

        int cardsInHands = 0;
        for (Player player : game.players) {
            assertEquals(4, player.getHandSize()); //Checks each player has 4 cards in hand
            cardsInHands += player.getHandSize();
        }

        int cardsInDecks = 0;
        for (Deck deck : game.decks) {
            cardsInDecks += deck.size(); //Checks each deck has 4 cards
        }

        assertEquals(8 * n, cardsInHands + cardsInDecks);
        assertEquals(4 * n, cardsInHands);
        assertEquals(4 * n, cardsInDecks);
    }
}
