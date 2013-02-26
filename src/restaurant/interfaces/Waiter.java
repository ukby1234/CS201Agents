package restaurant.interfaces;

import restaurant.layoutGUI.Food;

public interface Waiter {
	public void msgSitCustomerAtTable(Customer customer, int tableNum);
	public void msgImReadyToOrder(Customer customer);
	public void msgHereIsMyChoice(Customer customer, String choice);
	public void msgOrderIsReady(int tableNum, Food f);
	public void msgDoneEatingAndLeaving(Customer customer);
	public void msgDecisionOnBreak(boolean working);
	public void msgCookReorder(int tableNum);
	public void msgCustReorder(Customer customer, String choice);
	public void msgLeaving(Customer customer);
	public void msgDecisionChangeOrder(int tableNum, boolean approved);
	public void msgImReadyToReorder(Customer customer);
	public void setBreakStatus(boolean state);
	public void msgHereIsBill(Customer customer, Double bill);
	public void setCashier(Cashier cashier);
	public void setHost(Host host);
	public void setCook(Cook cook);
	public String getName();
}
