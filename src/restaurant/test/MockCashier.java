package restaurant.test;

import java.util.*;
import restaurant.*;
import restaurant.interfaces.*;

public class MockCashier implements Cashier{
	public EventLog events = new EventLog();
	enum BillStatus {Ready, Pending, Received, Cleared};
	public List<MyCustomer> customers =new ArrayList<MyCustomer>();
	public List<MyMarket> markets = new ArrayList<MyMarket>();
	public Menu menu = new Menu();
	@Override
	public void msgMakeBill(Waiter wtr, Customer cta, String choice) {
		events.add(new LoggedEvent("MakeBill" + wtr.getName() + cta.getName() + choice));
		Bill bill = new Bill(menu.choices.get(choice), BillStatus.Ready);
		customers.add(new MyCustomer(cta, wtr, bill));
	}

	@Override
	public void msgHereIsPayment(Customer cta, Double payment) {
		events.add(new LoggedEvent("ReceivePayment" + cta.getName() + String.format("%.2f", payment)));
		for (MyCustomer c : customers) {
			if (c.customer.equals(cta)) {
				c.bill.status = BillStatus.Received;
				c.payment = payment;
			}
		}
	}

	@Override
	public void msgHereIsBill(Market mka, Double amount) {
		events.add(new LoggedEvent("MakeBillMarket" + mka.getName() + String.format("%.2f", amount)));
		markets.add(new MyMarket(mka, BillStatus.Ready, amount));
	}

	@Override
	public String getName() {
		return "MockCashier";
	}

	public boolean pickAndExecuteAnAction() {
		for (MyCustomer c : customers) {
			if (c.bill.status == BillStatus.Ready) {
				makeBill(c);
				return true;
			}
		}

		for (MyCustomer c : customers) {
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
	
	public void addCustomer(Customer cta, Waiter wtr, Double price) {
		customers.add(new MyCustomer(cta, wtr, new Bill(price, BillStatus.Ready)));
	}
	
	public void addMarket(Market market, Double bill) {
		markets.add(new MyMarket(market, BillStatus.Ready, bill));
	}

	private void payToMarket(MyMarket m) {
		m.market.msgHereIsPayment(m.bill);
		m.status = BillStatus.Cleared;
	}

	private void makeChange(MyCustomer c) {
		if (c.payment - c.bill.price >= 0)
			c.customer.msgHereIsChange(c.payment - c.bill.price);
		else
			c.customer.msgWashingDishes(c.bill.price - c.payment + 1);
		c.bill.status = BillStatus.Cleared;
	}

	private void makeBill(MyCustomer c) {
		c.waiter.msgHereIsBill(c.customer, c.bill.price);
		c.bill.status = BillStatus.Pending;
	}

	class MyCustomer {
		Customer customer;
		Waiter waiter;
		Bill bill;
		Double payment;
		public MyCustomer(Customer cta, Waiter wtr, Bill bill) {
			customer = cta;
			waiter = wtr;
			this.bill = bill;
			payment = 0.0;
		}
	}

	class Bill {
		Double price;
		BillStatus status;
		public Bill(Double price, BillStatus status) {
			this.price = price;
			this.status = status;
		}
	}

	class MyMarket {
		Market market;
		BillStatus status;
		Double bill;
		public MyMarket(Market market, BillStatus status, Double bill) {
			this.market = market;
			this.status = status;
			this.bill = bill;
		}
	}

}
