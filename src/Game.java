import java.util.*;

public class Game {
    private final List<Player> players;
    private final Deck deck;
    private int currentPlayerIndex;
    private final Scanner scanner;

    public Game(List<String> playerNames) {
        this.deck = new Deck();
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.scanner = new Scanner(System.in);
        initializePlayers(playerNames);
    }

    private void initializePlayers(List<String> playerNames) {
        for (String name : playerNames) {
            players.add(new Player(name, deck));
        }
    }

    public void start() {
        System.out.println("🎮 Coup Game Started!");
        while (!isGameOver()) {
            Player currentPlayer = players.get(currentPlayerIndex);
            if (currentPlayer.isAlive()) {
                System.out.println("\n🔁 It's " + currentPlayer.getName() + "'s turn.");
                showCoins();
                takeTurn(currentPlayer);
            }
            nextTurn();
        }

        Player winner = players.stream().filter(Player::isAlive).findFirst().orElse(null);
        System.out.println("\n🏆 Game Over! The winner is: " + (winner != null ? winner.getName() : "No one"));
    }

    private void takeTurn(Player player) {
        System.out.println(player.getName() + ", choose your action:");
        System.out.println("1. Income");
        System.out.println("2. Foreign Aid");
        System.out.println("3. Coup");
        System.out.println("4. Tax (Claim Duke)");
        System.out.println("5. Assassinate (Claim Assassin)");
        System.out.println("6. Steal (Claim Captain)");
        System.out.println("7. Exchange (Claim Ambassador)");

        int choice = getUserChoice(1, 7);
        ActionType action = ActionType.values()[choice - 1];

        switch (action) {
            case INCOME -> {
                System.out.println(player.getName() + " takes 1 coin.");
                player.addCoins(1);
            }

            case FOREIGN_AID -> {
                System.out.println(player.getName() + " attempts to take 2 coins (Foreign Aid).");
                boolean blocked = false;
                for (Player p : players) {
                    if (!p.equals(player) && p.isAlive()) {
                        if (handleBlock(p, CardType.DUKE)) {
                            System.out.println("Foreign Aid blocked by Duke!");
                            blocked = true;
                            break;
                        }
                    }
                }
                if (!blocked) {
                    player.addCoins(2);
                }
            }

            case COUP -> {
                if (player.getCoins() < 7) {
                    System.out.println("Not enough coins to Coup.");
                } else {
                    Player target = chooseTarget(player);
                    if (target != null) {
                        System.out.println(player.getName() + " coups " + target.getName());
                        player.deductCoins(7);
                        target.loseCard(scanner);
                    }
                }
            }

            case TAX -> {
                System.out.println(player.getName() + " claims DUKE to take 3 coins.");
                if (handleChallenge(player, CardType.DUKE)) {
                    player.addCoins(3);
                }
            }

            case ASSASSINATE -> {
                if (player.getCoins() < 3) {
                    System.out.println("Not enough coins to assassinate.");
                    break;
                }
                Player target = chooseTarget(player);
                if (target == null) break;

                System.out.println(player.getName() + " claims ASSASSIN to assassinate " + target.getName());
                if (handleChallenge(player, CardType.ASSASSIN)) {
                    if (handleBlock(target, CardType.CONTESSA)) {
                        System.out.println("Assassination blocked by Contessa!");
                        return;
                    }
                    player.deductCoins(3);
                    target.loseCard(scanner);
                }
            }

            case STEAL -> {
                Player target = chooseTarget(player);
                if (target == null) break;

                System.out.println(player.getName() + " claims CAPTAIN to steal from " + target.getName());
                if (handleChallenge(player, CardType.CAPTAIN)) {
                    if (handleBlock(target, CardType.CAPTAIN, CardType.AMBASSADOR)) {
                        System.out.println("Steal blocked!");
                        return;
                    }
                    int stolen = Math.min(2, target.getCoins());
                    target.deductCoins(stolen);
                    player.addCoins(stolen);
                    System.out.println(player.getName() + " stole " + stolen + " coin(s).");
                }
            }

            case EXCHANGE -> {
                System.out.println(player.getName() + " claims AMBASSADOR to exchange cards.");
                if (handleChallenge(player, CardType.AMBASSADOR)) {
                    List<Card> pool = new ArrayList<>(player.getHand());
                    pool.add(deck.draw());
                    pool.add(deck.draw());

                    System.out.println("Choose 2 cards to KEEP:");
                    for (int i = 0; i < pool.size(); i++) {
                        System.out.println((i + 1) + ". " + pool.get(i));
                    }

                    Set<Integer> choices = new HashSet<>();
                    while (choices.size() < 2) {
                        int c = getUserChoice(1, pool.size());
                        choices.add(c - 1);
                    }

                    List<Card> newHand = new ArrayList<>();
                    for (int i : choices) {
                        newHand.add(pool.get(i));
                    }

                    // Return unused cards to deck
                    for (int i = 0; i < pool.size(); i++) {
                        if (!choices.contains(i)) {
                            deck.returnCard(pool.get(i));
                        }
                    }

                    player.getHand().clear();
                    player.getHand().addAll(newHand);
                    System.out.println(player.getName() + " finished exchanging cards.");
                }
            }
        }
    }

