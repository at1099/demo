package com.example;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class DeckConcurrencyTest {

    @Test
    void concurrentDrawsRetrieveEveryCardExactlyOnce()
            throws InterruptedException {

        Deck deck = new Deck(1);

        int numberOfCards = 1000;
        int numberOfThreads = 10;
        int drawsPerThread = numberOfCards / numberOfThreads;

        //Adds 1000 unique cards
        for (int i = 0; i < numberOfCards; i++) {
            deck.addCard(new Card(i));
        }

        //Thread-safe collection for recording results
        ConcurrentLinkedQueue<Integer> drawnCards = new ConcurrentLinkedQueue<>();

        CountDownLatch ready = new CountDownLatch(numberOfThreads);
        CountDownLatch start = new CountDownLatch(1);

        Thread[] threads = new Thread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                ready.countDown();

                try {
                    //Wait until all threads are ready
                    start.await();

                    for (int j = 0; j < drawsPerThread; j++) {
                        Card card = deck.takeCard();

                        if (card != null) {
                            drawnCards.add(card.getValue());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }

        //Release all threads at approximately the same time
        ready.await();
        start.countDown();

        for (Thread thread : threads) {
            thread.join();
        }

        //Every requested draw must have succeeded
        assertEquals(numberOfCards, drawnCards.size());

        //Every type of card must have been drawn once
        Set<Integer> uniqueValues = new HashSet<>(drawnCards);
        assertEquals(numberOfCards, uniqueValues.size());

        //Check that the deck is now empty
        assertNull(deck.takeCard());
    }

    
    @Test
    void concurrentAddsAndDrawsKeepEveryCard() throws InterruptedException {
        Deck deck = new Deck(1);

        int numberOfThreads = 10; //10 threads adding cards and 10 drawing
        int cardsPerThread = 100; //Each drawer takes 100 cards and each creator produces 100 cards
        int totalCards = numberOfThreads * cardsPerThread;

        ConcurrentLinkedQueue<Integer> drawnCards = new ConcurrentLinkedQueue<>(); //Stores values of cards drawn by creator threads

        CountDownLatch ready = new CountDownLatch(numberOfThreads * 2);
        CountDownLatch start = new CountDownLatch(1); //Threads can wait for a particular event

        Thread[] threads = new Thread[numberOfThreads * 2];

        //Half the threads add cards; half draw cards
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;

            //Creator thread code
            threads[i] = new Thread(() -> {
                ready.countDown();

                try {
                    start.await();

                    for (int j = 0; j < cardsPerThread; j++) { //Adds the 100 cards to the deck
                        int value = threadId * cardsPerThread + j;
                        deck.addCard(new Card(value));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            //Taker thread code
            threads[numberOfThreads + i] = new Thread(() -> {
                ready.countDown();

                try {
                    start.await();

                    int successfulDraws = 0;

                    while (successfulDraws < cardsPerThread) { //Draws 100 cards
                        Card card = deck.takeCard();

                        if (card != null) { //If card exist the thread safely stores the contents for later comparison
                            drawnCards.add(card.getValue());
                            successfulDraws++;
                        } else {
                            Thread.yield(); //Lets another thread run because the deck is empty
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        //Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        //Wait until all threads are ready, then release them
        ready.await();
        start.countDown();

        //Wait for all producers and consumers to finish
        for (Thread thread : threads) {
            thread.join();
        }

        //Every card should have been drawn exactly once
        assertEquals(totalCards, drawnCards.size());

        Set<Integer> uniqueValues = new HashSet<>(drawnCards);
        assertEquals(totalCards, uniqueValues.size());

        //All cards have been removed
        assertNull(deck.takeCard());
    }

}

