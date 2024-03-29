package restaurant;
import java.awt.Color;
import restaurant.gui.RestaurantGui;
import restaurant.layoutGUI.*;
import agent.Agent;
import astar.*;
import java.util.*;
import java.util.concurrent.*;

import restaurant.interfaces.*;

/** Restaurant Waiter Agent.
 * Sits customers at assigned tables and takes their orders.
 * Takes the orders to the cook and then returns them 
 * when the food is done.  Cleans up the tables after the customers leave.
 * Interacts with customers, host, and cook */
public abstract class WaiterAgent extends Agent implements Waiter{

	//State variables for Waiter
	public enum WaiterState {Pending_Break, Pending_Resume, Sent_Break, Sent_Resume, Working, NotWorking};
	private boolean onBreak = false;
	private WaiterState state; 
	//State constants for Customers

	public enum CustomerState 
	{NEED_SEATED,READY_TO_ORDER,ORDER_PENDING,ORDER_READY,IS_DONE,NO_ACTION, COOK_REORDER_PENDING, COOK_REORDER_DONE, LEAVING};
	public enum ChangeOrderState {CUST_REORDER_READY, CUST_REORDER_PENDING, CUST_REORDER_WAITING, CUST_REORDER_APPROVED, CUST_REORDER_DECLINED, CUST_NOTHING};

	Timer timer = new Timer();

	/** Private class to hold information for each customer.
	 * Contains a reference to the customer, his choice, 
	 * table number, and state */
	class MyCustomer {
		public CustomerState state;
		public Customer cmr;
		public ChangeOrderState changeOrderState;
		public String choice = "";
		public String secondChoice = "";
		public int tableNum;
		public Double bill;
		public Menu menu;
		public int isChangedOrder = 0;
		public Food food;//gui thing

		/** Constructor for MyCustomer class.
		 * @param cmr reference to customer
		 * @param num assigned table number */
		public MyCustomer(Customer cmr, int num){
			this.cmr = cmr;
			tableNum = num;
			this.bill = 0.0;
			state = CustomerState.NO_ACTION;
			changeOrderState = ChangeOrderState.CUST_NOTHING;
			menu = new Menu();
		}
	}

	//Name of waiter
	private String name;

	//All the customers that this waiter is serving
	private List<MyCustomer> customers = Collections.synchronizedList(new ArrayList<MyCustomer>());

	private Host host;
	protected Cook cook;
	private Cashier cashier;
	private Semaphore sem = new Semaphore(0, true);

	//Animation Variables
	RestaurantGui mainframe;
	AStarTraversal aStar;
	Restaurant restaurant; //the gui layout
	GuiWaiter guiWaiter; 
	Position currentPosition; 
	Position originalPosition;
	Table[] tables; //the gui tables



	/** Constructor for WaiterAgent class
	 * @param name name of waiter
	 * @param gui reference to the gui */
	public WaiterAgent(String name, AStarTraversal aStar,
			Restaurant restaurant, Table[] tables) {
		super();

		this.name = name;
		//initialize all the animation objects
		this.aStar = aStar;
		this.restaurant = restaurant;//the layout for astar
		guiWaiter = new GuiWaiter(name.substring(0,2), new Color(255, 0, 0), restaurant);
		currentPosition = new Position(guiWaiter.getX(), guiWaiter.getY());
		currentPosition.moveInto(aStar.getGrid());
		originalPosition = currentPosition;//save this for moving into
		this.tables = tables;
	} 

	// *** MESSAGES ***

	/** Host sends this to give the waiter a new customer.
	 * @param customer customer who needs seated.
	 * @param tableNum identification number for table */
	public void msgSitCustomerAtTable(Customer customer, int tableNum){
		MyCustomer c = new MyCustomer(customer, tableNum);
		c.state = CustomerState.NEED_SEATED;
		customers.add(c);
		stateChanged();
	}

