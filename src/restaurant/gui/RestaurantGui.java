package restaurant.gui;

import restaurant.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.io.File;


/** Main GUI class.
 * Contains the main frame and subsequent panels */
public class RestaurantGui extends JFrame implements ActionListener{

	private final int WINDOWX = 1050;
	private final int WINDOWY = 450;

	private RestaurantPanel restPanel = new RestaurantPanel(this);
	private JPanel infoPanel = new JPanel();
	private JLabel infoLabel = new JLabel(
			"<html><pre><i>(Click on a customer/waiter)</i></pre></html>");
	private JCheckBox stateCB_1 = new JCheckBox();
	private JCheckBox stateCB_2 = new JCheckBox();
	private JCheckBox stateCB_3 = new JCheckBox();
	private JCheckBox stateCB_4 = new JCheckBox();
	private JCheckBox stateCB_5 = new JCheckBox();
	private JButton addTable = new JButton("Add Table");

	private Object currentPerson;

	/** Constructor for RestaurantGui class.
	 * Sets up all the gui components. */
	public RestaurantGui(){

		super("Restaurant Application");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50,50, WINDOWX, WINDOWY);

		getContentPane().setLayout(new BoxLayout((Container)getContentPane(),BoxLayout.Y_AXIS));

		Dimension rest = new Dimension(WINDOWX, (int)(WINDOWY*.6));
		Dimension info = new Dimension(WINDOWX, (int)(WINDOWY*.25));
		restPanel.setPreferredSize(rest);
		restPanel.setMinimumSize(rest);
		restPanel.setMaximumSize(rest);
		infoPanel.setPreferredSize(info);
		infoPanel.setMinimumSize(info);
		infoPanel.setMaximumSize(info);
		infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

		stateCB_1.setVisible(false);
		stateCB_1.addActionListener(this);
		stateCB_2.setVisible(false);
		stateCB_2.addActionListener(this);
		stateCB_3.setVisible(false);
		stateCB_3.addActionListener(this);
		stateCB_4.setVisible(false);
		stateCB_4.addActionListener(this);
		stateCB_5.setVisible(false);
		stateCB_5.addActionListener(this);

		infoPanel.setLayout(new GridLayout(1,2, 30,0));
		infoPanel.add(infoLabel);
		infoPanel.add(stateCB_1);
		infoPanel.add(stateCB_2);
		infoPanel.add(stateCB_3);
		infoPanel.add(stateCB_4);
		infoPanel.add(stateCB_5);

		getContentPane().add(restPanel);
		getContentPane().add(addTable);
		getContentPane().add(infoPanel);