    private boolean handleChallenge(Player claimer, CardType claimedRole) {
        for (Player p : players) {
            if (!p.equals(claimer) && p.isAlive()) {
                System.out.println(p.getName() + ", do you want to challenge " + claimer.getName() + "'s claim of " + claimedRole + "? (y/n)");
                String input = scanner.next().trim().toLowerCase();
                if (input.equals("y")) {
                    boolean hasCard = claimer.getHand().stream().anyMatch(card -> card.getType() == claimedRole);

                    if (hasCard) {
                        System.out.println(claimer.getName() + "successfully proves their claim with a " + claimedRole + "!");
                        // Reveal and replace card
                        Card shown = null;
                        for (Card card : claimer.getHand()) {
                            if (card.getType() == claimedRole) {
                                shown = card;
                                break;
                            }
                        }
                        claimer.getHand().remove(shown);
                        deck.returnCard(shown);
                        claimer.getHand().add(deck.draw());

                        p.loseCard(scanner);
                        return true;
                    } else {
                        System.out.println(claimer.getName() + "was bluffing and loses a card!");
                        claimer.loseCard(scanner);
                        return false;
                    }
                }
            }
        }
        return true; // no challenge
    }

    private Player chooseTarget(Player currentPlayer) {
        List<Player> targets = new ArrayList<>();
        for (Player p : players) {
            if (!p.equals(currentPlayer) && p.isAlive()) {
                targets.add(p);
            }
        }

        if (targets.isEmpty()) {
            System.out.println("No valid targets available.");
            return null;
        }

        System.out.println("Choose a player to target:");
        for (int i = 0; i < targets.size(); i++) {
            System.out.println((i + 1) + ". " + targets.get(i).getName());
        }

        int choice = getUserChoice(1, targets.size());
        return targets.get(choice - 1);
    }

    private void showCoins() {
        for (Player p : players) {
            if (p.isAlive()) {
                System.out.println("- " + p.getName() + ": " + p.getCoins() + " coins");
            }
        }
    }

    private int getUserChoice(int min, int max) {
        int choice;
        do {
            System.out.print("Enter choice (" + min + "-" + max + "): ");
            while (!scanner.hasNextInt()) {
                scanner.next();
                System.out.print("Enter a valid number: ");
            }
            choice = scanner.nextInt();
        } while (choice < min || choice > max);
        return choice;
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private boolean handleBlock(Player target, CardType... validBlockers) {
        System.out.println(target.getName() + ", do you want to block? (y/n)");
        String input = scanner.next().trim().toLowerCase();
        if (!input.equals("y")) return false;

        // Choose blocker
        System.out.println("Choose a blocking role:");
        for (int i = 0; i < validBlockers.length; i++) {
            System.out.println((i + 1) + ". " + validBlockers[i]);
        }
        int blockerChoice = getUserChoice(1, validBlockers.length);
        CardType blocker = validBlockers[blockerChoice - 1];

        // Offer all players a chance to challenge this block
        for (Player p : players) {
            if (!p.equals(target) && p.isAlive()) {
                System.out.println(p.getName() + ", do you want to challenge " + target.getName() + "'s block with " + blocker + "? (y/n)");
                String c = scanner.next().trim().toLowerCase();
                if (c.equals("y")) {
                    boolean hasBlocker = target.getHand().stream().anyMatch(card -> card.getType() == blocker);

                    if (hasBlocker) {
                        System.out.println(target.getName() + " proves the block with a " + blocker + "!");
                        // Refresh card
                        Card shown = null;
                        for (Card card : target.getHand()) {
                            if (card.getType() == blocker) {
                                shown = card;
                                break;
                            }
                        }
                        target.getHand().remove(shown);
                        deck.returnCard(shown);
                        target.getHand().add(deck.draw());

                        p.loseCard(scanner);
                        return true; // block successful
                    } else {
                        System.out.println(target.getName() + " was bluffing and loses a card!");
                        target.loseCard(scanner);
                        return false; // block failed
                    }
                }
            }
        }

        return true; // block unchallenged
    }

    private boolean isGameOver() {
        long aliveCount = players.stream().filter(Player::isAlive).count();
        return aliveCount <= 1;
    }
}
