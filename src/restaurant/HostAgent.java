package restaurant;

import agent.Agent;
import java.util.*;


/** Host agent for restaurant.
 *  Keeps a list of all the waiters and tables.
 *  Assigns new customers to waiters for seating and 
 *  keeps a list of waiting customers.
 *  Interacts with customers and waiters.
 */
public class HostAgent extends Agent {

    /** Private class storing all the information for each table,
     * including table number and state. */
    private class Table {
		public int tableNum;
		public boolean occupied;
	
		/** Constructor for table class.
		 * @param num identification number
		 */
		public Table(int num){
		    tableNum = num;
		    occupied = false;
		}	
    }

    /** Private class to hold waiter information and state */
    private class MyWaiter {
	public WaiterAgent wtr;
	public boolean working = true;
	public boolean pending = false;

	/** Constructor for MyWaiter class
	 * @param waiter
	 */
	public MyWaiter(WaiterAgent waiter){
	    wtr = waiter;
	}
    }
    
    private class MyCustomer {
    	public CustomerAgent customer;
    	public CustomerState state;
    	public MyCustomer(CustomerAgent wtr, CustomerState state) {
    		customer = wtr;
    		this.state = state;
    	}
    }
    
    public enum CustomerState {Pending, Deciding, Waiting};

    //List of all the customers that need a table
    private List<MyCustomer> waitList =
		Collections.synchronizedList(new ArrayList<MyCustomer>());

    //List of all waiter that exist.
    private List<MyWaiter> waiters =
		Collections.synchronizedList(new ArrayList<MyWaiter>());
    private int nextWaiter =0; //The next waiter that needs a customer
    
    //List of all the tables
    int nTables;
    private Table tables[];

    //Name of the host
    private String name;

    /** Constructor for HostAgent class 
     * @param name name of the host */
    public HostAgent(String name, int ntables) {
	super();
	this.nTables = ntables;
	tables = new Table[nTables];

	for(int i=0; i < nTables; i++){
	    tables[i] = new Table(i);
	}
	this.name = name;
    }

    // *** MESSAGES ***

    /** Customer sends this message to be added to the wait list 
     * @param customer customer that wants to be added */
    public void msgIWantToEat(CustomerAgent customer){
	//print("Here is Customer");
    waitList.add(new MyCustomer(customer, CustomerState.Pending));
	stateChanged();
	//print("" + this.stateChange.availablePermits());
    }

    /** Waiter sends this message after the customer has left the table 
     * @param tableNum table identification number */
    public void msgTableIsFree(int tableNum){
	tables[tableNum].occupied = false;
	stateChanged();
    }
    
    public void msgCanIOnBreak(WaiterAgent w) {
    	print(String.format("%s asking for break", w.getName()));
    	for (MyWaiter waiter : waiters) {
    		if (waiter.wtr.equals(w)) {
    			print("found");
    			waiter.pending = true;
    		}
    	}
    	stateChanged();
    }
    
    public void msgResumeWork(WaiterAgent w) {
    	print(w.getName() + " resuming work");
    	for (MyWaiter waiter : waiters) {
    		if (waiter.wtr.equals(w))
    			waiter.pending = false;
    			waiter.working = true;
    	}
    	stateChanged();
    }
    
    public void msgStayOrLeave(CustomerAgent c, boolean choice) {
    	if (choice)
    		print(c.getName() + " waiting");
    	else
    		print(c.getName() + " leaving");
    	MyCustomer temp = null;
    	for (MyCustomer cu : waitList) {
    		if (cu.customer.equals(c)) 
    			temp = cu;
    	}
    	if (temp != null && choice)
    		temp.state = CustomerState.Waiting;
    	if (temp != null && !choice) {
    		print("" + waitList.remove(temp));
    	}
    	stateChanged();
    }

    /** Scheduler.  Determine what action is called for, and do it. */
    protected boolean pickAndExecuteAnAction() {
    	//print("Here");
    	for (MyWaiter waiter : waiters) {
			if (waiter.pending) {
				decideOnBreak(waiter);
				return true;
			}
		}
	boolean full = true;
	boolean onBreak = true;
	for (MyWaiter w : waiters) {
		if (w.working)
			onBreak = false;
	}
	for (int i = 0; i < nTables; i++)
		if (!tables[i].occupied) {
			full = false;
			break;
		}
	//print("" + waiters.size());
	//print("" + full);
	//print("" + onBreak);
	if (full || onBreak) {
		for (MyCustomer c : waitList) {
			if (c.state == CustomerState.Pending) {
				tellCustomerFull(c);
				return true;
			}
		}
	}
    if(!waitList.isEmpty() && !waiters.isEmpty() && !onBreak){
	    synchronized(waiters){
		//Finds the next waiter that is working
	    while(!waiters.get(nextWaiter).working){
		    nextWaiter = (nextWaiter+1)%waiters.size();
		}
	    }
	    print("picking waiter number:"+nextWaiter);
	    //Then runs through the tables and finds the first unoccupied 
	    //table and tells the waiter to sit the first customer at that table
	    for(int i=0; i < nTables; i++){

		if(!tables[i].occupied){
		    synchronized(waitList){
		    	for (MyCustomer c : waitList)
		    		if (c.state != CustomerState.Deciding) {
		    			tellWaiterToSitCustomerAtTable(waiters.get(nextWaiter), c, i);
		    			break;
		    		}
		    }
		    return true;
		}
	    }
	}

	//we have tried all our rules (in this case only one) and found
	//nothing to do. So return false to main loop of abstract agent
	//and wait.
	return false;
    }
    
    // *** ACTIONS ***
    
    /** Assigns a customer to a specified waiter and 
     * tells that waiter which table to sit them at.
     * @param waiter
     * @param customer
     * @param tableNum */
    private void tellWaiterToSitCustomerAtTable(MyWaiter waiter, MyCustomer customer, int tableNum){
	print("Telling " + waiter.wtr + " to sit " + customer +" at table "+(tableNum+1));
	waiter.wtr.msgSitCustomerAtTable(customer.customer, tableNum);
	tables[tableNum].occupied = true;
	waitList.remove(customer);
	nextWaiter = (nextWaiter+1)%waiters.size();
	stateChanged();
    }
    
    private void decideOnBreak(MyWaiter w) {
    	print(String.format("%s on break", w.wtr.getName()));
    	Double res = Math.random();
    	//w.wtr.msgDecisionOnBreak(true);
    	w.pending = false;
    	if (res > 0.5) {
    		w.working = false;
    		w.wtr.msgDecisionOnBreak(true);
    	}
    	else {
    		w.wtr.msgDecisionOnBreak(false);
    		w.working = true;
    	}
    	stateChanged();
    }
    
    private void tellCustomerFull(MyCustomer c) {
    	c.state = CustomerState.Deciding;
		c.customer.msgFullRightNow();
    }
	
    

    // *** EXTRA ***

    /** Returns the name of the host 
     * @return name of host */
    public String getName(){
        return name;
    }    

    /** Hack to enable the host to know of all possible waiters 
     * @param waiter new waiter to be added to list
     */
    public void setWaiter(WaiterAgent waiter){
	waiters.add(new MyWaiter(waiter));
	stateChanged();
    }
    
    //Gautam Nayak - Gui calls this when table is created in animation
    public void addTable() {
	nTables++;
	Table[] tempTables = new Table[nTables];
	for(int i=0; i < nTables - 1; i++){
	    tempTables[i] = tables[i];
	}  		  			
	tempTables[nTables - 1] = new Table(nTables - 1);
	tables = tempTables;
    }
}
