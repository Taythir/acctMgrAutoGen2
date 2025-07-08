package acctMgr.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import acctMgr.model.Account;
import acctMgr.model.DepositAgent;

public class DepositAgentTest {
	Account acc;
	DepositAgent depAg;
	@BeforeEach
	public void setUp() throws Exception {
		acc = new Account("Alice", "58392", new BigDecimal("10.45"));
		depAg = new DepositAgent(acc, new BigDecimal("3.32"), 15);
	}

	@AfterEach
	public void tearDown() throws Exception {
		acc = null;
		depAg = null;
	}

	@Test
	public void testDepositAgent() {
		depAg.run();
		System.out.println("Balance " + acc.getBalance());
		assertEquals("60.25", acc.getBalance().toString());
	}

	/*
	@Test
	public void testDepositAgentFail() {
		fail();
	}
	*/
}
