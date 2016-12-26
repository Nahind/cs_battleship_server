package model;

import model.Case.State;

import java.util.Random;

public class Room {
	Joueur j1;
    Joueur j2;
    Damier d1;
    Damier d2;
    Random randomGenerator = new Random();
    Joueur turn;
    boolean open = true;

    public Room(Joueur j1) {
        this.j1 = j1;
        turn = j1;
    }
    
    public void addPlayer(Joueur j2) {
    	this.j2 = j2;
    	this.d1 = j1.getDamier();
    	this.d2 = j2.getDamier();
    	
    	open = false;
    }
    
    public Joueur getOpponent(Joueur j) {
    	Joueur opponent = (j == j1) ? j2 : j1;
    	return opponent;
    }
    
    public String play(Joueur j, int row, int col) {
    	String message = "";

    	if (turn == j && j2 != null) {
    		Damier targetedDamier = getOpponent(j).getDamier();
    		Case hit = targetedDamier.hit(row, col);
    		
    		if (hit != null) {
    			message = "success " + hit.getState() + " " + hit.getRow() + "," + hit.getCol();
    			if (hit.getState().equals(State.missed)) changeTurn();
    		} else {
    			message = "error already-hit"; 
    		}
    	} else message = "error turn " + turn.getName();
    	
    	return message;
    }
    
    public Joueur getRoomCreator() {
    	return j1;
    }

    public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public Joueur getJ1() {
        return j1;
    }

    public Joueur getJ2() {
        return j2;
    }

    public void changeTurn() {
        turn = (turn == j1) ? j2 : j1 ;
        Damier d = turn.getDamier();
        d.setDisabled(false);
    }

    public Case selectedByPlayer(int row, int col, Joueur j) {
        Case selection = null;
        Joueur opponent = (j == j1) ? j2 : j1;
        Damier opponentGrid = opponent.getDamier();
        selection = opponentGrid.hit(row, col);

        return selection;
    }

    public void endGame() {
        d1.setDisabled( true );
        d2.setDisabled( true );
        Joueur winner = (j1.hasWon) ? j1 : j2;
        System.out.println("Game is finished. Winner is : " + winner.getName());
    }
}
