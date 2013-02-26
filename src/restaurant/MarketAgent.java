package restaurant;

import agent.Agent;
import java.util.*;
import restaurant.interfaces.*;

public class MarketAgent extends Agent implements Market{

	String name;
	Cashier cashier;
	Map<String, FoodData> inventory = new HashMap<String, FoodData>();
	List<MyOrder> orders = new ArrayList<MyOrder>();
	enum OrderStatus {Received, Delivered, Paying, Failed};
	boolean isEnoughInventory = true;
	public MarketAgent(String name) {
		this.name = name;
	}
	//Messaging
	public void msgOrder(Cook cook, String choice, int amount) {
		print(String.format("Getting Order From %s %s %d", cook.getName(), choice, amount));
		orders.add(new MyOrder(cook, choice, OrderStatus.Received, amount));
		stateChanged();
	}
	public void msgHereIsPayment(Double amount) {
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
		if (inventory.get(o.choice).amount - o.amount < 0) {
			print("Don't have enough inventories");
			o.cook.msgDelivery(o.choice, 0);
			o.status = OrderStatus.Failed;
			stateChanged();
			return;
		}
		print(String.format("Delivering %s in %d amount(1000 millisecondes)", o.choice, o.amount));
		try {
			Thread.sleep(o.cook.getRunOutOfFood() ? 100000 : 1000);
		}catch(InterruptedException e) {}
		o.cook.msgDelivery(o.choice, o.amount);
		inventory.get(o.choice).amount -= o.amount;
		o.status = OrderStatus.Delivered;
		stateChanged();
	}
	private void makeBill(MyOrder o) {
		print("Calling cashier to make a payment (1000 milliseconds)");
		try {
			Thread.sleep(1000);
		}catch (InterruptedException e) {}
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
		Cook cook;
		String choice;
		OrderStatus status;
		Integer amount;
		public MyOrder(Cook cook, String choice, OrderStatus status, int amount) {
			this.cook = cook;
			this.choice = choice;
			this.status = status;
			this.amount = amount;
		}
	}
	
	//Methods
	public void setCashier(Cashier cashier) {
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
	
	public void cleanInventory() {
		inventory.clear();
	}
	
	public boolean getEnoughInventory() {
		return isEnoughInventory;
	}
	
	public void setEnoughInventory(boolean enough) {
		isEnoughInventory = enough;
		if(!enough) {
			cleanInventory();
			addInventory("Steak", 3, 10.99);
			addInventory("Chicken", 3, 5.99);
			addInventory("Pizza", 3, 0.99);
			addInventory("Salad", 3, 3.99);
		}else {
			cleanInventory();
			addInventory("Steak", 50, 10.99);
			addInventory("Chicken", 50, 5.99);
			addInventory("Pizza", 50, 0.99);
			addInventory("Salad", 50, 3.99);
		}
	}
}
