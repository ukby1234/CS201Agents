package restaurant;

import agent.Agent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;
import restaurant.layoutGUI.*;
import java.awt.Color;


/** Cook agent for restaurant.
 *  Keeps a list of orders for waiters
 *  and simulates cooking them.
 *  Interacts with waiters only.
 */
public class CookAgent extends Agent {

    //List of all the orders
    private List<Order> orders = new ArrayList<Order>();
    private Map<String,FoodData> inventory = new HashMap<String,FoodData>();
    private List<InventoryOrder> invords = new ArrayList<InventoryOrder>();
    private List<MarketAgent> agents = new ArrayList<MarketAgent>();
    private final int surplus = 10;
    private int marketPos = 0;
    public enum InventoryStatus {Pending, Ordered, Received, Done};
    public enum Status {pending, cooking, done}; // order status

    //Name of the cook
    private String name;

    //Timer for simulation
    Timer timer = new Timer();
    Restaurant restaurant; //Gui layout

    /** Constructor for CookAgent class
     * @param name name of the cook
     */
    public CookAgent(String name, Restaurant restaurant) {
	super();

	this.name = name;
	this.restaurant = restaurant;
	//Create the restaurant's inventory.
	inventory.put("Steak",new FoodData("Steak", 5, 5, 5));
	inventory.put("Chicken",new FoodData("Chicken", 4, 5, 5));
	inventory.put("Pizza",new FoodData("Pizza", 3, 5, 5));
	inventory.put("Salad",new FoodData("Salad", 2, 5, 5));
    }
    /** Private class to store information about food.
     *  Contains the food type, its cooking time, and ...
     */
    private class FoodData {
	String type; //kind of food
	double cookTime;
	int amount;
	int limit;
	// other things ...
	
	public FoodData(String type, double cookTime, int amount, int limit){
	    this.type = type;
	    this.cookTime = cookTime;
	    this.amount = amount;
	    this.limit = limit;
	}
    }
    /** Private class to store order information.
     *  Contains the waiter, table number, food item,
     *  cooktime and status.
     */
    private class Order {
	public WaiterAgent waiter;
	public int tableNum;
	public String choice;
	public Status status;
	public Food food; //a gui variable

	/** Constructor for Order class 
	 * @param waiter waiter that this order belongs to
	 * @param tableNum identification number for the table
	 * @param choice type of food to be cooked 
	 */
	public Order(WaiterAgent waiter, int tableNum, String choice){
	    this.waiter = waiter;
	    this.choice = choice;
	    this.tableNum = tableNum;
	    this.status = Status.pending;
	}
	

	/** Represents the object as a string */
	public String toString(){
	    return choice + " for " + waiter ;
	}
    }
    
    private class InventoryOrder {
		String type;
		MarketAgent market;
		InventoryStatus status;
		int amount;
		public InventoryOrder(String type, MarketAgent market, InventoryStatus status, int amount) {
			this.type = type;
			this.market = market;
			this.status = status;
			this.amount = amount;
		}
	}

    


    // *** MESSAGES ***

    /** Message from a waiter giving the cook a new order.
     * @param waiter waiter that the order belongs to
     * @param tableNum identification number for the table
     * @param choice type of food to be cooked
     */
    public void msgHereIsAnOrder(WaiterAgent waiter, int tableNum, String choice){
	orders.add(new Order(waiter, tableNum, choice));
	stateChanged();
    }
    
    public void msgDelivery(String type, int amount) {
    	if (amount == 0)
    		print(type + " deliver fails");
    	else
    		print(String.format("%s delivers %d", type, amount));
    	for (InventoryOrder o : invords) {
    		if (o.type.equals(type) && o.status == InventoryStatus.Ordered) {
    			o.status = InventoryStatus.Received;
    			o.amount = amount;
    		}
    	}
    	stateChanged();
    }


    /** Scheduler.  Determine what action is called for, and do it. */
    protected boolean pickAndExecuteAnAction() {
	
	for (String type : inventory.keySet()) {
		FoodData fd = inventory.get(type);
		boolean flag = true;
		for (InventoryOrder o : invords) {
			if (o.type.equals(fd.type) && o.status != InventoryStatus.Done)
				flag = false;
			
		}
		if (fd.amount < fd.limit && flag) {
			orderMore(fd);
			return true;
		}
	}
	for (InventoryOrder o : invords) {
		if (o.status == InventoryStatus.Pending) {
			requestMarket(o);
			return true;
		}
		if (o.status == InventoryStatus.Received) {
			addToInventory(o);
			return true;
		}
	}
    //If there exists an order o whose status is done, place o.
	for(Order o:orders){
	    if(o.status == Status.done){
		placeOrder(o);
		return true;
	    }
	}
	//If there exists an order o whose status is pending, cook o.
	for(Order o:orders){
	    if(o.status == Status.pending){
		cookOrder(o);
		return true;
	    }
	}

	//we have tried all our rules (in this case only one) and found
	//nothing to do. So return false to main loop of abstract agent
	//and wait.
	return false;
    }
    

    // *** ACTIONS ***
    
    /** Starts a timer for the order that needs to be cooked. 
     * @param order
     */
    private void cookOrder(Order order){
	inventory.get(order.choice).amount--;
    DoCooking(order);
	order.status = Status.cooking;
    }

    private void placeOrder(Order order){
	DoPlacement(order);
	order.waiter.msgOrderIsReady(order.tableNum, order.food);
	orders.remove(order);
    }

    private void orderMore(FoodData fd) {
    	print("called");
    	invords.add(new InventoryOrder(fd.type, agents.get(marketPos), InventoryStatus.Pending, fd.limit - fd.amount + surplus));
    	stateChanged();
    }
    
    private void requestMarket(InventoryOrder o) {
    	//print("called");
    	o.status = InventoryStatus.Ordered;
    	o.market.msgOrder(this, o.type, o.amount);
    	stateChanged();
    }
    
    private void addToInventory(InventoryOrder o) {
    	if (o.amount == 0) {
    		o.status = InventoryStatus.Done;
    		marketPos = (marketPos + 1) % agents.size();
    	}
    	else {
    		inventory.get(o.type).amount += o.amount;
    		print(String.format("%s is now %d", o.type, inventory.get(o.type).amount));
    		o.status = InventoryStatus.Done;
    	}
    	stateChanged();
    }

    // *** EXTRA -- all the simulation routines***

    /** Returns the name of the cook */
    public String getName(){
        return name;
    }
    
    public void addMarket(MarketAgent market) {
    	agents.add(market);
    }
    private void DoCooking(final Order order){
	print("Cooking:" + order + " for table:" + (order.tableNum+1));
	//put it on the grill. gui stuff
	order.food = new Food(order.choice.substring(0,2),new Color(0,255,255), restaurant);
	order.food.cookFood();

	timer.schedule(new TimerTask(){
	    public void run(){//this routine is like a message reception    
		order.status = Status.done;
		stateChanged();
	    }
	}, (int)(inventory.get(order.choice).cookTime*1000));
    }
    public void DoPlacement(Order order){
	print("Order finished: " + order + " for table:" + (order.tableNum+1));
	order.food.placeOnCounter();
    }
}


    
