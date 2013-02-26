package restaurant;

import restaurant.WaiterAgent.CustomerState;
import restaurant.gui.RestaurantGui;
import restaurant.interfaces.*;
import restaurant.layoutGUI.*;
import agent.Agent;
import java.util.*;
import java.awt.Color;

/** Restaurant customer agent. 
 * Comes to the restaurant when he/she becomes hungry.
 * Randomly chooses a menu item and simulates eating 
 * when the food arrives. 
 * Interacts with a waiter only */
public class CustomerAgent extends Agent implements Customer{
	private String name;
	private int hungerLevel = 5;  // Determines length of meal
	private RestaurantGui gui;

	// ** Agent connections **
	private Host host;
	private Waiter waiter;
	private Cashier cashier;
	Restaurant restaurant;
	private Menu menu;
	private Double money;
	private Double change;
	private Double bill;
	private Integer days;
	private boolean NonNormLeave;
	private boolean changeOrder = false;
	private boolean isOrdered = false;
	Timer timer = new Timer();
	GuiCustomer guiCustomer; //for gui
	// ** Agent state **
	private boolean isHungry = false; //hack for gui
	private boolean isWaiting = true;
	private boolean isEnoughMoney = true;
	private boolean isCheapestOrder = false;
	public enum AgentState
	{DoingNothing, WaitingInRestaurant, SeatedWithMenu, WaiterCalled, WaitingForFood, Eating, Paying, Leaving};
	//{NO_ACTION,NEED_SEATED,NEED_DECIDE,NEED_ORDER,NEED_EAT,NEED_LEAVE};
	private AgentState state = AgentState.DoingNothing;//The start state
	public enum AgentEvent 
	{gotHungry, beingSeated, decidedChoice, waiterToTakeOrder, foodDelivered, doneEating, gotBill, gotChange, hostFull, changeOrder, WashingDishes};
	List<AgentEvent> events = new ArrayList<AgentEvent>();

	/** Constructor for CustomerAgent class 
	 * @param name name of the customer
	 * @param gui reference to the gui so the customer can send it messages
	 */
	public CustomerAgent(String name, RestaurantGui gui, Restaurant restaurant) {
		super();
		this.gui = gui;
		this.name = name;
		this.restaurant = restaurant;
		this.money = 100.0;//0.0;//Math.random() * 100;
		this.change = 0.0;
		this.bill = 0.0;
		NonNormLeave = false;
		guiCustomer = new GuiCustomer(name.substring(0,2), new Color(0,255,0), restaurant);
	}
	public CustomerAgent(String name, Restaurant restaurant) {
		super();
		this.gui = null;
		this.name = name;
		this.restaurant = restaurant;
		this.money = 100.0;//0.0;//Math.random() * 100;
		this.change = 0.0;
		this.bill = 0.0;
		NonNormLeave = false;
		guiCustomer = new GuiCustomer(name.substring(0,1), new Color(0,255,0), restaurant);
	}
	// *** MESSAGES ***
	/** Sent from GUI to set the customer as hungry */
	public void setHungry() {
		events.add(AgentEvent.gotHungry);
		isHungry = true;
		NonNormLeave = false;
		isOrdered = false;
		print("I'm hungry");
		stateChanged();
	}
	/** Waiter sends this message so the customer knows to sit down 
	 * @param waiter the waiter that sent the message
	 * @param menu a reference to a menu */
	public void msgFollowMeToTable(Waiter waiter, Menu menu) {
		this.menu = menu;
		this.waiter = waiter;
		print("Received msgFollowMeToTable from" + waiter);
		events.add(AgentEvent.beingSeated);
		stateChanged();
	}
	/** Waiter sends this message to take the customer's order */
	public void msgDecided(){
		events.add(AgentEvent.decidedChoice);
		stateChanged(); 
	}
	/** Waiter sends this message to take the customer's order */
	public void msgWhatWouldYouLike(){
		events.add(AgentEvent.waiterToTakeOrder);
		stateChanged(); 
	}

	/** Waiter sends this when the food is ready 
	 * @param choice the food that is done cooking for the customer to eat */
	public void msgHereIsYourFood(String choice) {
		events.add(AgentEvent.foodDelivered);
		stateChanged();
	}
	/** Timer sends this when the customer has finished eating */
	public void msgDoneEating() {
		events.add(AgentEvent.doneEating);
		stateChanged(); 
	}

	public void msgHereIsMyBill(Double bill) {
		print("Getting Bill From Waiter");
		events.add(AgentEvent.gotBill);
		this.bill = bill;
		stateChanged();
	}
	public void msgHereIsChange(Double change) {
		print("Getting Change From Cashier");
		events.add(AgentEvent.gotChange);
		this.change = change;
		stateChanged();
	}

	public void msgFullRightNow() {
		print("Restaurant Full");
		events.add(AgentEvent.hostFull);
		stateChanged();
	}

	public void msgLeaving() {
		print("I'm Leaving");
		this.state = AgentState.DoingNothing;
		isHungry = false;
		stateChanged();
	}

