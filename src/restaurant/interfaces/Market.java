package restaurant.interfaces;


public interface Market {
	public void msgOrder(Cook cook, String choice, int amount);
	public void msgHereIsPayment(Double amount);
	public void setEnoughInventory(boolean enough);
	public void setCashier(Cashier cashier);
	public String getName();
}