	/** Customer sends this when they are ready.
	 * @param customer customer who is ready to order.
	 */
	public void msgImReadyToOrder(Customer customer){
		//print("received msgImReadyToOrder from:"+customer);
		synchronized (customers) {
			for(int i=0; i < customers.size(); i++){
				//if(customers.get(i).cmr.equals(customer)){
				if (customers.get(i).cmr == customer){
					customers.get(i).state = CustomerState.READY_TO_ORDER;
					stateChanged();
					return;
				}
			}
		}
		System.out.println("msgImReadyToOrder in WaiterAgent, didn't find him?");
	}

	/** Customer sends this when they have decided what they want to eat 
	 * @param customer customer who has decided their choice
	 * @param choice the food item that the customer chose */
	public void msgHereIsMyChoice(Customer customer, String choice){
		synchronized (customers) {
			for(MyCustomer c:customers){
				if(c.cmr.equals(customer)){
					c.choice = choice;
					c.state = CustomerState.ORDER_PENDING;
					stateChanged();
					sem.release();
					return;
				}
			}
		}
	}

	/** Cook sends this when the order is ready.
	 * @param tableNum identification number of table whose food is ready
	 * @param f is the guiFood object */
	public void msgOrderIsReady(int tableNum, Food f){
		synchronized (customers) {
			for(MyCustomer c:customers){
				print("Order Ready");
				if(c.tableNum == tableNum){
					c.state = CustomerState.ORDER_READY;
					c.food = f; //so that later we can remove it from the table.
					if(c.changeOrderState == ChangeOrderState.CUST_REORDER_WAITING) {
						c.changeOrderState = ChangeOrderState.CUST_REORDER_DECLINED;
					}
					stateChanged();
					return;
				}
			}
		}
	}

	/** Customer sends this when they are done eating.
	 * @param customer customer who is leaving the restaurant. */
	public void msgDoneEatingAndLeaving(Customer customer){
		synchronized (customers) {
			for(MyCustomer c:customers){
				if(c.cmr.equals(customer)){
					c.state = CustomerState.IS_DONE;
					stateChanged();
					return;
				}
			}
		}
	}

	public void msgDecisionOnBreak(boolean working) {
		if (working) {
			print("Host allow me on break");
			state = WaiterState.NotWorking;
		}
		else {
			print("Host doesn't allow me on break");
			state = WaiterState.Working;
		}
		onBreak = working;
		mainframe.setstateCB(onBreak);
		stateChanged();
	}

	public void msgCookReorder(int tableNum) {
		synchronized (customers) {
			for (MyCustomer c : customers) {
				if (c.tableNum == tableNum && c.secondChoice.equals("")) {
					c.state = CustomerState.COOK_REORDER_PENDING;
				}
			}
		}
		stateChanged();
	}

	public void msgCustReorder(Customer customer, String choice) {
		print(String.format("%s change order", customer.getName()));
		synchronized (customers) {
			for (MyCustomer c : customers) {
				if (c.cmr == customer) {
					c.changeOrderState = ChangeOrderState.CUST_REORDER_PENDING;
					c.secondChoice = choice;
				}
			}
		}
		stateChanged();
	}

	public void msgLeaving(Customer customer) {
		synchronized (customers) {
			for(MyCustomer c:customers){
				if(c.cmr.equals(customer)){
					c.state = CustomerState.LEAVING;
					stateChanged();
					return;
				}
			}
		}
		stateChanged();
	}

	public void msgDecisionChangeOrder(int tableNum, boolean approved) {
		synchronized (customers) {
			for (MyCustomer c : customers) {
				if(c.tableNum == tableNum && c.changeOrderState == ChangeOrderState.CUST_REORDER_WAITING) {
					c.isChangedOrder = 1 + (approved ? 1 : 0);
				}
			}
		}
		stateChanged();
	}

