package com.example;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void cardsAreDrawnInFifoOrder() {
        Deck deck = new Deck(1);

        deck.addCard(new Card(3));
        deck.addCard(new Card(7));
        deck.addCard(new Card(2));

        assertEquals(3, deck.takeCard().getValue()); //check that queue is FIFO
        assertEquals(7, deck.takeCard().getValue()); //make sure this works for the entire queue
        assertEquals(2, deck.takeCard().getValue());
    }

    @Test
    void addingCardPlacesItAtBack() {
        Deck deck = new Deck(1);

        deck.addCard(new Card(4));
        deck.addCard(new Card(8));

        assertEquals(4, deck.takeCard().getValue()); //Checks added card is at the back

        deck.addCard(new Card(9));

        assertEquals(8, deck.takeCard().getValue());
        assertEquals(9, deck.takeCard().getValue());
    }

    @Test
    void drawingFromEmptyDeckReturnsNull() {
        Deck deck = new Deck(1);

        assertNull(deck.takeCard()); //Checks that taking from empty deck returns null
        assertNull(deck.takeCard());
    }
}
