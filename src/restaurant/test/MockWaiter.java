package restaurant.test;

import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Host;
import restaurant.interfaces.Waiter;
import restaurant.layoutGUI.Food;

public class MockWaiter implements Waiter{
	public EventLog events = new EventLog();
	@Override
	public void msgSitCustomerAtTable(Customer customer, int tableNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgImReadyToOrder(Customer customer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsMyChoice(Customer customer, String choice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgOrderIsReady(int tableNum, Food f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDoneEatingAndLeaving(Customer customer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDecisionOnBreak(boolean working) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgCookReorder(int tableNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgCustReorder(Customer customer, String choice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgLeaving(Customer customer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDecisionChangeOrder(int tableNum, boolean approved) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgImReadyToReorder(Customer customer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBreakStatus(boolean state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsBill(Customer customer, Double bill) {
		events.add(new LoggedEvent(customer.getName() + String.format("%.2f", bill)));
		
	}

	@Override
	public void setCashier(Cashier cashier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHost(Host host) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCook(Cook cook) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MockWaiter";
	}

}
