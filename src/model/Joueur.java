package model;

public class Joueur {

    String name;
    Damier damier = new Damier(10);
    boolean hasWon = damier.getVictory();

    public String getName() {
        return name;
    }

    public Damier getDamier() {
        return damier;
    }

    public Joueur(String name) {
        this.name = name;

    }

}
