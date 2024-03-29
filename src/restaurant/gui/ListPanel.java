package restaurant.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;


/** Panel in the contained in the restaurantPanel.
 * This holds the scroll panes for the customers and waiters */
public class ListPanel extends JPanel implements ActionListener{

	public JScrollPane pane = 
			new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private JPanel view = new JPanel();
	private Vector<JButton> list = new Vector<JButton>();
	private JButton addPersonB = new JButton("Add");
	private JButton addPersonC = new JButton("Add ShareData");

	private RestaurantPanel restPanel;
	private String type;
	/** Constructor for ListPanel.  Sets up all the gui
	 * @param rp reference to the restaurant panel
	 * @param type indicates if this is for customers or waiters */
	public ListPanel(RestaurantPanel rp, String type){
		restPanel = rp;
		this.type = type;

		setLayout(new BoxLayout((Container) this, BoxLayout.Y_AXIS));
		add(new JLabel("<html><pre> <u>"+type+ "</u><br></pre></html>"));

		addPersonB.addActionListener(this);
		add(addPersonB);
		if (type.equals("Waiters")) {
			addPersonC.addActionListener(this);
			add(addPersonC);
		}
		if (type.equals("Host") || type.equals("Market") || type.equals("Cook"))
			addPersonB.setEnabled(false);
		if (type.equals("Waiters"))
			addPersonB.setText("Add Normal");
		view.setLayout(new BoxLayout((Container) view, BoxLayout.Y_AXIS));
		pane.setViewportView(view);
		add(pane);
	}

	/** Method from the ActionListener interface. 
	 * Handles the event of the add button being pressed */
	public void actionPerformed(ActionEvent e){

		if(e.getSource() == addPersonB) {
			addPerson(JOptionPane.showInputDialog("Please enter a name:"), type.equals("Waiters") ? "Normal" : type);
		}
		else if(e.getSource() == addPersonC) {
			addPerson(JOptionPane.showInputDialog("Please enter a name:"), type.equals("Waiters") ? "Share" : type);
		}
		else {

			for(int i=0; i < list.size(); i++){
				JButton temp = list.get(i);

				if(e.getSource() == temp)
					restPanel.showInfo(type, temp.getText());		
			}
		}
	}

	/** If the add button is pressed, this function creates 
	 * a spot for it in the scroll pane, and tells the restaurant panel 
	 * to add a new person.
	 * @param name name of new person */
	public void addPerson(String name, String type){
		if(name != null){
			try {
				String c;
				if (type.equals("Customers")) c="c"; else c="w"; 
				int n = Integer.valueOf( name ).intValue();
				for (int i=1; i<=n; i++) createIt(c+i, type);
			}
			catch (NumberFormatException e) {
				createIt(name, type);
			}
		}
	}
	void createIt(String name, String type){
		//System.out.println("createIt name="+name+"XX"); 
		JButton button = new JButton(name);
		button.setBackground(Color.white);

		Dimension paneSize = pane.getSize();
		Dimension buttonSize = new Dimension(paneSize.width-20, 
				(int)(paneSize.height/7));
		button.setPreferredSize(buttonSize);
		button.setMinimumSize(buttonSize);
		button.setMaximumSize(buttonSize);
		button.addActionListener(this);
		list.add(button);
		synchronized (view) {
			view.add(button);
		}
		//System.out.println(pane.getSize());
		restPanel.addPerson(type, name);
		validate();
	}

	public void startThread(){
		ChangeButtonSize thread = new ChangeButtonSize();
		thread.start();
	}

	private class ChangeButtonSize extends Thread {
		public void run() {
			while(checkButtons());
			//System.out.println("Exit");
		}
		private boolean checkButtons() {
			boolean flag = false;
			synchronized (view) {
				for (Component c : view.getComponents())
					if (c instanceof JButton) {
						JButton button = (JButton) c;
						if(button.getHeight() == 0 && button.getWidth() == 0) {
							flag = true;
							Dimension paneSize = pane.getSize();
							Dimension buttonSize = new Dimension(paneSize.width-20, 
									(int)(paneSize.height/7));
							button.setPreferredSize(buttonSize);
							button.setMinimumSize(buttonSize);
							button.setMaximumSize(buttonSize);
							pane.revalidate();
						}
					}
			}
			return flag;
		}

	}

}