	public void msgImReadyToReorder(Customer customer){
		//print("received msgImReadyToOrder from:"+customer);
		synchronized (customers) {
			for(int i=0; i < customers.size(); i++){
				//if(customers.get(i).cmr.equals(customer)){
				if (customers.get(i).cmr == customer){
					customers.get(i).changeOrderState = ChangeOrderState.CUST_REORDER_READY;
					stateChanged();
					return;
				}
			}
		}
		System.out.println("msgImReadyToOrder in WaiterAgent, didn't find him?");
	}

	/** Sent from GUI to control breaks 
	 * @param state true when the waiter should go on break and 
	 *              false when the waiter should go off break
	 *              Is the name onBreak right? What should it be?*/
	public void setBreakStatus(boolean state){
		//print("" + state);
		if (state)
			this.state = WaiterState.Pending_Break;
		else {
			this.state = WaiterState.Pending_Resume;
			this.onBreak = false;
		}
		stateChanged();
	}

	public void msgHereIsBill(Customer customer, Double bill) {
		print("Getting Bill From Cashier");
		synchronized (customers) {
			for (MyCustomer c : customers) {
				if (c.cmr.equals(customer)) {
					c.bill = bill;
					stateChanged();
					return;
				}
			}
		}
	}



	/** Scheduler.  Determine what action is called for, and do it. */
	protected boolean pickAndExecuteAnAction() {
		//print("in waiter scheduler");

		//Runs through the customers for each rule, so 
		//the waiter doesn't serve only one customer at a time

		if (state == WaiterState.Pending_Break) {
			print("Asking the host");
			host.msgCanIOnBreak(this);
		}
		synchronized (customers) {
			if(customers.isEmpty() && state == WaiterState.NotWorking) {
				print("I'm on break now");
			}
		}

		if (state == WaiterState.Pending_Resume) {
			host.msgResumeWork(this);
		}

		if(!customers.isEmpty()){
			//System.out.println("in scheduler, customers not empty:");	  
			MyCustomer temp = null;
			synchronized (customers)  {
				for(MyCustomer c:customers){
					if(c.changeOrderState == ChangeOrderState.CUST_REORDER_WAITING && c.isChangedOrder != 0) {
						if(c.isChangedOrder - 1 == 0) {
							c.changeOrderState = ChangeOrderState.CUST_REORDER_DECLINED;
						}else
							c.changeOrderState = ChangeOrderState.CUST_REORDER_APPROVED;
						return true;
					}
				}
			}

			synchronized (customers) {
				for(MyCustomer c:customers){
					if(c.changeOrderState == ChangeOrderState.CUST_REORDER_DECLINED) {
						temp = c;		
						c.secondChoice = "";
						break;				
					}
				}
			}
			if (temp != null) {
				tellCustomerChangeOrderDecision(temp, false);
				return true;
			}
			synchronized (customers) {
				temp = null;
				for(MyCustomer c:customers){
					if(c.changeOrderState == ChangeOrderState.CUST_REORDER_APPROVED) {
						temp = c;
						c.secondChoice = "";
						break;
					}
				}
			}

			if (temp != null) {
				tellCustomerChangeOrderDecision(temp, true);
				return true;
			}

			//Gives food to customer if the order is ready
			synchronized (customers) {
				temp = null;
				for(MyCustomer c:customers){
					if(c.state == CustomerState.ORDER_READY) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				makeBill(temp);
				giveFoodToCustomer(temp);
				return true;
			}
			//Clears the table if the customer has left
			synchronized (customers) {
				temp = null;
				for(MyCustomer c:customers){
					if(c.state == CustomerState.IS_DONE && c.bill != 0) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				clearTable(temp);
				sendBillToCustomer(temp);
				return true;
			}

			synchronized (customers) {
				temp = null;
				for(MyCustomer c:customers){
					if(c.state == CustomerState.LEAVING) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				clearTable(temp);
				return true;
			}

			//Seats the customer if they need it
			synchronized (customers) {
				temp = null;
				for(MyCustomer c:customers){
					if(c.state == CustomerState.NEED_SEATED){
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				seatCustomer(temp);
				return true;
			}

			//Cook runs out of food
			synchronized (customers) {
				temp = null;
				for(MyCustomer c : customers){
					if(c.state == CustomerState.COOK_REORDER_PENDING){
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				reorderCustomer(temp);
				return true;
			}

			//Gives all pending orders to the cook
			/*for(MyCustomer c:customers){
					if(c.state == CustomerState.ORDER_PENDING){
						//print("Cook");
						giveOrderToCook(c);
						return true;
					}
				}*/

			//Customer reorder
			synchronized (customers) {
				temp = null;
				for(MyCustomer c:customers){
					//print("testing for ready to order"+c.state);
					if(c.changeOrderState == ChangeOrderState.CUST_REORDER_READY) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				takeReorder(temp);
				return true;
			}

			synchronized (customers) {
				temp = null;
				for(MyCustomer c : customers){
					if(c.changeOrderState == ChangeOrderState.CUST_REORDER_PENDING){
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				reorderCook(temp);
				return true;
			}

			//Takes new orders for customers that are ready
			synchronized (customers) {
				temp = null;
				for(MyCustomer c:customers){
					//print("testing for ready to order"+c.state);
					if(c.state == CustomerState.READY_TO_ORDER) {
						temp = c;
						break;
					}
				}	
			}
			if (temp != null) {
				multiStepAction(temp);
				return true;
			}

		}


		if (!currentPosition.equals(originalPosition)) {
			DoMoveToOriginalPosition();//Animation thing
			return true;
		}

		//we have tried all our rules and found nothing to do. 
		// So return false to main loop of abstract agent and wait.
		//print("in scheduler, no rules matched:");
		return false;
	}

	// *** ACTIONS ***


	/** Seats the customer at a specific table 
	 * @param customer customer that needs seated */
	protected void seatCustomer(MyCustomer customer) {
		DoSeatCustomer(customer); //animation	
		customer.state = CustomerState.NO_ACTION;
		customer.cmr.msgFollowMeToTable(this, customer.menu);
		stateChanged();
	}
	/** Takes down the customers order 
	 * @param customer customer that is ready to order */
	protected void takeOrder(MyCustomer customer) {
		DoTakeOrder(customer); //animation
		customer.state = CustomerState.NO_ACTION;
		customer.cmr.msgWhatWouldYouLike();
		stateChanged();
	}

	protected void takeReorder(MyCustomer customer) {
		DoTakeOrder(customer); //animation
		customer.changeOrderState = ChangeOrderState.CUST_NOTHING;
		customer.cmr.msgWhatWouldYouLike();
		stateChanged();
	}

	/** Gives any pending orders to the cook 
	 * @param customer customer that needs food cooked */
	protected abstract void giveOrderToCook(MyCustomer customer);

	/** Gives food to the customer 
	 * @param customer customer whose food is ready */
	protected void giveFoodToCustomer(MyCustomer customer) {
		DoGiveFoodToCustomer(customer);//Animation
		customer.state = CustomerState.NO_ACTION;
		customer.cmr.msgHereIsYourFood(customer.choice);
		stateChanged();
	}
	/** Starts a timer to clear the table 
	 * @param customer customer whose table needs cleared */
	protected void clearTable(MyCustomer customer) {
		DoClearingTable(customer);
		customers.remove(customer);
		//customer.state = CustomerState.NO_ACTION;
		stateChanged();
	}

	protected void makeBill(MyCustomer c) {
		cashier.msgMakeBill(this, c.cmr, c.choice);
	}

	protected void sendBillToCustomer(MyCustomer customer) {
		customer.cmr.msgHereIsMyBill(customer.bill);
	}

	protected void reorderCustomer(MyCustomer c){
		print(String.format("Send To %s To Reorder", c.cmr.getName()));
		c.menu.choices.remove(c.choice);
		c.cmr.msgPleaseReorder(c.menu);
		c.state = CustomerState.COOK_REORDER_DONE;
		stateChanged();
	}

	protected void reorderCook(MyCustomer c) {
		print(String.format("%s changes order to %s", c.cmr.getName(), c.secondChoice));
		tables[c.tableNum].takeOrder(c.secondChoice.substring(0,2)+"?");
		restaurant.placeFood(tables[c.tableNum].foodX(),
				tables[c.tableNum].foodY(),
				new Color(255, 255, 255), c.secondChoice.substring(0,2)+"?");
		cook.msgchangeOrder(this, c.tableNum, c.secondChoice);
		c.changeOrderState = ChangeOrderState.CUST_REORDER_WAITING;
		stateChanged();
	}

	protected void tellCustomerChangeOrderDecision(MyCustomer c, boolean decision) {
		if(decision) {
			print(String.format("Cook approved %s change order", c.cmr.getName()));
			c.choice = c.secondChoice;
		}
		else
			print(String.format("Cook declined %s change order", c.cmr.getName()));
		c.secondChoice = "";
		c.changeOrderState = ChangeOrderState.CUST_NOTHING;
		c.cmr.msgDecisionOnChangeOrder(decision);
		stateChanged();
	}
	
	protected void multiStepAction(MyCustomer c) {
		takeOrder(c);
		while (c.state != CustomerState.ORDER_PENDING)
			while(!sem.tryAcquire());
		giveOrderToCook(c);
		DoMoveToOriginalPosition();
	}

	// Animation Actions
	void DoSeatCustomer (MyCustomer customer){
		print("Seating " + customer.cmr + " at table " + (customer.tableNum+1));
		//move to customer first.
		GuiCustomer guiCustomer = customer.cmr.getGuiCustomer();
		guiMoveFromCurrentPostionTo(new Position(guiCustomer.getX()+1,guiCustomer.getY()));
		guiWaiter.pickUpCustomer(guiCustomer);
		Position tablePos = new Position(tables[customer.tableNum].getX()-1,
				tables[customer.tableNum].getY()+1);
		guiMoveFromCurrentPostionTo(tablePos);
		guiWaiter.seatCustomer(tables[customer.tableNum]);
	}
	void DoTakeOrder(MyCustomer customer){
		print("Taking " + customer.cmr +"'s order.");
		Position tablePos = new Position(tables[customer.tableNum].getX()-1,
				tables[customer.tableNum].getY()+1);
		print(tablePos.toString());
		guiMoveFromCurrentPostionTo(tablePos);
	}
	void DoGiveFoodToCustomer(MyCustomer customer){
		print("Giving finished order of " + customer.choice +" to " + customer.cmr);
		Position inFrontOfGrill = new Position(customer.food.getX()-1,customer.food.getY());
		guiMoveFromCurrentPostionTo(inFrontOfGrill);//in front of grill
		guiWaiter.pickUpFood(customer.food);
		Position tablePos = new Position(tables[customer.tableNum].getX()-1,
				tables[customer.tableNum].getY()+1);
		guiMoveFromCurrentPostionTo(tablePos);
		guiWaiter.serveFood(tables[customer.tableNum]);
	}
	void DoClearingTable(final MyCustomer customer){
		print("Clearing table " + (customer.tableNum+1) + " (1500 milliseconds)");
		timer.schedule(new TimerTask(){
			public void run(){		    
				endCustomer(customer);
			}
		}, 1500);
	}
	/** Function called at the end of the clear table timer
	 * to officially remove the customer from the waiter's list.
	 * @param customer customer who needs removed from list */
	protected void endCustomer(MyCustomer customer){ 
		print("Table " + (customer.tableNum+1) + " is cleared!");
		if(customer.food != null)
			customer.food.remove(); //remove the food from table animation
		host.msgTableIsFree(customer.tableNum);
		customers.remove(customer);
		stateChanged();
	}
	protected void DoMoveToOriginalPosition(){
		print("Nothing to do. Moving to original position="+originalPosition);
		guiMoveFromCurrentPostionTo(originalPosition);
	}

	//this is just a subroutine for waiter moves. It's not an "Action"
	//itself, it is called by Actions.
	void guiMoveFromCurrentPostionTo(Position to){
		//System.out.println("[Gaut] " + guiWaiter.getName() + " moving from " + currentPosition.toString() + " to " + to.toString());

		AStarNode aStarNode = (AStarNode)aStar.generalSearch(currentPosition, to);
		List<Position> path = aStarNode.getPath();
		Boolean firstStep   = true;
		Boolean gotPermit   = true;

		for (Position tmpPath: path) {
			//The first node in the path is the current node. So skip it.
			if (firstStep) {
				firstStep   = false;
				continue;
			}

			//Try and get lock for the next step.
			int attempts    = 1;
			gotPermit       = new Position(tmpPath.getX(), tmpPath.getY()).moveInto(aStar.getGrid());

			//Did not get lock. Lets make n attempts.
			while (!gotPermit && attempts < 3) {
				//System.out.println("[Gaut] " + guiWaiter.getName() + " got NO permit for " + tmpPath.toString() + " on attempt " + attempts);

				//Wait for 1sec and try again to get lock.
				try { Thread.sleep(1000); }
				catch (Exception e){}

				gotPermit   = new Position(tmpPath.getX(), tmpPath.getY()).moveInto(aStar.getGrid());
				attempts ++;
			}

			//Did not get lock after trying n attempts. So recalculating path.            
			if (!gotPermit) {
				//System.out.println("[Gaut] " + guiWaiter.getName() + " No Luck even after " + attempts + " attempts! Lets recalculate");
				guiMoveFromCurrentPostionTo(to);
				break;
			}

			//Got the required lock. Lets move.
			//System.out.println("[Gaut] " + guiWaiter.getName() + " got permit for " + tmpPath.toString());
			currentPosition.release(aStar.getGrid());
			currentPosition = new Position(tmpPath.getX(), tmpPath.getY ());
			guiWaiter.move(currentPosition.getX(), currentPosition.getY());
		}
		/*
	boolean pathTaken = false;
	while (!pathTaken) {
	    pathTaken = true;
	    //print("A* search from " + currentPosition + "to "+to);
	    AStarNode a = (AStarNode)aStar.generalSearch(currentPosition,to);
	    if (a == null) {//generally won't happen. A* will run out of space first.
		System.out.println("no path found. What should we do?");
		break; //dw for now
	    }
	    //dw coming. Get the table position for table 4 from the gui
	    //now we have a path. We should try to move there
	    List<Position> ps = a.getPath();
	    Do("Moving to position " + to + " via " + ps);
	    for (int i=1; i<ps.size();i++){//i=0 is where we are
		//we will try to move to each position from where we are.
		//this should work unless someone has moved into our way
		//during our calculation. This could easily happen. If it
		//does we need to recompute another A* on the fly.
		Position next = ps.get(i);
		if (next.moveInto(aStar.getGrid())){
		    //tell the layout gui
		    guiWaiter.move(next.getX(),next.getY());
		    currentPosition.release(aStar.getGrid());
		    currentPosition = next;
		}
		else {
		    System.out.println("going to break out path-moving");
		    pathTaken = false;
		    break;
		}
	    }
	}
		 */
	}

	// *** EXTRA ***

	/** @return name of waiter */
	public String getName(){
		return name;
	}

	/** @return string representation of waiter */
	public String toString(){
		return "waiter " + getName();
	}

	/** Hack to set the cook for the waiter */
	public void setCook(Cook cook){
		this.cook = cook;
	}

	/** Hack to set the host for the waiter */
	public void setHost(Host host){
		this.host = host;
	}

	/** @return true if the waiter is on break, false otherwise */
	public boolean isOnBreak(){
		return onBreak;
	}

	public void setCashier(Cashier cashier) {
		this.cashier = cashier;
	}

	public void setGuiPanel(RestaurantGui frame) {
		mainframe = frame;
	}
}

