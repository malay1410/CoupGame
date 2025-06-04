//public class Main {
//    public static void main(String[] args) {
//        Deck deck = new Deck();
//        System.out.println("Drawing all cards from the deck:");
//
//        try {
//            while (true) {
//                Card card = deck.draw();
//                System.out.println("- " + card);
//            }
//        } catch (Exception e) {
//            System.out.println("No more cards to draw. Deck is empty!");
//        }
//    }
//}


import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Game game = new Game(Arrays.asList("Malay", "Anish", "Vedant", "Ayush", "Syyed"));
        game.start();
    }
}
