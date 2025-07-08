package acctMgr.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.Iterator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import acctMgr.model.Account;
import acctMgr.model.AccountList;

public class AccountListTest {

	private AccountList list;
	@BeforeEach
	public void setUp() throws Exception {
		//list = new AccountList("accounts.txt");
		
		Account acctJake = new Account("Jake", "437", new BigDecimal("3299.65"));
		Account acctLisa = new Account("Lisa", "7463", new BigDecimal("5385.62"));
		list = new AccountList();
		list.addAccount(acctJake);
		list.addAccount(acctLisa);;
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoad() {
		Iterator<Account> iter = list.accountsIterator();
		if(iter.hasNext()) {
			Account account = iter.next();
			String name = account.getName();
			String ID = account.getID();
			BigDecimal balance = account.getBalance();
			System.out.println(name + " " + ID + " " + balance.toString());
			assertEquals("Jake", name);
			assertEquals("437", ID);
			assertEquals("3299.65", balance.toString());
		}
		else {
			fail("NO accounts read");
		}
	}
	@Test
	public void testAddAccount() {
		Account acctBob = new Account("Bob", "7463", new BigDecimal("5385.62"));
		list.addAccount(acctBob);
		assertEquals(acctBob, list.getAccount("Bob"));
	}
	
	@Test
	public void testRemoveAccount() {
		list.removeAccount("Jake");
		assertNull(list.getAccount("Jake"));
	}
		/*
		while(iter.hasNext()) {
			Account account = iter.next();
			
		}*/
	}