		addTable.addActionListener(this);
	}


	/** This function takes the given customer or waiter object and 
	 * changes the information panel to hold that person's info.
	 * @param person customer or waiter object */
	public void updateInfoPanel(Object person){
		stateCB_1.setVisible(true);
		currentPerson = person;
		stateCB_2.setVisible(false);
		stateCB_3.setVisible(false);
		stateCB_4.setVisible(false);
		stateCB_5.setVisible(false);
		if(person instanceof CustomerAgent){
			CustomerAgent customer = (CustomerAgent) person;
			stateCB_1.setText("Hungry?");
			stateCB_1.setSelected(customer.isHungry());
			stateCB_1.setEnabled(!customer.isHungry());
			stateCB_2.setText("Change Order?");
			stateCB_2.setVisible(true);
			stateCB_2.setEnabled(true);
			stateCB_2.setSelected(customer.getChangeOrder());
			stateCB_3.setText("Waiting?");
			stateCB_3.setVisible(true);
			stateCB_3.setEnabled(true);
			stateCB_3.setSelected(customer.isWaiting()); 
			stateCB_4.setText("Enough Money?");
			stateCB_4.setVisible(true);
			stateCB_4.setEnabled(true);
			stateCB_4.setSelected(customer.isEnoughMoney()); 
			stateCB_5.setText("Cheapest Order?");
			stateCB_5.setVisible(true);
			stateCB_5.setEnabled(customer.isEnoughMoney());
			stateCB_5.setSelected(customer.getCheapestOrder()); 
			infoLabel.setText(
					"<html><pre>     Name: " + customer.getName() + " </pre></html>");

		}else if(person instanceof WaiterAgent){
			WaiterAgent waiter = (WaiterAgent) person;
			stateCB_1.setText("On Break?");
			stateCB_1.setSelected(waiter.isOnBreak());
			stateCB_1.setEnabled(true);
			infoLabel.setText(
					"<html><pre>     Name: " + waiter.getName() + " </html>");
		}else if (person instanceof HostAgent) {
			HostAgent host = (HostAgent) person;
			stateCB_1.setText("Allow Break?");
			stateCB_1.setSelected(host.getOverallAllowBreak());
			stateCB_1.setEnabled(true);
			infoLabel.setText(
					"<html><pre>     Name: " + host.getName() + " </html>");
		}else if (person instanceof MarketAgent) {
			MarketAgent market = (MarketAgent) person;
			stateCB_1.setText("Have Enough Inventory?");
			stateCB_1.setSelected(market.getEnoughInventory());
			stateCB_1.setEnabled(true);
			infoLabel.setText(
					"<html><pre>     Name: " + market.getName() + " </html>");
		}else if (person instanceof CookAgent) {
			CookAgent cook = (CookAgent) person;
			stateCB_2.setVisible(true);
			stateCB_3.setVisible(true);
			stateCB_1.setText("Wait before cooking?");
			stateCB_1.setSelected(cook.getWaiting());
			stateCB_1.setEnabled(true);
			stateCB_2.setText("Inventory below limit?");
			stateCB_2.setSelected(cook.getUnderLimit());
			stateCB_2.setEnabled(true);
			stateCB_3.setText("Running out of food?");
			stateCB_3.setSelected(cook.getUnderLimit());
			stateCB_3.setEnabled(true);
			infoLabel.setText(
					"<html><pre>     Name: " + cook.getName() + " </html>");
		}

		infoPanel.validate();
	}

	/** Action listener method that reacts to the checkbox being clicked */
	public void actionPerformed(ActionEvent e){

		if(e.getSource() == stateCB_1){
			if(currentPerson instanceof CustomerAgent){
				CustomerAgent c = (CustomerAgent) currentPerson;
				c.setHungry();
				stateCB_1.setEnabled(false);
			}else if(currentPerson instanceof WaiterAgent){
				WaiterAgent w = (WaiterAgent) currentPerson;
				w.setBreakStatus(stateCB_1.isSelected());
			}else if (currentPerson instanceof HostAgent) {
				HostAgent host = (HostAgent) currentPerson;
				host.setOverallAllowBreak(stateCB_1.isSelected());
			}else if (currentPerson instanceof MarketAgent) {
				MarketAgent market = (MarketAgent) currentPerson;
				market.setEnoughInventory(stateCB_1.isSelected());
			}else if (currentPerson instanceof CookAgent) {
				CookAgent market = (CookAgent) currentPerson;
				market.setWaiting(stateCB_1.isSelected());
			}
		}
		else if (e.getSource() == addTable) {
			try {
				System.out.println("[Gautam] Add Table!");
				//String XPos = JOptionPane.showInputDialog("Please enter X Position: ");
				//String YPos = JOptionPane.showInputDialog("Please enter Y Position: ");
				//String size = JOptionPane.showInputDialog("Please enter Size: ");
				//restPanel.addTable(10, 5, 1);
				//restPanel.addTable(Integer.valueOf(YPos).intValue(), Integer.valueOf(XPos).intValue(), Integer.valueOf(size).intValue());
				restPanel.addTable();
			}
			catch(Exception ex) {
				System.out.println("Unexpected exception caught in during setup:"+ ex);
			}
		}else if (e.getSource() == stateCB_2) {
			if(currentPerson instanceof CustomerAgent){
				CustomerAgent c = (CustomerAgent) currentPerson;
				c.setChangeOrder(stateCB_2.isSelected());
			}else if (currentPerson instanceof CookAgent) {
				CookAgent c = (CookAgent) currentPerson;
				c.setUnderLimit(stateCB_2.isSelected());
			}
		}else if (e.getSource() == stateCB_3) {
			if (currentPerson instanceof CookAgent) {
				CookAgent c = (CookAgent) currentPerson;
				c.setRunOutOfFood(stateCB_3.isSelected());
			}else if (currentPerson instanceof CustomerAgent) {
				CustomerAgent c = (CustomerAgent) currentPerson;
				c.setWaiting(stateCB_3.isSelected());
			}
		}else if (e.getSource() == stateCB_4) {
			if (currentPerson instanceof CustomerAgent) {
				CustomerAgent c = (CustomerAgent) currentPerson;
				c.setEnoughMoney(stateCB_4.isSelected());
				stateCB_5.setEnabled(c.isEnoughMoney());
			}
		}else if (e.getSource() == stateCB_5) {
			if (currentPerson instanceof CustomerAgent) {
				CustomerAgent c = (CustomerAgent) currentPerson;
				c.setCheapestOrder(stateCB_5.isSelected());
			}
		}

	}

	/** Message sent from a customer agent to enable that customer's 
	 * "I'm hungery" checkbox.
	 * @param c reference to the customer */
	public void setCustomerEnabled(CustomerAgent c){
		if(currentPerson instanceof CustomerAgent){
			CustomerAgent cust = (CustomerAgent) currentPerson;
			if(c.equals(cust)){
				stateCB_1.setEnabled(true);
				stateCB_1.setSelected(false);
			}
		}
	}

	public void setstateCB(boolean state) {
		stateCB_1.setSelected(state);
	}



	/** Main routine to get gui started */
	public static void main(String[] args){
		RestaurantGui gui = new RestaurantGui();
		gui.setVisible(true);
		gui.setResizable(false);
		//gui.restPanel.hostPanel.addPerson("Test");
	}
}
