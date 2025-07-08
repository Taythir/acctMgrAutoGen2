package acctMgr.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import acctMgr.controller.AccountController;
import acctMgr.controller.AccountListController;
import acctMgr.controller.AgentController;
import acctMgr.model.Account;
import acctMgr.model.AccountList;
import acctMgr.model.AgentStatus;
import acctMgr.model.DepositAgent;
import acctMgr.model.IAgent;
import acctMgr.model.ModelEvent;
import acctMgr.view.AccountView.Currency;
import acctMgr.view.AccountView.Handler;

public class AgentView extends JFrameView {
	public static final String START = "Start"; 
	public static final String PAUSE = "Pause";
	public static final String RESUME = "Resume";
	public static final String DISMISS = "Dismiss";
	
	private JPanel topPanel;
	private JPanel formPanel;
	private JPanel buttonPanel;
	
	protected JLabel transferredLabel;
	protected JLabel amountLabel;
	protected JLabel statusLabel;
	
	protected JTextField transferredField;
	protected JTextField amountField;
	protected JTextField statusField;
	
	protected JButton startButton;
	protected JButton pauseButton;
	protected JButton resumeButton;
	protected JButton dismissButton;
	
	private Handler handler = new Handler();
	private String initAmountS;
	private static volatile int ID = 0;
	private String accountName;
	private String type;
	
