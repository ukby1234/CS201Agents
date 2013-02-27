package restaurant;

import agent.Agent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;
import restaurant.layoutGUI.*;
import java.awt.Color;
import restaurant.interfaces.*;


/** Cook agent for restaurant.
 *  Keeps a list of orders for waiters
 *  and simulates cooking them.
 *  Interacts with waiters only.
 */
public class CookAgent extends Agent implements Cook{

	//List of all the orders
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	private List<ChangeOrder> changeOrders = Collections.synchronizedList(new ArrayList<ChangeOrder>());
	private Map<String,FoodData> inventory = Collections.synchronizedMap(new HashMap<String,FoodData>());
	private List<InventoryOrder> invords = Collections.synchronizedList(new ArrayList<InventoryOrder>());
	private List<Market> agents = Collections.synchronizedList(new ArrayList<Market>());
	private final int surplus = 10;
	public enum InventoryStatus {Pending, Ordered, Received, Done};
	public enum Status {pending, waiting, cooking, done}; // order status
	private boolean waiting = false;
	private boolean isUnderLimit = false;
	private boolean isRunOutOfFood = false;
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
		inventory.put("Steak",new FoodData("Steak", 5, 10, 5));
		inventory.put("Chicken",new FoodData("Chicken", 4, 10, 5));
		inventory.put("Pizza",new FoodData("Pizza", 3, 10, 5));
		inventory.put("Salad",new FoodData("Salad", 2, 10, 5));
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
		public Waiter waiter;
		public int tableNum;
		public String choice;
		public Status status;
		public Food food; //a gui variable
		public Timer t;

		/** Constructor for Order class 
		 * @param waiter waiter that this order belongs to
		 * @param tableNum identification number for the table
		 * @param choice type of food to be cooked 
		 */
		public Order(Waiter waiter, int tableNum, String choice){
			this.waiter = waiter;
			this.choice = choice;
			this.tableNum = tableNum;
			this.status = Status.pending;
			t = new Timer();
		}


