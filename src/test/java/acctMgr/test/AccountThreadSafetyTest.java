package acctMgr.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import acctMgr.model.Account;
import acctMgr.model.OverdrawException;
import acctMgr.model.DepositAgent;
import acctMgr.model.WithdrawAgent;

public class AccountThreadSafetyTest {
	Account acc;
	WithdrawAgent withdrawAg;
	DepositAgent depAg;
	@BeforeEach
	public void setUp() throws Exception {
		acc = new Account("Alice", "58392", new BigDecimal("1000.45"));
		depAg = new DepositAgent(acc, new BigDecimal("3.32"), 15);
		withdrawAg = new WithdrawAgent(acc, new BigDecimal("10.07"), 10);
	}

	@AfterEach
	public void tearDown() throws Exception {
		acc = null;
	}

	@Test
	public void testAccountThreadSafe() {
		Thread depAgT = new Thread(depAg);
		Thread withdrawAgT = new Thread(withdrawAg);
		
		depAgT.start();
		withdrawAgT.start();
		try{
			depAgT.join();
			withdrawAgT.join();
		}
		catch(InterruptedException ex){System.out.println("Agent threads interrupted");}
		System.out.println("deposit agent transferred " + depAg.getTransferred());
		System.out.println("withdraw agent transferred " + withdrawAg.getTransferred());
		assertEquals("49.80", depAg.getTransferred().toString());
		assertEquals("100.70", withdrawAg.getTransferred().toString());
		assertEquals("949.55", acc.getBalance().toString());
	}

}
