import java.util.*;

public class Deck {
    private final Stack<Card> cards;

    public Deck() {
        cards = new Stack<>();
        initialize();
    }

    private void initialize() {
        for (CardType type : CardType.values()) {
            for (int i = 0; i < 3; i++) { // 3 of each card
                cards.add(new Card(type));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new NoSuchElementException("The deck is empty!");
        }
        return cards.pop();
    }

    public void returnCard(Card card) {
        cards.push(card);
        shuffle(); // optional, can keep separate
    }

    public int size() {
        return cards.size();
    }
}
