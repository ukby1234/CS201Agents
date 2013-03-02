package restaurant;

public class ShareOrder {
	public WaiterAgent waiter;
	public int tableNum;
	public String choice;
	public ShareOrder(WaiterAgent waiter, int tableNum, String choice) {
		this.waiter = waiter;
		this.tableNum = tableNum;
		this.choice = choice;
	}
}
