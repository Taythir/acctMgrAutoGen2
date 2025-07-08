package acctMgr.controller;

import acctMgr.model.Account;
import java.math.BigDecimal;
import acctMgr.model.OverdrawException;

import javax.swing.SwingUtilities;
import acctMgr.view.AccountView;
public class AccountController extends AbstractController {
	
	public void operation(String opt) {
		if(opt == AccountView.Deposit) {
			BigDecimal amount = ((AccountView)getView()).getAmount();
			((Account)getModel()).deposit(amount);
		} else if(opt == AccountView.Withdraw) {
			BigDecimal amount = ((AccountView)getView()).getAmount();
			try {
				((Account)getModel()).withdraw(amount);
			}
			catch(OverdrawException ex) {
				final String msg = ex.getMessage();
				SwingUtilities.invokeLater(new Runnable() {
				      public void run() {
				    	  ((AccountView)getView()).showError(msg);
				      }
				    });
			}
		}
	}
}