		/** Represents the object as a string */
		public String toString(){
			return choice + " for " + waiter ;
		}
	}

	private class ChangeOrder {
		public Waiter waiter;
		public int tableNum;
		public String choice;
		public boolean decision;
		public ChangeOrder(Waiter waiter, int tableNum, String choice) {
			this.waiter = waiter;
			this.tableNum = tableNum;
			this.choice = choice;
			this.decision = false;
		}
	}

	private class InventoryOrder {
		public String type;
		public Market market;
		public InventoryStatus status;
		public int amount;
		public InventoryOrder(String type, Market market, InventoryStatus status, int amount) {
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
	public void msgHereIsAnOrder(Waiter waiter, int tableNum, String choice){
		synchronized (orders) {
			for (Order o : orders) 
				if (o.tableNum == tableNum) {
					print("found");
					o.waiter = waiter;
					o.choice = choice;
					o.status = Status.pending;
					stateChanged();
					return;
				}
		}
		orders.add(new Order(waiter, tableNum, choice));
		stateChanged();
	}

	public void msgDelivery(String type, int amount) {
		if (amount == 0)
			print(type + " deliver fails");
		else
			print(String.format("%s delivers %d", type, amount));
		synchronized (invords) {
			for (InventoryOrder o : invords) {
				if (o.type.equals(type) && o.status == InventoryStatus.Ordered) {
					o.status = InventoryStatus.Received;
					o.amount = amount;
				}
			}
		}
		stateChanged();
	}

	public void msgchangeOrder(Waiter waiter, int tableNum, String choice) {
		print(String.format("Cook received %d change order to %s", tableNum, choice));
		changeOrders.add(new ChangeOrder(waiter, tableNum, choice));
		stateChanged();
	}


	/** Scheduler.  Determine what action is called for, and do it. */
	protected boolean pickAndExecuteAnAction() {
		try {
			if (!changeOrders.isEmpty()) {
				//print("Here");
				ChangeOrder co = changeOrders.remove(0);
				for (Order o : orders) {
					if (o.tableNum == co.tableNum) {
						if(o.status == Status.pending || o.status == Status.waiting) {
							//print("Here");
							co.decision = true;
						}
					}
					changeOrder(o, co);
				}
				return true;
			}
			for (Order o : orders) {
				if (inventory.get(o.choice).amount <= 0 && o.status == Status.pending) {
					runOutOfFood(o);
					return true;
				}
			}

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
			for(final Order o:orders){
				if(o.status == Status.pending){
					if(waiting) {
						print("Waiting 5000 milliseconds");
						o.status = Status.waiting;
						o.t.schedule(new TimerTask() {
							public void run() {
								cookOrder(o);
							}
						}, 5000);
					}else
						cookOrder(o);
					return true;
				}
			}
		}catch (ConcurrentModificationException e) {return true;}

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
		print(String.format("Now I have %s %d", order.choice, inventory.get(order.choice).amount));
		DoCooking(order);
		order.status = Status.cooking;
		stateChanged();
	}

	private void placeOrder(Order order){
		DoPlacement(order);
		order.waiter.msgOrderIsReady(order.tableNum, order.food);
		orders.remove(order);
		stateChanged();
	}

	private void orderMore(FoodData fd) {
		print("I have to order more raw food for " + fd.type);
		invords.add(new InventoryOrder(fd.type, agents.get(0), InventoryStatus.Pending, fd.limit - fd.amount + surplus));
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
			agents.remove(o.market);
			o.status = InventoryStatus.Done;
		}
		else {
			inventory.get(o.type).amount += o.amount;
			print(String.format("%s is now %d", o.type, inventory.get(o.type).amount));
			o.status = InventoryStatus.Done;
		}
		stateChanged();
	}

	private void runOutOfFood(Order o) {
		print(String.format("Running Out Of Food for %s", o.choice));
		o.waiter.msgCookReorder(o.tableNum);
		//orders.remove(o);
		o.status = Status.waiting;
		stateChanged();
	}

	private void changeOrder(Order o, ChangeOrder co) {
		if(co.decision) {
			print(String.format("Change order for %d", co.tableNum));
			o.choice = co.choice;
			o.status = Status.pending;
			o.t.cancel();
			o.t = new Timer();
		} else
			print(String.format("Not change order for %d", co.tableNum));
		co.waiter.msgDecisionChangeOrder(co.tableNum, co.decision);
		stateChanged();

	}
	// *** EXTRA -- all the simulation routines***

	/** Returns the name of the cook */
	public String getName(){
		return name;
	}

	public void addMarket(Market market) {
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

	public boolean getWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public boolean getUnderLimit() {
		return isUnderLimit;
	}

	public void setUnderLimit(boolean limit) {
		isUnderLimit = limit;
		if (limit) {
			inventory.clear();
			inventory.put("Steak",new FoodData("Steak", 5, 4, 5));
			inventory.put("Chicken",new FoodData("Chicken", 4, 4, 5));
			inventory.put("Pizza",new FoodData("Pizza", 3, 4, 5));
			inventory.put("Salad",new FoodData("Salad", 2, 4, 5));
		}else {
			inventory.clear();
			inventory.put("Steak",new FoodData("Steak", 5, 10, 5));
			inventory.put("Chicken",new FoodData("Chicken", 4, 10, 5));
			inventory.put("Pizza",new FoodData("Pizza", 3, 10, 5));
			inventory.put("Salad",new FoodData("Salad", 2, 10, 5));
		}
		stateChanged();
	}

	public boolean getRunOutOfFood() {
		return isRunOutOfFood;
	}

	public void setRunOutOfFood(boolean RunOutOfFood) {
		isRunOutOfFood = RunOutOfFood;
		if (RunOutOfFood) {
			inventory.clear();
			inventory.put("Steak",new FoodData("Steak", 5, 1, 5));
			inventory.put("Chicken",new FoodData("Chicken", 4, 0, 5));
			inventory.put("Pizza",new FoodData("Pizza", 3, 0, 5));
			inventory.put("Salad",new FoodData("Salad", 2, 0, 5));
		}else {
			inventory.clear();
			inventory.put("Steak",new FoodData("Steak", 5, 10, 5));
			inventory.put("Chicken",new FoodData("Chicken", 4, 10, 5));
			inventory.put("Pizza",new FoodData("Pizza", 3, 10, 5));
			inventory.put("Salad",new FoodData("Salad", 2, 10, 5));
		}
		stateChanged();
	}
}



