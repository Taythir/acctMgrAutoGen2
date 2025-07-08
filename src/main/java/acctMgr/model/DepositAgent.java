package acctMgr.model;

import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DepositAgent extends AgentImpl {
	
	public DepositAgent(Account account, BigDecimal amount){
		super(account, amount);
	}
	public DepositAgent(Account account, BigDecimal amount, int iters){
		super(account, amount, iters);
	}
	public void run() {
			setStatus(AgentStatus.Running);
			while(active || iters > 0) {
				synchronized (pauseLock) {
					while (paused) {
						try {
							pauseLock.wait();
						} catch (InterruptedException e) {
							System.out.println("Thread " + Thread.currentThread().getName() + " interrupted");
						}
					}
				}
				account.deposit(amount);
				transferred = transferred.add(amount);
				iters--;
				final ModelEvent me = new ModelEvent(ModelEvent.EventKind.AmountTransferredUpdate, transferred, AgentStatus.NA);
				SwingUtilities.invokeLater(
						new Runnable() {
							public void run() {
								notifyChanged(me);
							}
						});
				try {
					Thread.sleep(delay);
				}
				catch(InterruptedException ex){
					System.out.println("Thread " + Thread.currentThread().getName() + " interrupted");
				}
			}
		
	}
	/*
	public BigDecimal getTransferred(){return this.transferred;}
	public void onPause() {
        synchronized (pauseLock) {
            paused = true;
            setStatus(AgentStatus.Paused);
        }
    }

    public void onResume() {
        synchronized (pauseLock) {
            paused = false;
            setStatus(AgentStatus.Running);
            pauseLock.notify();
        }
    }
    public void setStatus(AgentStatus agSt) {
    	status = agSt;
    	final ModelEvent me = new ModelEvent(ModelEvent.EventKind.AgentStatusUpdate, BigDecimal.ZERO, agSt);
    	SwingUtilities.invokeLater(
				new Runnable() {
				    public void run() {
				    	notifyChanged(me);
				    }
				});
    }
    public AgentStatus getStatus(){return status;}
    public void setName(String name) {this.name = name;}
    public String getName(){return name;}
    public Account getAccount(){return account;}
    public BigDecimal getAmount() {return amount;}
    public void finish(){
    	active = false;
    	Thread.currentThread().interrupt();
    }
    */
}
