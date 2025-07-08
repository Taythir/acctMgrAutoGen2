package acctMgr.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/*
import accountManager.view.AccountManagerView;
import accountManager.view.AccountView;
import accountManager.view.AgentView;
import accountManager.controller.AccountController;
import accountManager.controller.AgentController;
*/
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import acctMgr.controller.AccountController;
import acctMgr.controller.AgentController;
import acctMgr.utils.CoresNumber;
import acctMgr.view.AccountListView;
import acctMgr.view.AccountView;
import acctMgr.view.AccountViewSpring;
import acctMgr.view.AgentView;
import java.lang.Integer;

/**
 * Model class for account management form.
 * @since September 29, 2011
 * @version 1.0
 * @author Corey Holland
 */
public class AccountList extends AbstractModel
{
	/**
	 * Path to accounts file.
	 */
	private String accountFile;
	
	/**
	 * Dynamic list of accounts.
	 */
	private List<Account> accountList = new ArrayList<Account>();
	private Map<String, Account> accountMap = new HashMap<String, Account>();
	
	private Account currentAccount;
	
	//private ExecutorService fixedPool;
	
	/**
	 * Dynamic list of children.
	 */
	//public ArrayList<AccountController> children = new ArrayList<AccountController>();
	
	/**
	 * AccountManagerModel constructor.
	 */
	public AccountList () { super(); }
	
	/**
	 * AccountManagerModel constructor. Generates array of accounts from file.
	 * @param filePath - path to accounts file
	 */
	public AccountList (String filePath) throws Exception
	{
		accountFile = filePath;
		load(filePath);
		if(accountList.size() > 0) currentAccount = accountList.get(0);
		else {
			System.out.println("The account file is empty");
			System.exit(1);
		}
		int coresNumber = CoresNumber.getNumberOfCPUCores();
		System.out.println("Number of physical cores = " + coresNumber);
		//fixedPool = Executors.newFixedThreadPool(coresNumber);
	}
	public void addAccount(Account account) {
		accountList.add(account);
		accountMap.put(account.getName(), account);
	}
	
	public void removeAccount(String name) {
		Account account = accountMap.get(name);
		accountList.remove(account);
		accountMap.remove(name);
	}
	
	public void load(String filePath) throws Exception {
		int lineNumber = 0;
		try {
			BufferedReader inputFile = new BufferedReader(new FileReader(filePath));
			String temp = "";
			while ((temp = inputFile.readLine()) != null)
			{
				++lineNumber;
				System.out.println(temp);
				String [] tempArray = temp.split("\\s+");
				System.out.println(tempArray[0] + ";");
				System.out.println(tempArray[1] + ";");
				System.out.println(tempArray[2] + ";");
				Account account = new Account(tempArray[0], tempArray[1], new BigDecimal(tempArray[2]));
				
				accountList.add(account);
				accountMap.put(tempArray[0], account);
			}
			inputFile.close();
		} catch (FileNotFoundException e) {
			throw new Exception("Invalid file: input file '" + filePath + "' not found");
		} catch (Exception e) {
			throw new Exception("Invalid file: error reading '" + filePath + "' at line " + lineNumber);
		}
		
	}
	public Account getAccount(String name) {
		return accountMap.get(name);
	}
	public Iterator<Account> accountsIterator() {
		return accountList.iterator();
	}
	/**
	 * Enumerates account list as an array of strings.
	 */
	public String [] listAccounts ()
	{
		String [] accounts = new String[accountList.size()];
		for (int i = 0; i < accountList.size(); ++i) { accounts[i] = accountList.get(i).getName(); }
		return accounts;
	}
	
