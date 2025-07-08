package acctMgr.test;

import acctMgr.model.Account;
import acctMgr.model.OverdrawException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountTest {
	Account accountW;
	@BeforeEach
	public void setUp() throws Exception {
		accountW = new Account("Joe", "474832", new BigDecimal("200.45"));
	}

	@AfterEach
	public void tearDown() throws Exception {
		accountW = null;
		System.gc();
	}
	
	@Test
	public void testWithdraw() throws Exception {
		accountW.withdraw(new BigDecimal("110.55"));
		System.out.println("withdraw, new balance = " + accountW.getBalance());
		assertEquals("89.90", accountW.getBalance().toString());
	}
	
	@Test
	public void testDeposit() throws Exception {
		accountW.deposit(new BigDecimal("100.4"));
		System.out.println("deposit, new balance = " + accountW.getBalance());
		assertEquals("300.85", accountW.getBalance().toString());
	}
	
	@Test
	public void testOverdrawException(){
		try{
			accountW.withdraw(new BigDecimal("210"));
			fail("Overdraw exception should be thrown");
		}
		catch(OverdrawException ex) {
			System.out.println(ex.getMessage());
			assertEquals("Overdraw by $-9.55", ex.getMessage());
			assertTrue(true);
		}
	}
}