	public AgentView (IAgent model, AgentController controller, String accountName)
	{
		super(model, controller);
		initAmountS = "30.00";
		type = "Undefined";
		this.accountName = accountName;
		if(model instanceof acctMgr.model.DepositAgent) {
			type = "Deposit";
		}
		else if (model instanceof acctMgr.model.WithdrawAgent) {
			type = "Withdraw";
		}
		setTitle(type + " agent " + ID);
		ID++;
		
		setContentPane(getTopPanel());
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent evt) {
		    	//super.windowClosing(evt);
		    	stopAgent();
		    }
		});
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2 - 50, dim.height/2-this.getSize().height/2 - 50);
		setResizable(false);
		pack();
	}
	
	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			GridLayout layout = new GridLayout(2, 1, 5, 5);
			//GridLayout layout = new GridLayout(0, 1);
			topPanel.setLayout(layout);
			
			topPanel.add(getFormPanel());
			topPanel.add(getButtonPanel());
			Border emptyBorder = BorderFactory.createEmptyBorder(20, 20, 5, 20);
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			Border compBorder = BorderFactory.createCompoundBorder(emptyBorder, loweredetched);
			topPanel.setBorder(BorderFactory.createTitledBorder(compBorder, accountName, TitledBorder.LEFT, TitledBorder.TOP));
			//topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		}
		return topPanel;
	}

	private JPanel getFormPanel() {
		if(formPanel == null) {
			formPanel = new JPanel(new SpringLayout());
			transferredField = getTransferredField();
			amountField = getAmountField();
			statusField = getStatusField();
			transferredLabel = getTransferredLabel();
			transferredLabel.setHorizontalTextPosition(JLabel.TRAILING);
			amountLabel = getAmountLabel();
			amountLabel.setHorizontalTextPosition(JLabel.TRAILING);
			statusLabel = getStatusLabel();
			statusLabel.setHorizontalTextPosition(JLabel.TRAILING);
			
			formPanel.add(transferredLabel);
			formPanel.add(transferredField);
			formPanel.add(amountLabel);
			formPanel.add(amountField);
			formPanel.add(statusLabel);
			formPanel.add(statusField);
			SpringUtilities.makeCompactGrid(formPanel, 3, 2, //rows, cols
			        6, 6, //initX, initY
			        6, 6); //xPad, yPad
		}
		return formPanel;
	}
	
	private JPanel getButtonPanel() {
		if(buttonPanel == null) {
			buttonPanel = new JPanel();
			GroupLayout layout = new GroupLayout(buttonPanel);
			buttonPanel.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			startButton = getStartButton();
			pauseButton = getPauseButton();
			resumeButton = getResumeButton();
			dismissButton = getDismissButton();
			layout.linkSize(SwingConstants.HORIZONTAL, startButton, pauseButton, resumeButton, dismissButton);
			layout.setHorizontalGroup(
					   layout.createSequentialGroup()
					      	.addComponent(startButton)
					      	.addComponent(pauseButton)
					      	.addComponent(resumeButton)
					      	.addComponent(dismissButton)
					    );
			layout.setVerticalGroup(
						layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(startButton)
							.addComponent(pauseButton)
							.addComponent(resumeButton)
							.addComponent(dismissButton)
				      	);
			
		}
		return buttonPanel;
	}
	
	private JTextField getTransferredField() {
		if(transferredField == null){
			transferredField = new JTextField();
			transferredField.setColumns(10);
			BigDecimal transferred = ((IAgent)getModel()).getTransferred();
			transferredField.setText(transferred.toString());
			transferredField.setEditable(false);
		}
		return transferredField;
	}
	
	private JTextField getAmountField() {
		if(amountField == null){
			amountField = new JTextField();
			amountField.setColumns(10);
			amountField.setText(initAmountS);
			amountField.setEditable(true);
		}
		return amountField;
	}
	
	private JTextField getStatusField() {
		if(statusField == null) {
			statusField = new JTextField();
			statusField.setColumns(10);
			statusField.setText("Not started");
			statusField.setEditable(false);
		}
		return statusField;
	}
	
	private JLabel getTransferredLabel() {
		if(transferredLabel == null){
			transferredLabel = new JLabel();
			transferredLabel.setText("Transferred: ");
			transferredLabel.setLabelFor(transferredField);
		}
		return transferredLabel;
	}
	
	private JLabel getAmountLabel(){
		if(amountLabel == null){
			amountLabel = new JLabel();
			amountLabel.setText("Amount: ");
			amountLabel.setLabelFor(amountField);
		}
		return amountLabel;
	}
	
	private JLabel getStatusLabel(){
		if(statusLabel == null){
			statusLabel = new JLabel();
			statusLabel.setText("Agent status: ");
			statusLabel.setLabelFor(statusField);
		}
		return statusLabel;
	}
	
	private JButton getStartButton(){
		if(startButton == null){
			startButton = new JButton(START);
			startButton.addActionListener(handler);
		}
		return startButton;
	}
	
	private JButton getPauseButton(){
		if(pauseButton == null){
			pauseButton = new JButton(PAUSE);
			pauseButton.addActionListener(handler);
		}
		return pauseButton;
	}
	
	private JButton getResumeButton(){
		if(resumeButton == null){
			resumeButton = new JButton(RESUME);
			resumeButton.addActionListener(handler);
		}
		return resumeButton;
	}
	
	private JButton getDismissButton(){
		if(dismissButton == null){
			dismissButton = new JButton(DISMISS);
			dismissButton.addActionListener(handler);
		}
		return dismissButton;
	}
	private void setPaused(boolean paused){
		resumeButton.setEnabled(paused);
		pauseButton.setEnabled(!paused);
	}
	public void modelChanged (ModelEvent me)
	{
		ModelEvent.EventKind kind = me.getKind();
		if(kind == ModelEvent.EventKind.AmountTransferredUpdate) {
			//System.out.println("Balance field to " + me.getBalance());
			transferredField.setText((me.getBalance()).toString());
		}
		else if(kind == ModelEvent.EventKind.AgentStatusUpdate) {
			AgentStatus agSt = me.getAgStatus();
			//System.out.println("Status to " + agSt.toString());
			if(agSt == AgentStatus.Paused) setPaused(true);
			else setPaused(false);
			statusField.setText(agSt.toString());
		}
	}
	public void setStatusField(String msg) {
		statusField.setText(msg);
	}
	public void stopAgent() {
		IAgent agent = (IAgent)getModel();
		if(agent == null) {
			System.out.println("AgentView.stopAgent: model is null !");
		}
		else {
			System.out.println("AgentView.stopAgent: stopping " + type + " agent " + ID + " for " + accountName);
			unregisterWithModel();
			((IAgent)getModel()).finish();
		}
    	getController().setView(null);
    	setController(null);
        dispose();
        System.gc();
	}
	public void disableStartButton() {
		startButton.setEnabled(false);
	}
	public BigDecimal getAmount() {
		BigDecimal amount = new BigDecimal("30.00");
		try {
			amount = new BigDecimal(amountField.getText());
			amount = amount.setScale(2, AccountView.ROUNDING);
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
	 // Inner classes for Event Handling 
	class Handler implements ActionListener { 
		// Event handling is handled locally
		public void actionPerformed(ActionEvent e) {
			((AgentController)getController()).operation(e.getActionCommand()); 
	    } }
	
	public static void main(String... args) {
		Account currentAccount = new Account("Lucy", "8736", new BigDecimal("300.00"));
		final DepositAgent depAgent = new DepositAgent(currentAccount, new BigDecimal("30.00"));
		final AgentController contr = new AgentController();
		contr.setModel(depAgent);
		SwingUtilities.invokeLater(new Runnable() {
	    	public void run() {
	    		//AccountView app = new AccountView((Account)contr.getModel(), contr, type);
	    		JFrame.setDefaultLookAndFeelDecorated(true);
	    		AgentView app = new AgentView(depAgent, contr, currentAccount.getName());
	    		contr.setView(app);
	    		app.setVisible(true);
	    	}
	    });
	}
}
