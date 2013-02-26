package restaurant.interfaces;

import restaurant.Menu;
import restaurant.layoutGUI.GuiCustomer;

public interface Customer {
	public void setHungry();
	public void msgFollowMeToTable(Waiter waiter, Menu menu);
	public void msgDecided();
	public void msgWhatWouldYouLike();
	public void msgHereIsYourFood(String choice);
	public void msgDoneEating();
	public void msgHereIsMyBill(Double bill);
	public void msgHereIsChange(Double change);
	public void msgFullRightNow();
	public void msgLeaving();
	public void msgPleaseReorder(Menu menu);
	public void msgDecisionOnChangeOrder(boolean decision);
	public void setChangeOrder(boolean change);
	public void setCheapestOrder(boolean Cheapset);
	public void msgWashingDishes(double days);
	public void setCashier(Cashier cashier);
	public void setHost(Host host);
	public String getName();
	public GuiCustomer getGuiCustomer();
	
}
