package acctMgr.model;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class Account extends AbstractModel {
	private BigDecimal balance;
	private String name;
	private String ID;
	public Account(String name, String ID, BigDecimal balance){
		this.name = name;
		this.ID = ID;
		this.balance = balance.setScale(2, RoundingMode.HALF_EVEN);
	}
	public BigDecimal getBalance(){return balance;}
	public String getName() {return name;}
	public String getID() {return ID;}
	public synchronized void deposit(BigDecimal amount) {
		balance = balance.add(amount);
		
		final ModelEvent me = new ModelEvent(ModelEvent.EventKind.BalanceUpdate, balance, AgentStatus.NA);
		//notifyChanged(me);
		
		
		SwingUtilities.invokeLater(
				new Runnable() {
				    public void run() {
				    	notifyChanged(me);
				    }
				});
		notifyAll();
	}
	
	public synchronized void withdraw(BigDecimal amount) throws OverdrawException {
		//BigDecimal newB = balance.add(BigDecimal.ZERO);
		//newB = newB.subtract(amount);
		BigDecimal newB = balance.subtract(amount); 
		if(newB.signum() < 0) throw new OverdrawException(newB);
		balance = newB;
		final ModelEvent me = new ModelEvent(ModelEvent.EventKind.BalanceUpdate, balance, AgentStatus.NA);
		
		SwingUtilities.invokeLater(
				new Runnable() {
				    public void run() {
				    	notifyChanged(me);
				    }
				});
	}
	
	public synchronized void autoWithdraw(BigDecimal amount, IAgent ag) throws InterruptedException {
		int maxWaits = 5; // Try up to 5 times (total 5*400ms = 2s)
		int waits = 0;
		while(balance.subtract(amount).signum() < 0) {
			ag.setStatus(AgentStatus.Blocked);
			if (waits++ >= maxWaits) {
				// Timeout: give up after several waits
				throw new InterruptedException("Timeout waiting for sufficient funds");
			}
			wait(400); // Wait up to 400ms each time
		}
		if(ag.getStatus() == AgentStatus.Paused) return;
		ag.setStatus(AgentStatus.Running);
		balance = balance.subtract(amount);
		final ModelEvent me = new ModelEvent(ModelEvent.EventKind.BalanceUpdate, this.balance, AgentStatus.Running);
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					notifyChanged(me);
				}
			});
	}
	
	/**
     * Transfers a specified amount from this account to the target account.
     * To avoid dynamic deadlock, locks are acquired in a fixed order based on the account IDs.
     *
     * @param target the account to transfer funds to
     * @param amount the amount to transfer
     * @throws OverdrawException if this account lacks sufficient funds
     * @throws IllegalArgumentException if the target account is null or if the transfer amount is not positive
     */
    public void transfer(Account target, BigDecimal amount) throws OverdrawException {
        if (target == null) {
            throw new IllegalArgumentException("Target account cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (this == target) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        // Determine locking order to avoid deadlock
        Account first, second;
        first = this;
        second = target;
		/*
        int cmp = this.ID.compareTo(target.ID);
        if (cmp < 0) {
            first = this;
            second = target;
        } else if (cmp > 0) {
            first = target;
            second = this;
        } else {
            // Fallback in the unlikely event IDs are equal
            if (System.identityHashCode(this) < System.identityHashCode(target)) {
                first = this;
                second = target;
            } else {
                first = target;
                second = this;
            }
        }
		*/
        
        synchronized(first) {
            synchronized(second) {
                // Check if sufficient funds are available in the source account
                BigDecimal newBalance = this.balance.subtract(amount);
                if (newBalance.signum() < 0) {
                    throw new OverdrawException(newBalance);
                }
                
                // Perform the transfer
                this.balance = newBalance;
                target.balance = target.balance.add(amount);
                
                // Create events for both accounts
                final ModelEvent eventSource = new ModelEvent(ModelEvent.EventKind.BalanceUpdate, this.balance, AgentStatus.NA);
                final ModelEvent eventTarget = new ModelEvent(ModelEvent.EventKind.BalanceUpdate, target.balance, AgentStatus.NA);
                
                // Notify listeners on the Swing Event Dispatch Thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        notifyChanged(eventSource);
                        target.notifyChanged(eventTarget);
                    }
                });
            }
        }
    }
}

