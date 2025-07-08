package acctMgr.test;

import acctMgr.model.Account;
import acctMgr.model.DepositAgent;
import java.math.BigDecimal;

public class DepositAgentMainTest {

	public static void main(String[] args) {
		Account acc = new Account("Alice", "58392", new BigDecimal("10.45"));
		DepositAgent depAg = new DepositAgent(acc, new BigDecimal("3.32"), 15);
		depAg.run();
		System.err.println("Balance " + acc.getBalance());

	}

}