	public void msgPleaseReorder(Menu menu) {
		print("I Have to Reorder");
		state = AgentState.WaitingInRestaurant;
		events.add(AgentEvent.beingSeated);
		this.menu = menu;
		stateChanged();
	}
	
	public void msgDecisionOnChangeOrder(boolean decision) {
		if(decision)
			print("Cook approved my change order");
		else
			print("Cook declined my change order");
	}
	
	public void setChangeOrder(boolean change) {
		changeOrder = change;
		stateChanged();
	}
	
	public void setCheapestOrder(boolean Cheapset) {
		isCheapestOrder = Cheapset;
		if (Cheapset) {
			money = 6.99;
		} else{
			money = 100.0;
			isEnoughMoney = true;
			gui.invalidate();
		}
		stateChanged();
	}
	
	public void msgWashingDishes(double days) {
		this.days = (int)days;
		events.add(AgentEvent.WashingDishes);
		stateChanged();
	}
	/** Scheduler.  Determine what action is called for, and do it. */
	protected boolean pickAndExecuteAnAction() {
		if (events.isEmpty()) return false;
		AgentEvent event = events.remove(0); //pop first element

		//Simple finite state machine
		if (state == AgentState.DoingNothing){
			if (event == AgentEvent.gotHungry)	{
				goingToRestaurant();
				state = AgentState.WaitingInRestaurant;
				return true;
			}
			// elseif (event == xxx) {}
		}
		if (state == AgentState.WaitingInRestaurant) {
			if (event == AgentEvent.beingSeated)	{
				makeMenuChoice();
				return true;
			}
			if (event == AgentEvent.hostFull) {
				decideWaiting();
				return true;
			}
		}
		if (state == AgentState.SeatedWithMenu) {
			if (event == AgentEvent.decidedChoice)	{
				callWaiter();
				state = AgentState.WaiterCalled;
				return true;
			}
		}
		if (state == AgentState.WaiterCalled) {
			//print("Here");
			if (event == AgentEvent.waiterToTakeOrder)	{
				if(changeOrder && !isOrdered) {
					if(menu.choices.isEmpty()) {
						NonNormLeave = true;
						leaveRestaurant();
						return true;
					}
					events.add(AgentEvent.changeOrder);
					stateChanged();
				}
				orderFood();
				state = AgentState.WaitingForFood;
				return true;
			}
		}
		if (state == AgentState.WaitingForFood) {
			//print("Here");
			if (event == AgentEvent.foodDelivered)	{
				eatFood();
				state = AgentState.Eating;
				return true;
			}
			if (event == AgentEvent.changeOrder) {
				changeOrder();
				return true;
			}
		}
		if (state == AgentState.Eating) {
			if (event == AgentEvent.doneEating)	{
				leaveRestaurant();
				state = AgentState.Paying;
				return true;
			}
		}
		if (state == AgentState.Paying) {
			if (event == AgentEvent.gotBill) {
				payForBill();
				state = AgentState.Paying;
				return true;
			}
			if (event == AgentEvent.gotChange) {
				gotChange();
				state = AgentState.DoingNothing;
				return true;
			}
			if (event == AgentEvent.WashingDishes) {
				washDishes();
				return true;
			}
		}

		print("No scheduler rule fired, should not happen in FSM, event="+event+" state="+state);
		return false;
	}

	// *** ACTIONS ***

	/** Goes to the restaurant when the customer becomes hungry */
	private void goingToRestaurant() {
		print("Going to restaurant");
		guiCustomer.appearInWaitingQueue();
		host.msgIWantToEat(this);//send him our instance, so he can respond to us
		stateChanged();
	}

	/** Starts a timer to simulate the customer thinking about the menu */
	private void makeMenuChoice(){
		Double min = Double.MAX_VALUE;
		for (String choice : menu.choices.keySet()) {
			if (menu.choices.get(choice) < min)
				min = menu.choices.get(choice);
		}
		if (menu.choices.isEmpty()) {
			print("All Food Running Out");
			NonNormLeave = true;
			leaveRestaurant();
			return;
		}
		if (money < min && !isWaiting) {
			print("I don't have enough money");
			NonNormLeave = true;
			leaveRestaurant();
			return;
		}
		print("Deciding menu choice...(3000 milliseconds)");
		timer.schedule(new TimerTask() {
			public void run() {  
				msgDecided();	    
			}},
			3000);//how long to wait before running task
		state = AgentState.SeatedWithMenu;
		stateChanged();
	}
	private void callWaiter(){
		if(!(changeOrder && isOrdered)) {
			print("I decided!");
			waiter.msgImReadyToOrder(this);
		}else {
			print("Call waiter to reorder");
			waiter.msgImReadyToReorder(this);
		}
		
		stateChanged();
	}

