package restaurant;

import agent.Agent;
import java.util.*;
public class MarketAgent extends Agent {

	String name;
	CashierAgent cashier;
	Map<String, FoodData> inventory = new HashMap<String, FoodData>();
	List<MyOrder> orders = new ArrayList<MyOrder>();
	enum OrderStatus {Received, Delivered, Paying, Failed};
	public MarketAgent(String name) {
		this.name = name;
	}
	//Messaging
	void msgOrder(CookAgent cook, String choice, int amount) {
		print(String.format("Getting Order From %s %s %d", cook.getName(), choice, amount));
		orders.add(new MyOrder(cook, choice, OrderStatus.Received, amount));
		stateChanged();
	}
	void msgHereIsPayment(Double amount) {
		print(String.format("Here Is Payment: %.2f", amount));
		stateChanged();
	}
	//Scheduler
	@Override
	protected boolean pickAndExecuteAnAction() {
		for (MyOrder o : orders){
			if (o.status == OrderStatus.Received) {
				deliverOrder(o);			
				return true;
			}
			if (o.status == OrderStatus.Delivered) {
				makeBill(o);
				return true;
			}
		}
		return false;
	}
	//Actions
	private void deliverOrder(MyOrder o) {
		//print("" + o.amount);
		if (inventory.get(o.choice).amount - o.amount >= 0) {
			o.cook.msgDelivery(o.choice, o.amount);
			inventory.get(o.choice).amount -= o.amount;
			o.status = OrderStatus.Delivered;
		}
		else {
			o.cook.msgDelivery(o.choice, 0);
			o.status = OrderStatus.Failed;
		}
		stateChanged();
	}
	private void makeBill(MyOrder o) {
		cashier.msgHereIsBill(this, o.amount * inventory.get(o.choice).price);
		o.status = OrderStatus.Paying;
		stateChanged();
	}
	//Private classes
	private class FoodData {
		Double price;
		Integer amount;
		public FoodData(Double price, Integer amount) {
			this.price = price;
			this.amount = amount;
		}
	}
	
	private class MyOrder {
		CookAgent cook;
		String choice;
		OrderStatus status;
		Integer amount;
		public MyOrder(CookAgent cook, String choice, OrderStatus status, int amount) {
			this.cook = cook;
			this.choice = choice;
			this.status = status;
			this.amount = amount;
		}
	}
	
	//Methods
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return "market " + getName();
	}
	
	public void addInventory(String choice, int amount, double price) {
		inventory.put(choice, new FoodData(price, amount));
	}

}
