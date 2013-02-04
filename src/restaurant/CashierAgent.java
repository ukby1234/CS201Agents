package restaurant;

import agent.Agent;
import java.util.*;
public class CashierAgent extends Agent {
	enum BillStatus {Ready, Pending, Received, Cleared};
	List<MyCustomer> customers = new ArrayList<MyCustomer>();
	List<MyMarket> markets = new ArrayList<MyMarket>();
	Menu menu;
	WaiterAgent waiter;
	String name;
	public CashierAgent(String name) {
		this.name = name;
		menu = new Menu();
	}
	//Messaging
	void msgMakeBill(WaiterAgent wtr, CustomerAgent cta, String choice) {
		print("Making Bill for " + cta.getName());
		System.out.println(menu.choices.get(choice));
		Bill bill = new Bill(menu.choices.get(choice), BillStatus.Ready);
		for (MyCustomer c : customers) {
			if (c.customer.equals(cta)) {
				print("Old Customer");
				c.bill = bill;
				stateChanged();
				return;
			}
		}
		customers.add(new MyCustomer(cta, wtr, bill));
		stateChanged();
	}
	
	void msgHereIsPayment(CustomerAgent cta, Double payment) {
		print("Receiving Payment from " + cta.getName());
		for (MyCustomer c : customers) {
			if (c.customer.equals(cta)) {
				c.bill.status = BillStatus.Received;
				c.payment = payment;
			}
		}
		stateChanged();
	}
	
	void msgHereIsBill(MarketAgent mka, Double amount) {
		print(String.format("Paying to %s %.2f", mka.getName(), amount));
		markets.add(new MyMarket(mka, BillStatus.Ready, amount));
		stateChanged();
	}
	@Override
	protected boolean pickAndExecuteAnAction() {
		for (MyCustomer c : customers) {
			if (c.bill.status == BillStatus.Ready) {
				makeBill(c);
				return true;
			}
			if (c.bill.status == BillStatus.Received) {
				makeChange(c);
				return true;
			}
		}
		for (MyMarket m : markets) {
			if (m.status == BillStatus.Ready) {
				payToMarket(m);
				return true;
			}
		}
		return false;
	}
	//Actions
	private void makeBill(MyCustomer c) {
		print("Called");
		c.waiter.msgHereIsBill(c.customer, c.bill.price);
		c.bill.status = BillStatus.Pending;
		stateChanged();
	}
	
	private void makeChange(MyCustomer c) {
		if (c.payment - c.bill.price >= 0)
			c.customer.msgHereIsChange(c.payment - c.bill.price);
		else {
			print("Fuck You!");
			c.customer.msgHereIsChange(0.0);
		}
		c.bill.status = BillStatus.Cleared;
		stateChanged();
	}
	
	private void payToMarket(MyMarket m) {
		m.market.msgHereIsPayment(m.bill);
		m.status = BillStatus.Cleared;
		stateChanged();
	}
	private class MyCustomer {
		CustomerAgent customer;
		WaiterAgent waiter;
		Bill bill;
		Double payment;
		public MyCustomer(CustomerAgent cta, WaiterAgent wtr, Bill bill) {
			customer = cta;
			waiter = wtr;
			this.bill = bill;
			payment = 0.0;
		}
	}
	
	private class Bill {
		Double price;
		BillStatus status;
		public Bill(Double price, BillStatus status) {
			this.price = price;
			this.status = status;
		}
	}
	
	private class MyMarket {
		MarketAgent market;
		BillStatus status;
		Double bill;
		public MyMarket(MarketAgent market, BillStatus status, Double bill) {
			this.market = market;
			this.status = status;
			this.bill = bill;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return "cashier " + getName();
	}
}

