package model;

import model.Case.State;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

import static model.Case.State.empty;

/**
 * Created by nahind on 23/11/16.
 */
public class Damier extends Observable {

	private int size;
	private ArrayList<Case> cases = new ArrayList<Case>();
	private ArrayList<Boat> boats = new ArrayList<Boat>();
	boolean victory = false;
	boolean disabled = true;

	public Damier(int size) {
		this.size = size;
		init(size);
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public ArrayList<Boat> getBoats() {
		return boats;
	}

	public boolean getVictory() {
		return victory;
	}

	public boolean isWin() {
		return victory;
	}

	public int getSize() {
		return size;
	}

	private void init(int size) {
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				cases.add(new Case(x, y));
			}
		}
	}

	public ArrayList<Case> getCases() {
		return cases;
	}

	private Case getCase(int row, int col) {
		Case selected = null;
		int index = row * size + col;

		try {
			selected = cases.get(index);
		} catch (Exception e) {
		}

		return selected;
	}

	public String serializeBoats() {
		String bString = "";

		for (Boat b : boats) {
			for (Case c : b.getCases()) {
				bString += c.col + "," + c.row + "-";
			}
		}

		return bString;
	}

	private boolean addBoat(int rowStart, int colStart, boolean horizontal, int length) {
		ArrayList<Case> boatCases = new ArrayList<>();
		int indexStart = (horizontal) ? colStart : rowStart;
		boolean success = true;

		for (int i = indexStart; i < indexStart + length; i++) {
			Case boatCase = (horizontal) ? getCase(rowStart, i) : getCase(i, colStart);

			if (boatCase != null) {
				if (boatCase.isBoat() || hasNeighbours(boatCase)) {
					success = false;
					break;
				} else
					boatCases.add(boatCase);
			}

			else {
				success = false;
				break;
			}
		}

		if (success && !isOut(boatCases, horizontal)) {
			for (Case c : boatCases)
				c.setBoat(true);
			boats.add(new Boat(boatCases, horizontal));
		} else
			success = false;

		return success;
	}
	
	private boolean hasNeighbours(Case c) {
		boolean neighbour = false;
		int caseRow = c.getRow();
		int caseCol = c.getCol();
		print();
		
		for (int i = -1; i<=1; i++) {
			for (int j = -1; j<=1; j++) {
				
				if(getCase(caseRow + i, caseCol + j) != null) {
					Case neighbourCase =  getCase(caseRow + i, caseCol + j);
					
					if (i != 0 && j != 0 && neighbourCase.isBoat()) {
						System.out.println("has neighbourg : " + caseRow + "," + caseCol);
						neighbour = true;
						break;
					}
				}
			}
		}
		
		return neighbour;
	}

	private boolean isOut(ArrayList<Case> boatCases, boolean horizontal) {
		boolean out = false;
		int invarient = (horizontal) ? boatCases.get(0).row : boatCases.get(0).col;

		for (Case c : boatCases) {
			if (horizontal && c.row != invarient)
				out = true;
			if (!horizontal && c.col != invarient)
				out = true;
		}
		return out;
	}

	public void drawBoats(ArrayList<Integer> sizes) {
		Random randomGenerator = new Random();

		for (int size : sizes) {
			boolean success = false;

			while (!success) {
				int x = randomGenerator.nextInt(this.size);
				int y = randomGenerator.nextInt(this.size);
				boolean horizontal = randomGenerator.nextBoolean();

				success = addBoat(x, y, horizontal, size);
			}
		}
	}

	public void drawBoats() {
		drawBoats(Configuration.getInstance().sizes);
	}

	public Case hit(int row, int col) {

		Case selected = null;

		selected = getCase(row, col);

		if (selected.getState() == empty) {
			if (selected.isBoat()) {
				selected.setState(State.hit);
			} else
				selected.setState(State.missed);

		} else {
			selected = null;
			System.out.println("It is not your turn");
		}
		print();

		return selected;
	}

	// public Case hit(int row, int col) {
	//
	// Case selected = null;
	//
	// if (!disabled) {
	// selected = getCase(row, col);
	//
	// if (selected.getState() == empty) {
	// if (selected.isBoat()) {
	// selected.setState(State.hit);
	// }
	// else selected.setState(State.missed);
	//
	// changePlayer();
	// } else {
	// selected = null;
	// System.out.println("It is not your turn");
	// }
	//
	// victory = checkVictory();
	// if (victory) {
	// System.out.println("notifying observers");
	// notifyObservers("end");
	// }
	//
	// } else {
	// System.out.println("It is not your turn");
	// }
	//
	// return selected;
	// }

	public void play(int x, int y) {
		if (!victory) {
			Case selected = getCase(x, y);

			if (selected.getState() == empty) {
				if (selected.isBoat()) {
					selected.setState(State.hit);
				} else
					selected.setState(State.missed);
			}

			victory = checkVictory();
			if (victory)
				System.out.println("Game is finished");
		}
	}
	
	public Boat hasSunk(int row, int col) {
		Case hitCase = getCase(row, col);
		System.out.println("state = " + hitCase.getState());
		Boat sunkBoat = null;
		
		for (Boat b : boats) {
			if (b.containsField(hitCase) && b.sink) {
				sunkBoat = b;
				break;
			}
		}
		
		return sunkBoat;
	}

	public boolean checkVictory() {
		boolean victory = false;
		int boatCount = boats.size();
		int sinkBoatCount = 0;
		for (Boat boat : boats) {

			if (!boat.isSink()) {
				int casesCount = boat.cases.size();
				int hitCasesCount = 0;
				for (Case c : boat.cases) {
					if (c.getState() == State.hit)
						hitCasesCount++;
				}
				if (hitCasesCount == casesCount) {
					boat.setSink(true);
					sinkBoatCount++;
					System.out.println("boat has sunk");
				}

			} else
				sinkBoatCount++;
		}

		System.out.println("sink = " + sinkBoatCount + ", boats = " + boatCount);
		if (sinkBoatCount == boatCount)
			victory = true;

		return victory;
	}

	public void print() {
		String empty = "[ ]";
		String missed = "[-]";
		String boat = "[0]";
		String hit = "[X]";
		String damier = "";
		String design;
		ArrayList<Case> cases = getCases();

		for (int i = 0; i < cases.size(); i++) {
			design = empty;
			Case currentCase = cases.get(i);

			if (currentCase.isBoat()) {
				design = boat;
			}

			switch (currentCase.getState()) {
			case empty:
				break;
			case missed:
				design = missed;
				break;
			case hit:
				design = hit;
				break;
			}

			if (i % getSize() == 0) {
				damier += "\n" + design;
			} else {
				damier += design;
			}
		}

		System.out.print(damier);
	}

}
