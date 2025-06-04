public enum ActionType {
    INCOME,            // +1 coin (unblockable)
    FOREIGN_AID,       // +2 coins (blockable by DUKE)
    COUP,              // -7 coins, force player to lose card (unblockable)

    TAX,               // Claim DUKE, +3 coins
    ASSASSINATE,       // Claim ASSASSIN, -3 coins, target loses card
    STEAL,             // Claim CAPTAIN, steal 2 coins
    EXCHANGE           // Claim AMBASSADOR, swap cards
}
