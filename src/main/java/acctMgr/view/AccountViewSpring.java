package acctMgr.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import acctMgr.controller.AccountController;
import acctMgr.model.Account;

public class AccountViewSpring extends AccountView {
	
	protected JPanel formPanel;
	public AccountViewSpring(Account model, AccountController controller, String type) {
		super(model, controller, type);
	}

	@Override
	protected JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			GridLayout layout = new GridLayout(2, 1, 5, 5);
			//GridLayout layout = new GridLayout(0, 1);
			topPanel.setLayout(layout);
			
			topPanel.add(getFormPanel());
			topPanel.add(getButtonPanel());
			Border emptyBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			Border compBorder = BorderFactory.createCompoundBorder(emptyBorder, loweredetched);
			topPanel.setBorder(BorderFactory.createTitledBorder(compBorder, ((Account)getModel()).getName(), TitledBorder.LEFT, TitledBorder.TOP));
			//topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		}
		return topPanel;
	}
	
	private JPanel getFormPanel() {
		if(formPanel == null) {
			formPanel = new JPanel(new SpringLayout());
			balanceField = getBalanceField();
			amountField = getAmountField();
			balanceLabel = getBalanceLabel();
			balanceLabel.setHorizontalTextPosition(JLabel.TRAILING);
			amountLabel = getAmountLabel();
			amountLabel.setHorizontalTextPosition(JLabel.TRAILING);
			formPanel.add(balanceLabel);
			formPanel.add(balanceField);
			formPanel.add(amountLabel);
			formPanel.add(amountField);
			SpringUtilities.makeCompactGrid(formPanel, 2, 2, //rows, cols
			        6, 6, //initX, initY
			        6, 6); //xPad, yPad
			
			
		}
		return formPanel;
	}

	@Override
	protected JPanel getButtonPanel() {
		if(buttonPanel == null) {
			buttonPanel = new JPanel();
			GroupLayout layout = new GroupLayout(buttonPanel);
			buttonPanel.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			depButton = getDepButton();
			withdrawButton = getWithdrawButton();
			layout.linkSize(SwingConstants.HORIZONTAL, depButton, withdrawButton);
			layout.setHorizontalGroup(
					   layout.createSequentialGroup()
					      	.addComponent(depButton)
					      	.addComponent(withdrawButton)
					    );
			layout.setVerticalGroup(
						layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(depButton)
							.addComponent(withdrawButton)
				      	);
			
		}
		return buttonPanel;
	}
}



