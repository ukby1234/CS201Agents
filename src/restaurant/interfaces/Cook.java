package restaurant.interfaces;

public interface Cook {
	public void msgHereIsAnOrder(Waiter waiter, int tableNum, String choice);
	public void msgDelivery(String type, int amount);
	public void msgchangeOrder(Waiter waiter, int tableNum, String choice);
	public String getName();
	public void setWaiting(boolean waiting);
	public void setUnderLimit(boolean limit);
	public void setRunOutOfFood(boolean RunOutOfFood);
	public void addMarket(Market market);
	public boolean getRunOutOfFood();
}
