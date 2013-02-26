package restaurant.interfaces;

public interface Host {
	public void msgIWantToEat(Customer customer);
	public void msgTableIsFree(int tableNum);
	public void msgCanIOnBreak(Waiter w);
	public void msgResumeWork(Waiter w);
	public void msgStayOrLeave(Customer c, boolean choice);
	public String getName();
	public void setOverallAllowBreak(boolean overall);
	public void setWaiter(Waiter waiter);
}