	/**
	 * Generates child window to edit individual accounts.
	 * @param selected - array index of selected account
	 * @param type - currency format
	 */
	/*
	public void edit (int selected, String type)
	{
		if (type.equals(AccountManagerView.DOLLARS)) { children.add(new AccountController(this, accountList.get(selected), AccountView.DOLLARS)); }
		else if (type.equals(AccountManagerView.EUROS)) { children.add(new AccountController(this, accountList.get(selected), AccountView.EUROS)); }
		else if (type.equals(AccountManagerView.YEN)) { children.add(new AccountController(this, accountList.get(selected), AccountView.YEN)); }
	}
	
	public void agent (int selected, String type)
	{
		if (type == AccountManagerView.DEPOSIT) { new AgentController(this, accountList.get(selected), AgentView.DEPOSIT); }
		else if (type == AccountManagerView.WITHDRAW) { new AgentController(this, accountList.get(selected), AgentView.WITHDRAW); }
	}
	*/
	/**
	 * Generates child window to edit individual accounts.
	 * @param selected - array index of selected account
	 * @param type - currency format
	 */
	public void edit (int selected, String type)
	{
		currentAccount = accountList.get(selected);
		if(currentAccount == null) System.out.println("currentAccount == null");
		else {
			System.out.println("account selected for " + currentAccount.getName());
			final AccountController contr = new AccountController();
			contr.setModel(currentAccount);
			SwingUtilities.invokeLater(new Runnable() {
		    	public void run() {
		    		//AccountView app = new AccountView((Account)contr.getModel(), contr, type);
		    		AccountViewSpring app = new AccountViewSpring((Account)contr.getModel(), contr, type);
		    		contr.setView(app);
		    		app.setVisible(true);
		    	}
		    });
		}
	}
	
	public void createDepAgent(int selected) {
		
		currentAccount = accountList.get(selected);
		if(currentAccount == null) System.out.println("StartDepositAgent : currentAccount == null");
		else {
			//System.out.println("StartDepositAgent : account selected for " + currentAccount.getName());
			final DepositAgent depAgent = new DepositAgent(currentAccount, new BigDecimal("30.00"));
			final AgentController contr = new AgentController();
			contr.setModel(depAgent);
			SwingUtilities.invokeLater(new Runnable() {
		    	public void run() {
		    		//AccountView app = new AccountView((Account)contr.getModel(), contr, type);
		    		AgentView app = new AgentView(depAgent, contr, currentAccount.getName());
		    		contr.setView(app);
		    		app.setVisible(true);
		    	}
		    });
		    
		}
		
	}
	
	public void createWithdrawAgent(int selected) {
		currentAccount = accountList.get(selected);
		if(currentAccount == null) System.out.println("StartWitdrawAgent : currentAccount == null");
		else {
			//System.out.println("StartWithdrawAgent : account selected for " + currentAccount.getName());
			WithdrawAgent depAgent = new WithdrawAgent(currentAccount, new BigDecimal("30.00"));
			final AgentController contr = new AgentController();
			contr.setModel(depAgent);
			SwingUtilities.invokeLater(new Runnable() {
		    	public void run() {
		    		//AccountView app = new AccountView((Account)contr.getModel(), contr, type);
		    		AgentView app = new AgentView((IAgent)contr.getModel(), contr, currentAccount.getName());
		    		contr.setView(app);
		    		app.setVisible(true);
		    	}
		    });
		    
		}
	}
	
	/**
	 * Save accounts to file.
	 */
	public void save ()
	{
		try {
			PrintWriter outputFile = new PrintWriter(new FileWriter(accountFile));
			Iterator<Account> it = accountList.iterator();
			// for (Account account : accountList) {...}
			// for (i=0; i< ... ; i++) {}
			while (it.hasNext())
			{
				Account temp = (Account)it.next();
				//outputFile.printf("%s\t%d\t%s\n", temp.getName(), temp.getID(), temp.getBalance().toString());
				outputFile.println(temp.getName() + "\t" + temp.getID() + "\t" + temp.getBalance());
			}
			outputFile.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * Save accounts to file and dismiss all windows.
	 */
	public void exit ()
	{
		AgentImpl.shutdownAndAwaitTermination();
		System.err.println("Thread pool has shut down");
		save(); // save current state after all the threads shutdown
		//System.exit(0);
	}
}