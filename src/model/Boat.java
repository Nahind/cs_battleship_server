package model;

import java.util.ArrayList;

/**
 * Created by nahind on 24/11/16.
 */
public class Boat {

	
    ArrayList<Case> cases = new ArrayList<Case>();
    boolean sink = false;
    boolean horizontal;

    public Boat(ArrayList<Case> cases, boolean horizontal) {
        this.cases = cases;
        this.horizontal = horizontal;
    }
    
    

    public boolean isHorizontal() {
		return horizontal;
	}
    
    public String serialize() {
    	String serialization = "";
    	String orientation = (isHorizontal()) ? "horizontal" : "vertical";
    	String cases = " " + getCases().size() + " ";
    	for (Case c : getCases()) {
    		cases += c.getRow()+ "," + c.getCol() + "-";
    	}
    	serialization = orientation + cases;
    	
    	
    	return serialization;
    }


	public boolean isSink() {
        return sink;
    }

    public ArrayList<Case> getCases() {
        return cases;
    }

    public void setSink(boolean sink) {
        this.sink = sink;
    }
    
    public boolean containsField(Case field) {
    	boolean contains = false;
    	
    	for (Case c : cases) {
    		if (field == c) {
    			contains = true;
    			break;
    		}
    	}
    	
    	return contains;
    }
}
