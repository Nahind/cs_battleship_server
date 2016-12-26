package model;

import java.util.ArrayList;

/**
 * Created by nahind on 24/11/16.
 */
public class Configuration {

    private Configuration() {}

    private static Configuration INSTANCE = new Configuration();

    public static Configuration getInstance() {	return INSTANCE; }

    ArrayList<Integer> sizes = new ArrayList<Integer>(){
        {
            add(2);
            add(3);
            add(2);
            add(4);
            add(3);
            add(4);
        }
    };

    public ArrayList<Integer> getSizes() {
        return sizes;
    }
}

