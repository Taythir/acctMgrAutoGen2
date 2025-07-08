package acctMgr.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.Iterator;
import acctMgr.model.AccountList;
import acctMgr.model.Account;
import acctMgr.model.ModelEvent;
//import acctMgr.util.AgentManager;
//import acctMgr.controller.AccountController;
//import acctMgr.controller.AccountController;
import acctMgr.controller.AccountListController;
//import acctMgr.controller.AgentController;

public class AccountListView extends JFrameView {

	/**
	 * Localized interface string.
	 */
	public static final String DOLLARS = "Edit account in $";
	
	/**
	 * Localized interface string.
	 */
	public static final String EUROS = "Edit account in \u20ac";
	
	/**
	 * Localized interface string.
	 */
	public static final String YEN = "Edit account in \u00a5";
	
	/**
	 * Localized interface string.
	 */
	public static final String DEPOSITAGENT = "Create Deposit agent";
	
	/**
	 * Localized interface string.
	 */
	public static final String WITHDRAWAGENT = "Create Withdraw agent";
	
	/**
	 * Localized interface string.
	 */
	public static final String SAVE = "Save";
	
	/**
	 * Localized interface string.
	 */
	public static final String EXIT = "Exit";

	/**
	 * Account list interface element.
	 */
	private final JComboBox<String> accountListComboBox;
	private final JPanel topPanel;
	private final JPanel editPanel;
	private final JPanel agentPanel;
	private final JPanel commandPanel;
	
	public AccountListView (AccountList model, AccountListController controller)
	{
		super(model, controller);
		
		AccountManagerHandler handler = new AccountManagerHandler();
		
		setTitle("Account Selection");
		
		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(4, 1, 5, 5));
		topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		accountListComboBox = new JComboBox<String>(model.listAccounts());
		topPanel.add(getAccountList());
		
		editPanel = new JPanel();
		editPanel.setLayout(new GridLayout(1, 3, 5, 5));
		
		JButton editDollars = new JButton(DOLLARS);
		editDollars.addActionListener(handler);
		editPanel.add(editDollars);
		
		JButton editEuros = new JButton(EUROS);
		editEuros.addActionListener(handler);
		editPanel.add(editEuros);
		
		JButton editYen = new JButton(YEN);
		editYen.addActionListener(handler);
		editPanel.add(editYen);
		
		topPanel.add(editPanel);
		
		agentPanel = new JPanel();
		agentPanel.setLayout(new GridLayout(1, 2, 5, 5));
		
		JButton depositAgentB = new JButton(DEPOSITAGENT);
		depositAgentB.addActionListener(handler);
		agentPanel.add(depositAgentB);
		
		JButton withdrawAgentB = new JButton(WITHDRAWAGENT);
		withdrawAgentB.addActionListener(handler);
		agentPanel.add(withdrawAgentB);
		
		topPanel.add(agentPanel);
		
		commandPanel = new JPanel();
		commandPanel.setLayout(new GridLayout(1, 2, 5, 5));
		
		/*
		JButton agentDeposit = new JButton(DEPOSIT);
		agentDeposit.addActionListener(handler);
		commandPanel.add(agentDeposit, null);
		
		JButton agentWithdraw = new JButton(WITHDRAW);
		agentWithdraw.addActionListener(handler);
		commandPanel.add(agentWithdraw, null);
		*/
		
		JButton save = new JButton(SAVE);
		save.addActionListener(handler);
		commandPanel.add(save);
		
		JButton exit = new JButton(EXIT);
		exit.addActionListener(handler);
		commandPanel.add(exit);
		topPanel.add(commandPanel);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent evt) {
		        dispose();
		        //super.windowClosing(evt);
		        model.exit();
		        System.exit(ABORT);
		    }
		});
		
		//getContentPane().setLayout(new BorderLayout());
		//getContentPane().add(topPanel, BorderLayout.CENTER);
		
		topPanel.setOpaque(true);
		setContentPane(topPanel);
		//Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		//this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2 - 50);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
        setVisible(true);
	}
	
	/**
	 * Displays a message box.
	 * @param message - message box contents
	 */
	public void messageBox (String message) { JOptionPane.showMessageDialog(null, message); }
	
	/**
	 * Process events.
	 * @param event - interface event
	 */
	public void modelChanged (ModelEvent event)
	{
		/*
		if (event.getID() == 1)
		{
			Iterator<AccountController> it = ((AccountManagerModel)getModel()).children.iterator();
			while (it.hasNext())
			{
				AccountController temp = (AccountController)it.next();
				if (temp != null) { ((AccountModel)temp.getModel()).notifyChanged(event); }
			}
		} else if (event.getID() == 3) {
			Iterator<AccountController> it = ((AccountManagerModel)getModel()).children.iterator();
			while (it.hasNext())
			{
				AccountController temp = (AccountController)it.next();
				if (temp != null) { ((AccountModel)temp.getModel()).notifyChanged(event); }
			}
		}
		*/
	}
	
	/**
	 * Event handler structure.
	 */
	class AccountManagerHandler implements ActionListener
	{
		public void actionPerformed (ActionEvent event) { ((AccountListController)getController()).operation(event.getActionCommand()); }
	}
	
	public static void main(String... args) {
		/*

		String fileName = "";
		if(args.length == 0) {
			System.out.println("filename expected in command line");
			System.exit(1);
		}
		fileName = args[0];
		*/

	    String fileName = "accounts.txt";
		try {
			final AccountList model = new AccountList(fileName);
			final AccountListController contr = new AccountListController(model);
	    	SwingUtilities.invokeLater(new Runnable() {
	    		public void run() {
	    			JFrame.setDefaultLookAndFeelDecorated(true);
	    			JDialog.setDefaultLookAndFeelDecorated(true);
	    			AccountListView app = new AccountListView((AccountList)contr.getModel(), contr);
	    			contr.setView(app);
	    			//app.setVisible(true);
	    		}
	    	});
			
		}
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}
	  }

	public JComboBox getAccountList() {
		return accountListComboBox;
	}
}
