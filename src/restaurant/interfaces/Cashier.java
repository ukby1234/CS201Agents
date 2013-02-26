package restaurant.interfaces;

public interface Cashier {
	public void msgMakeBill(Waiter wtr, Customer cta, String choice);
	public void msgHereIsPayment(Customer cta, Double payment);
	public void msgHereIsBill(Market mka, Double amount);
	public String getName();
}
