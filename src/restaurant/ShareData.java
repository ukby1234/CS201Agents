package restaurant;
import java.util.*;
public class ShareData {
	private List<ShareOrder> orders = Collections.synchronizedList(new ArrayList<ShareOrder>(size));
	static int size = 2;
	private int count = 0;
	public synchronized void addItem(WaiterAgent waiter, int tableNum, String choice) {
		while (count == size) {
			try {
				wait();
			}catch (InterruptedException e) {}
		}
		orders.add(new ShareOrder(waiter, tableNum, choice));
		count++;
		if (count == 1)
			notify();
	}
	
	public synchronized ShareOrder removeItem() {
		while (count == 0) {
			try {
				wait();
			}catch (InterruptedException e) {}
		}
		count--;
		if (count == size - 1)
			notify();
		return orders.remove(0);
	}
}
