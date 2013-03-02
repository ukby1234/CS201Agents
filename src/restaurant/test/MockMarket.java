package restaurant.test;

import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Market;
public class MockMarket implements Market{
	public EventLog events = new EventLog();
	@Override
	public void msgOrder(Cook cook, String choice, int amount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsPayment(Double amount) {
		events.add(new LoggedEvent("Payment" + String.format("%.2f", amount)));
	}

	@Override
	public void setEnoughInventory(boolean enough) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCashier(Cashier cashier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MockMarket";
	}

}