	/** Picks a random choice from the menu and sends it to the waiter */
	private void orderFood(){
		if(menu.choices.isEmpty()) {
			print("All Food Running Out");
			NonNormLeave = true;
			leaveRestaurant();
			state = AgentState.DoingNothing;
			events.clear();
			waiter.msgHereIsMyChoice(this, "");
			stateChanged();
			return;
		}
		if (isCheapestOrder) {
			print("Ordering the " + "Salad");
			waiter.msgHereIsMyChoice(this, "Salad");
			menu.choices.remove("Salad");
			isOrdered = true;
			stateChanged();
			return;
		}
			
		Object choices[] = menu.choices.keySet().toArray();
		String choice = (String)choices[(int)(Math.random() * choices.length)];
		if (changeOrder && isOrdered) {
			print("Reordering the " + choice);
			waiter.msgCustReorder(this, choice);
		}else {
			print("Ordering the " + choice);
			waiter.msgHereIsMyChoice(this, choice);
			isOrdered = true;
		}
		menu.choices.remove(choice);
		stateChanged();
	}

	/** Starts a timer to simulate eating */
	private void eatFood() {
		print("Eating for " + hungerLevel*1000 + " milliseconds.");
		timer.schedule(new TimerTask() {
			public void run() {
				msgDoneEating();    
			}},
			getHungerLevel() * 1000);//how long to wait before running task
		stateChanged();
	}


	/** When the customer is done eating, he leaves the restaurant */
	private void leaveRestaurant() {
		print("Leaving the restaurant");
		guiCustomer.leave(); //for the animation
		if(NonNormLeave) {
			waiter.msgLeaving(this);
			this.msgLeaving();
		}
		else
			waiter.msgDoneEatingAndLeaving(this);
		isHungry = false;
		stateChanged();
		gui.setCustomerEnabled(this); //Message to gui to enable hunger button

		//hack to keep customer getting hungry. Only for non-gui customers
		if (gui==null) becomeHungryInAWhile();//set a timer to make us hungry.
	}

	/** This starts a timer so the customer will become hungry again.
	 * This is a hack that is used when the GUI is not being used */
	private void becomeHungryInAWhile() {
		timer.schedule(new TimerTask() {
			public void run() {  
				setHungry();		    
			}},
			15000);//how long to wait before running task
	}

	private void payForBill() {
		final CustomerAgent c = this;
		print("Walking to cashier (1000 milliseconds)");
		timer.schedule(new TimerTask() {
			public void run() {
				c.print(String.format("Paying %.2f", money));
				cashier.msgHereIsPayment(c, money);
			}
		}, 1000);
		stateChanged();
	}

	private void gotChange() {
		print(String.format("Got Change: %.2f", this.change));
		money = change;
		stateChanged();
	}

	private void decideWaiting() {
		final CustomerAgent c = this;
		print("Deciding for waiting 1000 milliseconds");
		timer.schedule(new TimerTask() {
			public void run() {
				if (isWaiting) {
					host.msgStayOrLeave(c, true);
					c.print("I'm waitng");
				}
				else {
					host.msgStayOrLeave(c, false);
					guiCustomer.leave();
					c.msgLeaving();
				}
			}
		}, 1000);
		
	}

	private void changeOrder() {
		print("Changing order");
		callWaiter();
		state = AgentState.WaiterCalled;
		stateChanged();
	}
	
	private void washDishes() {
		print(String.format("Washing dishes for %d days (%d milliseconds)", days, days * 100));
		timer.schedule(new TimerTask() {
			public void run() {
				state = AgentState.DoingNothing;
			}
		}, days * 100);
		
	}

	// *** EXTRA ***

	/** establish connection to host agent. 
	 * @param host reference to the host */
	public void setHost(Host host) {
		this.host = host;
	}

	/** Returns the customer's name
	 *@return name of customer */
	public String getName() {
		return name;
	}

	/** @return true if the customer is hungry, false otherwise.
	 ** Customer is hungry from time he is created (or button is
	 ** pushed, until he eats and leaves.*/
	public boolean isHungry() {
		return isHungry;
	}
	
	public boolean isWaiting() {
		return isWaiting;
	}
	
	public void setWaiting(boolean waiting) {
		isWaiting = waiting;
	}
	
	public boolean isEnoughMoney() {
		return isEnoughMoney;
	}
	
	public void setEnoughMoney(boolean EnoughMoney) {
		isEnoughMoney = EnoughMoney;
		if (EnoughMoney)
			this.money = 100.0;
		else
			this.money = 5.0;
	}

	/** @return the hungerlevel of the customer */
	public int getHungerLevel() {
		return hungerLevel;
	}

	/** Sets the customer's hungerlevel to a new value
	 * @param hungerLevel the new hungerlevel for the customer */
	public void setHungerLevel(int hungerLevel) {
		this.hungerLevel = hungerLevel; 
	}
	public GuiCustomer getGuiCustomer(){
		return guiCustomer;
	}

	/** @return the string representation of the class */
	public String toString() {
		return "customer " + getName();
	}

	public void setCashier(Cashier cashier) {
		this.cashier = cashier;
	}
	
	public boolean getChangeOrder() {
		return changeOrder;
	}
	
	public boolean getCheapestOrder() {
		return isCheapestOrder;
	}

}

