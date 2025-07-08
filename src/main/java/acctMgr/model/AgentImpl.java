package acctMgr.model;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import acctMgr.utils.CoresNumber;

public abstract class AgentImpl extends AbstractModel implements IAgent {
	protected Object pauseLock;
	protected volatile boolean paused;
	public volatile boolean active;
	protected Account account;
	protected int iters;
	protected BigDecimal amount;
	protected BigDecimal transferred;
	protected String name = new String("Default");
	protected AgentStatus status;
	protected volatile boolean wasBlocked;
	protected int delay = 300;
	//private static final int coresNumber = CoresNumber.getNumberOfCPUCores();
	private static final int coresNumber = Runtime.getRuntime().availableProcessors();
	protected static final ExecutorService pool = Executors.newFixedThreadPool(2*coresNumber);
	protected volatile Future<?> thisTask;
	protected static List<Future<?>> tasksList = new ArrayList<Future<?>>(10);
	
	public AgentImpl(Account account, BigDecimal amount){
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
	}
	public AgentImpl(Account account, BigDecimal amount, int iters){
		this(account, amount);
		this.iters = iters;
		active = false;
	}
	public static ExecutorService getFixedPool() {
		return pool;
	}
	public static void execute(Runnable r) {
		pool.execute(r);
	}
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
    public void setAmount(BigDecimal amount) {
    	this.amount = amount;
    }
    public void setTask(Future<?> task) {
    	thisTask = task;
    }
    public static void shutdownAndAwaitTermination() {
    	   pool.shutdown(); // Disable new tasks from being submitted
    	   cancelTasks();
    	   try {
    	     // Wait a while for existing tasks to terminate
    	     if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
    	    	 pool.shutdownNow(); // Cancel currently executing tasks
    	       // Wait a while for tasks to respond to being cancelled
    	       if (pool.awaitTermination(5, TimeUnit.SECONDS))
    	           System.err.println("Pool terminated");
    	     }
    	   } catch (InterruptedException ie) {
    	     // (Re-)Cancel if current thread also interrupted
    		   System.out.println("shutdown InterruptedException handler");
    		   pool.shutdownNow();
    	     // Preserve interrupt status
    	     Thread.currentThread().interrupt();
    	   }
    	 }
    public static void cancelTasks() {
    	for(Future<?> task : tasksList) {
    		task.cancel(true);
    	}
    }
    public static void addTask(Future<?> task) {
    	tasksList.add(task);
    }
    public void finish(){
    	active = false;
    	if(thisTask == null) {
    		System.out.println("Agent finish : thisTask is null !");
    	}
    	else {
    		tasksList.remove(thisTask);
    		thisTask.cancel(true);
    		System.out.println("Canceled task");
    		}
    }
}
