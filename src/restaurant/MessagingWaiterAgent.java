package restaurant;

import java.awt.Color;
import restaurant.layoutGUI.Restaurant;
import restaurant.layoutGUI.Table;
import astar.AStarTraversal;

public class MessagingWaiterAgent extends WaiterAgent{

	public MessagingWaiterAgent(String name, AStarTraversal aStar,
			Restaurant restaurant, Table[] tables) {
		super(name, aStar, restaurant, tables);
	}
	
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
		cook.msgHereIsAnOrder(this, customer.tableNum, customer.choice);
		stateChanged();

		//Here's a little animation hack. We put the first two
		//character of the food name affixed with a ? on the table.
		//Simply let's us see what was ordered.
		tables[customer.tableNum].takeOrder(customer.choice.substring(0,2)+"?");
		restaurant.placeFood(tables[customer.tableNum].foodX(),
				tables[customer.tableNum].foodY(),
				new Color(255, 255, 255), customer.choice.substring(0,2)+"?");
	}
}
