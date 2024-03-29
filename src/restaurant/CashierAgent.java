package restaurant;

import agent.Agent;
import java.util.*;
import restaurant.interfaces.*;
import restaurant.test.*;

public class CashierAgent extends Agent implements Cashier{
	public enum BillStatus {Ready, Pending, Received, Cleared};
	public List<MyCustomer> customers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	public List<MyMarket> markets = Collections.synchronizedList(new ArrayList<MyMarket>());
	Menu menu;
	String name;
	Timer t = new Timer();
	public CashierAgent(String name) {
		this.name = name;
		menu = new Menu();
	}
	public EventLog events = new EventLog();
	//Messaging
	public void msgMakeBill(Waiter wtr, Customer cta, String choice) {
		//print("Making Bill for " + cta.getName());
		//System.out.println(menu.choices.get(choice));
		Bill bill = new Bill(menu.choices.get(choice), BillStatus.Ready);
		synchronized (customers) {
			for (MyCustomer c : customers) {
				if (c.customer.equals(cta)) {
					//print("Old Customer");
					c.bill = bill;
					stateChanged();
					return;
				}
			}
		}
		customers.add(new MyCustomer(cta, wtr, bill));
		stateChanged();
	}

	public void msgHereIsPayment(Customer cta, Double payment) {
		synchronized (customers) {
			for (MyCustomer c : customers) {
				if (c.customer.equals(cta)) {
					c.bill.status = BillStatus.Received;
					c.payment = payment;
				}
			}
		}
		stateChanged();
	}

	public void msgHereIsBill(Market mka, Double amount) {
		markets.add(new MyMarket(mka, BillStatus.Ready, amount));
		stateChanged();
	}
	@Override
	public boolean pickAndExecuteAnAction() {
		MyCustomer customer = null;
		synchronized (customers) {
			for (MyCustomer c : customers) {
				if (c.bill.status == BillStatus.Ready) {
					customer = c;
					break;
				}
			}
		}
		if (customer != null) {
			makeBill(customer);
			return true;
		}
		customer = null;
		synchronized (customers) {
			for (MyCustomer c : customers) {
				if (c.bill.status == BillStatus.Received) {
					customer = c;
					break;
				}
			}
		}
		if (customer != null) {
			makeChange(customer);
			return true;
		}
		MyMarket market = null;
		synchronized (markets) {
			for (MyMarket m : markets) {
				if (m.status == BillStatus.Ready) {
					market = m;
					break;
				}
			}
		}
		if (market != null) {
			payToMarket(market);
			return true;
		}
		return false;
	}
	//Actions
	private void makeBill(MyCustomer c) {
		//print("Called");
		print("Making bill for " + c.customer.getName() + " (1000 milliseconds)");
		events.add(new LoggedEvent("Making bill for " + c.customer.getName() + " (1000 milliseconds)"));
		try {
			Thread.sleep(1000);
		}catch (InterruptedException e) {}
		c.waiter.msgHereIsBill(c.customer, c.bill.price);
		c.bill.status = BillStatus.Pending;
		stateChanged();
	}

	private void makeChange(MyCustomer c) {
		print("Receiving Payment from " + c.customer.getName() + " of $" + c.payment);
		events.add(new LoggedEvent("Receiving Payment from " + c.customer.getName() + " of $" + c.payment));
		if (c.payment - c.bill.price >= 0)
			c.customer.msgHereIsChange(c.payment - c.bill.price);
		else {
			print("Fuck You!");
			events.add(new LoggedEvent("Fuck You!"));
			c.customer.msgWashingDishes(c.bill.price - c.payment + 1);
		}
		c.bill.status = BillStatus.Cleared;
		stateChanged();
	}

	private void payToMarket(MyMarket m) {
		print(String.format("Paying to %s %.2f", m.market.getName(), m.bill));
		events.add(new LoggedEvent(String.format("Paying to %s %.2f", m.market.getName(), m.bill)));
		m.market.msgHereIsPayment(m.bill);
		m.status = BillStatus.Cleared;
		stateChanged();
	}
	public class MyCustomer {
		Customer customer;
		Waiter waiter;
		public Bill bill;
		public Double payment;
		public MyCustomer(Customer cta, Waiter wtr, Bill bill) {
			customer = cta;
			waiter = wtr;
			this.bill = bill;
			payment = 0.0;
		}
	}

	public class Bill {
		public Double price;
		public BillStatus status;
		public Bill(Double price, BillStatus status) {
			this.price = price;
			this.status = status;
		}
	}

	public class MyMarket {
		Market market;
		public BillStatus status;
		public Double bill;
		public MyMarket(Market market, BillStatus status, Double bill) {
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

