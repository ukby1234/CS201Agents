package restaurant;

import java.awt.Color;
import java.util.*;
import restaurant.layoutGUI.Restaurant;
import restaurant.layoutGUI.Table;
import astar.AStarTraversal;

public class ShareDataWaiterAgent extends WaiterAgent {

	public ShareDataWaiterAgent(String name, AStarTraversal aStar,
			Restaurant restaurant, Table[] tables, ShareData sd) {
		super(name, aStar, restaurant, tables);
		shareData = sd;
		po.start();
	}
	private ShareData shareData;
	private List<ShareOrder> orders = Collections.synchronizedList(new ArrayList<ShareOrder>());
	private PutOrder po = new PutOrder();
	
	protected void giveOrderToCook(MyCustomer customer) {
		//In our animation the waiter does not move to the cook in
		//order to give him an order. We assume some sort of electronic
		//method implemented as our message to the cook. So there is no
		//animation analog, and hence no DoXXX routine is needed.
		if (customer.choice.equals("")) {
			tables[customer.tableNum].takeOrder("");
			restaurant.placeFood(tables[customer.tableNum].foodX(),
					tables[customer.tableNum].foodY(),
					new Color(255, 255, 255), "");
			stateChanged();
			return;
		}
		print("Giving " + customer.cmr + "'s choice of " + customer.choice + " to cook");


		customer.state = CustomerState.NO_ACTION;
		orders.add(new ShareOrder(this, customer.tableNum, customer.choice));
		stateChanged();

		//Here's a little animation hack. We put the first two
		//character of the food name affixed with a ? on the table.
		//Simply let's us see what was ordered.
		tables[customer.tableNum].takeOrder(customer.choice.substring(0,2)+"?");
		restaurant.placeFood(tables[customer.tableNum].foodX(),
				tables[customer.tableNum].foodY(),
				new Color(255, 255, 255), customer.choice.substring(0,2)+"?");
	}
	
	private class PutOrder extends Thread {
		public void run() {
			while(true) {
				if (!orders.isEmpty()){
					ShareOrder sd = orders.remove(0);
					shareData.addItem(sd.waiter, sd.tableNum, sd.choice);
				}
			}
		}
	}
}
