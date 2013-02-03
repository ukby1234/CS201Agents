package restaurant;

import java.util.*;
public class Menu {

	public Map<String, Double> choices = new TreeMap<String, Double>();
	public Menu() {
		choices.put("Steak", 15.99);
		choices.put("Chicken", 10.99);
		choices.put("Salad", 5.99);
		choices.put("Pizza", 8.99);
	}
}
    
