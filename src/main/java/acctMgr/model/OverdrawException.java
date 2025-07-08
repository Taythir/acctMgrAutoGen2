package acctMgr.model;
import java.math.BigDecimal;
public class OverdrawException extends Exception {
	OverdrawException(BigDecimal amt){
		super("Overdraw by $" + amt);
	}
}
