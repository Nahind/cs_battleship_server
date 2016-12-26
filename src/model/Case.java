package model;

/**
 * Created by nahind on 23/11/16.
 */
public class Case {

    int row;
    int col;

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    Boolean boat = false;
    State state = State.empty;

    public enum State {
        empty,
        missed,
        hit,
    }

    public Case(int row, int y) {
        this.row = row;
        this.col = y;
    }

    public Boolean isBoat() {
        return boat;
    }

    public void setBoat(Boolean boat) {
        this.boat = boat;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
