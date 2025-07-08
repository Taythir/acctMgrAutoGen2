package acctMgr.view;

import acctMgr.model.Account;
import acctMgr.model.ModelEvent;
import acctMgr.controller.AccountController;
import acctMgr.view.JFrameView;
import acctMgr.view.AccountView.Handler;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class AccountView extends JFrameView {
	public static final String Deposit = "Deposit"; 
	public static final String Withdraw = "Withdraw";
	
	protected JPanel topPanel;
	private JPanel fieldsPanel;
	private JPanel textPane;
	private JPanel labelPane;
	protected JPanel buttonPanel;
	private JPanel titlePanel;
	
	protected JLabel balanceLabel;
	protected JLabel amountLabel;
	
	protected JTextField balanceField;
	protected JTextField amountField;
	
	protected JButton depButton;
	protected JButton withdrawButton;
	
	private Handler handler = new Handler();
	private String initAmountS;
	
    //Formats to format and parse numbers
    private NumberFormat amountFormat;
    private NumberFormat balanceFormat;
    
    public static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    enum Currency {DOLLAR, EURO, YEN};
    private static final BigDecimal[] rates = {new BigDecimal("1.0"), new BigDecimal("0.79"), new BigDecimal("94.1")};
    private static final BigDecimal[] reverseRates = {new BigDecimal("1.0"), new BigDecimal("1.27"), new BigDecimal("0.01")};
    private static final char[] symb = {'$', '\u20ac', '\u00a5'};
    private final Currency curType;
    private BigDecimal amount;
    
	public AccountView(Account model, AccountController controller, String type) { 
		super(model, controller);
		initAmountS = "30.00";
		if(type == AccountListView.EUROS) curType = Currency.EURO;
		else if (type == AccountListView.YEN) curType = Currency.YEN;
		else curType = Currency.DOLLAR;
		
		char curSymb = symb[curType.ordinal()];
		setTitle("Account operations in " + Character.toString(curSymb));
		
		//Container contentPane = getContentPane();
		//contentPane.setLayout(new BorderLayout());
		//contentPane.add(getTopPanel(), BorderLayout.CENTER);
		
		setContentPane(getTopPanel());
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent evt) {
		    	//super.windowClosing(evt);
		    	unregisterWithModel();
		    	getController().setView(null);
		    	setController(null);
		        dispose();
		        System.gc();
		    }
		});
		
		//setUpFormats();
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2 + 100);
		setResizable(false);
		pack();
		
	}
	protected JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			GridLayout layout = new GridLayout(2, 1, 5, 5);
			//GridLayout layout = new GridLayout(0, 1);
			topPanel.setLayout(layout);
			
			//topPanel.add(getTitlePanel());
			topPanel.add(getFieldsPanel());
			topPanel.add(getButtonPanel());
			Border emptyBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			Border compBorder = BorderFactory.createCompoundBorder(emptyBorder, loweredetched);
			topPanel.setBorder(BorderFactory.createTitledBorder(compBorder, ((Account)getModel()).getName(), TitledBorder.LEFT, TitledBorder.TOP));
			//topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		}
		return topPanel;
	}
	
	private JPanel getTitlePanel() {
		if(titlePanel == null) {
			titlePanel = new JPanel(new FlowLayout());
			JLabel titleLabel = new JLabel("Operations in ");
			titlePanel.add(titleLabel);
			//titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		return titlePanel;
	}
	private JPanel getFieldsPanel() {
		if(fieldsPanel == null) {
			fieldsPanel = new JPanel(new BorderLayout());
			fieldsPanel.add(getLabelPane(), BorderLayout.EAST);
			fieldsPanel.add(getTextPane(), BorderLayout.CENTER);
		}
		return fieldsPanel;
	}
	
	/*
	private JPanel getFieldsPanel() {
		if(fieldsPanel == null) {
			fieldsPanel = new JPanel(new BoxLayout(fieldsPanel, BoxLayout.X_AXIS));
			fieldsPanel.add(getLabelPane());
			fieldsPanel.add(getTextPane());
		}
		return fieldsPanel;
	}
	*/
	private JPanel getTextPane()
	{
		if(textPane == null){
			textPane = new JPanel(new GridLayout(0,1));
			textPane.add(getBalanceLabel());
			textPane.add(getAmountLabel());
			//textPane.add(getDepButton());
			textPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			//textPanel.setPreferredSize(new Dimension(250, 50));
		}
		return textPane;
	}
	private JPanel getLabelPane() {
		if(labelPane == null) {
			labelPane = new JPanel(new GridLayout(0,1));
			labelPane.add(getBalanceField());
			labelPane.add(getAmountField());
			//labelPane.add(getWithdrawButton());
			labelPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			labelPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		return labelPane;
	}
	
	protected JLabel getBalanceLabel(){
		if(balanceLabel == null){
			balanceLabel = new JLabel();
			balanceLabel.setText("Balance: ");
			balanceLabel.setLabelFor(balanceField);
			//balanceLabel.setPreferredSize(new Dimension(200, 20));
		}
		return balanceLabel;
	}
	
	protected JTextField getBalanceField(){
		if(balanceField == null){
			balanceField = new JTextField();
			balanceField.setColumns(10);
			
			BigDecimal rawAmount = ((Account)getModel()).getBalance();
			if(curType != Currency.DOLLAR) {
				amount = rawAmount.multiply(rates[curType.ordinal()]);
				amount = amount.setScale(2, ROUNDING);
			}
			else amount = rawAmount;
			
			balanceField.setText(amount.toString());
			balanceField.setEditable(false);
		}
		return balanceField;
	}
	
	protected JLabel getAmountLabel(){
		if(amountLabel == null){
			amountLabel = new JLabel();
			amountLabel.setText("Amount: ");
			amountLabel.setLabelFor(amountField);
			//amountLabel.setPreferredSize(new Dimension(200, 20));
		}
		return amountLabel;
	}
	
	protected JTextField getAmountField(){
		if(amountField == null){
			amountField = new JTextField();
			amountField.setColumns(10);
			amountField.setText(initAmountS);
			amountField.setEditable(true);
		}
		return amountField;
	}
	
	protected JPanel getButtonPanel()
	{
		if(buttonPanel == null){
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
			buttonPanel.add(getDepButton());
			buttonPanel.add(getWithdrawButton());
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		
		return buttonPanel;
	}
	protected JButton getDepButton(){
		if(depButton == null){
			depButton = new JButton(Deposit);
			//depButton.setPreferredSize(new Dimension(30, 10));
			depButton.addActionListener(handler);
		}
		return depButton;
	}
	protected JButton getWithdrawButton(){
		if(withdrawButton == null){
			withdrawButton = new JButton(Withdraw);
			//withdrawButton.setPreferredSize(new Dimension(30, 10));
			withdrawButton.addActionListener(handler);
		}
		return withdrawButton;
	}
	
	public BigDecimal getAmount() {
		try {
			BigDecimal rawAmount = new BigDecimal(amountField.getText());
			if(curType != Currency.DOLLAR) {
				amount = rawAmount.multiply(reverseRates[curType.ordinal()]);
				
			}
			else amount = rawAmount;
			amount = amount.setScale(2, ROUNDING);
			amountField.setText(amount.toString());
			
		}
		catch(NumberFormatException ex) {
			amountField.setText(initAmountS);
			showError("Amount field only accepts decimals");
		}
		return amount;
	}
	
	public void showError(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}
	public void modelChanged(ModelEvent event) {
		BigDecimal rawAmount = event.getBalance();
		if(curType != Currency.DOLLAR) {
			amount = rawAmount.multiply(rates[curType.ordinal()]);
			amount = amount.setScale(2, ROUNDING);
		}
		else amount = rawAmount;
		
		String msg = amount.toString();
		balanceField.setText(msg);
	 }
	
	   //Create and set up number formats. These objects also
    //parse numbers input by user.
    private void setUpFormats() {
        //amountFormat = NumberFormat.getNumberInstance();
        balanceFormat = NumberFormat.getCurrencyInstance();
        amountFormat = NumberFormat.getCurrencyInstance();
    }
	
	 // Inner classes for Event Handling 
	class Handler implements ActionListener { 
		// Event handling is handled locally
		public void actionPerformed(ActionEvent e) {
			((AccountController)getController()).operation(e.getActionCommand()); 
	    } }
}
