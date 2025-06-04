import java.util.*;

public class Player {
    private final String name;
    private int coins;
    private final List<Card> hand;

    public Player(String name, Deck deck) {
        this.name = name;
        this.coins = 2;
        this.hand = new ArrayList<>();
        drawInitialCards(deck);
    }

    private void drawInitialCards(Deck deck) {
        hand.add(deck.draw());
        hand.add(deck.draw());
    }

    public String getName() {
        return name;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public void deductCoins(int amount) {
        coins -= amount;
        if (coins < 0) coins = 0;
    }

    public boolean isAlive() {
        return hand.size() > 0;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void loseCard(Scanner scanner) {
        if (hand.isEmpty()) return;

        System.out.println(name + ", you must reveal and lose one of your cards:");
        for (int i = 0; i < hand.size(); i++) {
            System.out.println((i + 1) + ". " + hand.get(i));
        }

        int choice;
        do {
            System.out.print("Enter the number of the card to lose: ");
            while (!scanner.hasNextInt()) {
                scanner.next(); // clear invalid input
                System.out.print("Please enter a valid number: ");
            }
            choice = scanner.nextInt();
        } while (choice < 1 || choice > hand.size());

        Card lostCard = hand.remove(choice - 1);
        System.out.println(name + " has revealed and lost: " + lostCard);
    }

    public void showHand() {
        System.out.println(name + "'s cards: " + hand);
    }
}
