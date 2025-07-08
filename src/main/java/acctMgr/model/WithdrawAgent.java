package acctMgr.model;

import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class WithdrawAgent extends AgentImpl {
	/*
	private Object pauseLock;
	private volatile boolean paused;
	public volatile boolean active;
	private Account account;
	private int iters;
	private BigDecimal amount;
	private BigDecimal transferred;
	private String name = new String("Default");
	private AgentStatus status;
	private volatile boolean wasBlocked;
	*/
	
	
	public WithdrawAgent(Account account, BigDecimal amount){
		super(account, amount);
		/*
		this.account = account;
		this.amount = amount;
		//this.amount.setScale(2, RoundingMode.HALF_UP);
		this.transferred = BigDecimal.ZERO;
		//this.transferred.setScale(2, RoundingMode.HALF_UP);
		this.status = AgentStatus.Running;
		this.wasBlocked = false;
		this.active = true;
		this.paused = false;
		this.pauseLock = new Object();
		*/
	}
	public WithdrawAgent(Account account, BigDecimal amount, int iters){
		super(account, amount, iters);
		/*
		this(account, amount);
		this.iters = iters;
		active = false;
		*/
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
			try {
				account.autoWithdraw(amount, this);
			}
			catch(InterruptedException e) {
            	System.out.println("Thread " + Thread.currentThread().getName() + " interrupted");
            }
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
	public BigDecimal getTransferred(){return transferred;}
	public void onPause() {
        synchronized (pauseLock) {
            paused = true;
            setStatus(AgentStatus.Paused);
        }
    }

    public void onResume() {
        synchronized (pauseLock) {
        	if(wasBlocked) setStatus(AgentStatus.Blocked);
        	else setStatus(AgentStatus.Running);
            paused = false;
            pauseLock.notify();
        }
    }
    public void setStatus(AgentStatus agSt) {
    	status = agSt;
    	if(status == AgentStatus.Blocked) wasBlocked = true;
    	if(status == AgentStatus.Running) wasBlocked = false;
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
