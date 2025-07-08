package acctMgr.controller;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import acctMgr.model.AccountList;
import acctMgr.view.AccountListView;
import acctMgr.view.JFrameView;

public class AccountListController extends AbstractController {
	/**
	 * Controller constructor. Links model/view and shows form.
	 * @param filePath - input file path
	 */
	
	public AccountListController (AccountList model){setModel(model);}
	
	/**
	 * Calls model method according to view event.
	 * @param option - event type
	 */
	public void operation (String option)
	{
		int selected = ((AccountListView)getView()).getAccountList().getSelectedIndex();
		if (option == AccountListView.SAVE) { ((AccountList)getModel()).save(); }
		else if (option == AccountListView.EXIT) { ((AccountList)getModel()).exit(); }
		else if (option == AccountListView.DEPOSITAGENT) { 
			
			((AccountList)getModel()).createDepAgent(selected);
			
			}
		else if (option == AccountListView.WITHDRAWAGENT) { 
			
			((AccountList)getModel()).createWithdrawAgent(selected); 
			}
		else ((AccountList)getModel()).edit(selected, option);
	}
}
