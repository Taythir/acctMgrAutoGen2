package acctMgr.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import acctMgr.model.Account;
import acctMgr.model.WithdrawAgent;

public class WithdrawAgentTest {

	
	Account acc;
	WithdrawAgent withdrawAg;
	
	
	@BeforeEach
	public void setUp() throws Exception {
		acc = new Account("Bob", "58392", new BigDecimal("180.45"));
		withdrawAg = new WithdrawAgent(acc, new BigDecimal("10.07"), 15);
	}

	@AfterEach
	public void tearDown() throws Exception {
		acc = null;
		withdrawAg = null;
	}

	@Test
	public void testWithdrawAgent() {
		withdrawAg.run();
		System.out.println("Balance " + acc.getBalance());
		assertEquals("29.40", acc.getBalance().toString());
	}

}
