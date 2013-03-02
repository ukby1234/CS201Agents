package restaurant.test;

import junit.framework.TestCase;
import org.junit.Test;
import java.util.*;
import restaurant.*;
import restaurant.CashierAgent.*;

public class CashierTest extends TestCase{

	@Test
	public void testCustomerEnoughMoney() {
		MockCustomer customer = new MockCustomer();
		MockWaiter waiter = new MockWaiter();
		CashierAgent cashier = new CashierAgent("Cashier");
		assertTrue(cashier.customers.isEmpty());
		cashier.msgMakeBill(waiter, customer, "Steak");
		assertFalse(cashier.customers.isEmpty());
		cashier.msgHereIsPayment(customer, 100.0);
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(0).payment, 100.0);
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Receiving Payment from MockCustomer of $100.0"));
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
		assertTrue(customer.events.containsString("Change84.01"));
	}
	
	@Test
	public void testCustomerOtherFood() {
		MockCustomer customer = new MockCustomer();
		MockWaiter waiter = new MockWaiter();
		CashierAgent cashier = new CashierAgent("Cashier");
		assertTrue(cashier.customers.isEmpty());
		cashier.msgMakeBill(waiter, customer, "Chicken");
		assertFalse(cashier.customers.isEmpty());
		cashier.msgHereIsPayment(customer, 100.0);
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(0).payment, 100.0);
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Receiving Payment from MockCustomer of $100.0"));
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
		assertTrue(customer.events.containsString("Change89.01"));
		
		cashier.msgMakeBill(waiter, customer, "Salad");
		assertFalse(cashier.customers.isEmpty());
		cashier.msgHereIsPayment(customer, 100.0);
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(0).payment, 100.0);
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Receiving Payment from MockCustomer of $100.0"));
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
		assertTrue(customer.events.containsString("Change94.01"));
		
		cashier.msgMakeBill(waiter, customer, "Pizza");
		assertFalse(cashier.customers.isEmpty());
		cashier.msgHereIsPayment(customer, 100.0);
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(0).payment, 100.0);
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Receiving Payment from MockCustomer of $100.0"));
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
		assertTrue(customer.events.containsString("Change91.01"));
	}
	
	@Test
	public void testTwoCustomer() {
		MockCustomer customer1 = new MockCustomer();
		MockCustomer customer2 = new MockCustomer();
		MockWaiter waiter = new MockWaiter();
		CashierAgent cashier = new CashierAgent("Cashier");
		assertTrue(cashier.customers.isEmpty());
		cashier.msgMakeBill(waiter, customer1, "Chicken");
		cashier.msgMakeBill(waiter, customer2, "Salad");
		cashier.msgHereIsPayment(customer1, 100.0);
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(0).payment, 100.0);
		cashier.msgHereIsPayment(customer2, 100.0);
		assertEquals(cashier.customers.get(1).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(1).payment, 100.0);
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Receiving Payment from MockCustomer of $100.0"));
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
		assertTrue(customer1.events.containsString("Change89.01"));
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Receiving Payment from MockCustomer of $100.0"));
		assertEquals(cashier.customers.get(1).bill.status, BillStatus.Cleared);
		assertTrue(customer2.events.containsString("Change94.01"));
	}
	
	@Test
	public void testMultipleCustomer() {
		MockWaiter waiter = new MockWaiter();
		CashierAgent cashier = new CashierAgent("Cashier");
		List<MockCustomer> customers = new ArrayList<MockCustomer>();
		Menu menu = new Menu();
		Object[] choices = menu.choices.keySet().toArray();
		final int number = 25;
		assertTrue(cashier.customers.isEmpty());
		for (int i = 0; i < number; i++) {
			customers.add(new MockCustomer());
			cashier.msgMakeBill(waiter, customers.get(i), (String)choices[i % 4]);
			cashier.msgHereIsPayment(customers.get(i), i * 10.0);
			assertEquals(cashier.customers.get(i).bill.status, BillStatus.Received);
			assertEquals(cashier.customers.get(i).payment, i * 10.0);
			cashier.pickAndExecuteAnAction();
			assertTrue(cashier.events.containsString("Receiving Payment from MockCustomer of $" + i * 10.0));
			if (i * 10.0 < menu.choices.get((String)choices[i % 4]))
				assertTrue(cashier.events.containsString("Fuck You!"));
			assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
			if (i * 10.0 >= menu.choices.get((String)choices[i % 4]))
				assertTrue(customers.get(i).events.containsString("Change" + String.format("%.2f", i * 10.0 - menu.choices.get((String)choices[i % 4]))));
			else
				assertTrue(customers.get(i).events.containsString("Dishes" + String.format("%.2f", 1 - i * 10.0 + menu.choices.get((String)choices[i % 4]))));
		}
	}
	
	@Test
	public void testCustomerNotEnoughMoney() {
		MockCustomer customer = new MockCustomer();
		MockWaiter waiter = new MockWaiter();
		CashierAgent cashier = new CashierAgent("Cashier");
		assertTrue(cashier.customers.isEmpty());
		cashier.msgMakeBill(waiter, customer, "Steak");
		assertFalse(cashier.customers.isEmpty());
		cashier.msgHereIsPayment(customer, 0.0);
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(0).payment, 0.0);
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Receiving Payment from MockCustomer of $0.0"));
		assertTrue(cashier.events.containsString("Fuck You!"));
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
		assertTrue(customer.events.containsString("Dishes16.99"));
	}
	
	@Test
	public void testWaiter() {
		MockWaiter waiter = new MockWaiter();
		CashierAgent cashier = new CashierAgent("Cashier");
		MockCustomer customer = new MockCustomer();
		assertTrue(cashier.customers.isEmpty());
		cashier.msgMakeBill(waiter, customer, "Steak");
		assertFalse(cashier.customers.isEmpty());
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Ready);
		assertEquals(cashier.customers.get(0).bill.price, 15.99);
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Making bill for MockCustomer (1000 milliseconds)"));
		assertTrue(waiter.events.containsString("MockCustomer15.99"));
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Pending);
	}
	
	@Test
	public void testMarket() {
		CashierAgent cashier = new CashierAgent("Cashier");
		MockMarket market = new MockMarket();
		assertTrue(cashier.markets.isEmpty());
		cashier.msgHereIsBill(market, 100.0);
		assertFalse(cashier.markets.isEmpty());
		assertEquals(cashier.markets.get(0).bill, 100.0);
		assertEquals(cashier.markets.get(0).status, BillStatus.Ready);
		cashier.pickAndExecuteAnAction();
		assertTrue(cashier.events.containsString("Paying to MockMarket 100.00"));
		market.events.containsString("Payment100.00");
		assertEquals(cashier.markets.get(0).status, BillStatus.Cleared);
	}

}
