package restaurant.test;

import restaurant.Menu;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Host;
import restaurant.interfaces.Waiter;
import restaurant.layoutGUI.GuiCustomer;

public class MockCustomer implements Customer{
	public EventLog events = new EventLog();
	@Override
	public void setHungry() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgFollowMeToTable(Waiter waiter, Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDecided() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgWhatWouldYouLike() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsYourFood(String choice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDoneEating() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsMyBill(Double bill) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsChange(Double change) {
		events.add(new LoggedEvent("Change" + String.format("%.2f", change)));
		
	}

	@Override
	public void msgFullRightNow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgLeaving() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgPleaseReorder(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDecisionOnChangeOrder(boolean decision) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChangeOrder(boolean change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCheapestOrder(boolean Cheapset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgWashingDishes(double days) {
		events.add(new LoggedEvent("Dishes" + String.format("%.2f", days)));
		
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
	public String getName() {
		// TODO Auto-generated method stub
		return "MockCustomer";
	}

	@Override
	public GuiCustomer getGuiCustomer() {
		// TODO Auto-generated method stub
		return null;
	}

}
