package restaurant.test;

import static org.junit.Assert.*;
import junit.framework.TestCase;
import org.junit.Test;
import restaurant.test.MockCashier.*;

public class CashierTest extends TestCase{

	@Test
	public void testCustomerEnoughMoney() {
		MockCustomer customer = new MockCustomer();
		MockCashier cashier = new MockCashier();
		assertTrue(cashier.customers.isEmpty());
		cashier.addCustomer(customer, null, 30.0);
		assertFalse(cashier.customers.isEmpty());
		cashier.msgHereIsPayment(customer, 100.0);
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(0).payment, 100.0);
		assertTrue(cashier.events.containsString("ReceivePaymentMockCustomer100.00"));
		cashier.pickAndExecuteAnAction();
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
		assertTrue(customer.events.containsString("Change70.00"));
	}
	
	@Test
	public void testCustomerNotEnoughMoney() {
		MockCustomer customer = new MockCustomer();
		MockCashier cashier = new MockCashier();
		assertTrue(cashier.customers.isEmpty());
		cashier.addCustomer(customer, null, 30.0);
		assertFalse(cashier.customers.isEmpty());
		cashier.msgHereIsPayment(customer, 20.0);
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Received);
		assertEquals(cashier.customers.get(0).payment, 20.0);
		assertTrue(cashier.events.containsString("ReceivePaymentMockCustomer20.00"));
		cashier.pickAndExecuteAnAction();
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Cleared);
		assertTrue(customer.events.containsString("Dishes11.00"));
	}
	
	@Test
	public void testWaiter() {
		MockWaiter waiter = new MockWaiter();
		MockCashier cashier = new MockCashier();
		MockCustomer customer = new MockCustomer();
		assertTrue(cashier.customers.isEmpty());
		cashier.msgMakeBill(waiter, customer, "Steak");
		assertFalse(cashier.customers.isEmpty());
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Ready);
		assertEquals(cashier.customers.get(0).bill.price, 15.99);
		cashier.pickAndExecuteAnAction();
		assertTrue(waiter.events.containsString("MockCustomer15.99"));
		assertEquals(cashier.customers.get(0).bill.status, BillStatus.Pending);
	}
	
	@Test
	public void testMarket() {
		MockCashier cashier = new MockCashier();
		MockMarket market = new MockMarket();
		assertTrue(cashier.markets.isEmpty());
		cashier.msgHereIsBill(market, 100.0);
		assertFalse(cashier.markets.isEmpty());
		assertTrue(cashier.events.containsString("MakeBillMarketMockMarket100.00"));
		assertEquals(cashier.markets.get(0).bill, 100.0);
		assertEquals(cashier.markets.get(0).status, BillStatus.Ready);
		cashier.pickAndExecuteAnAction();
		market.events.containsString("Payment100.00");
		assertEquals(cashier.markets.get(0).status, BillStatus.Cleared);
	}

}
