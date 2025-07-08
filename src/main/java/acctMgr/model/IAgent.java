package acctMgr.model;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
public interface IAgent extends Model, Runnable {
	public BigDecimal getTransferred();
	public void onPause();
	public void onResume();
	public void setName(String name);
	public String getName();
	public Account getAccount();
	public BigDecimal getAmount();
	public void setAmount(BigDecimal amount);
	public void setStatus(AgentStatus agSt);
	public AgentStatus getStatus();
	public void setTask(Future<?> task);
	public void finish();
	public void run();
}
