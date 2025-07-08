package acctMgr.test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.List;

import acctMgr.model.Account;
import acctMgr.model.OverdrawException;
import acctMgr.model.DepositAgent;
import acctMgr.model.WithdrawAgent;
import acctMgr.model.AgentImpl;

public class ConcurrentFaultsTest {
    
    private Account sharedAccount;
    private DepositAgent depositAgent;
    private WithdrawAgent withdrawAgent;
    
    public static void main(String[] args) {
        ConcurrentFaultsTest test = new ConcurrentFaultsTest();
        
        System.out.println("Running concurrent faults tests...");
        
        try {
            test.setUp();
            
            System.out.println("1. Testing concurrent agent operations...");
            test.testConcurrentAgentOperations();
            
            System.out.println("2. Testing transfer deadlock scenario...");
            test.testTransferDeadlockScenario();
            
            System.out.println("3. Testing balance race condition...");
            test.testBalanceRaceCondition();
            
            System.out.println("4. Testing autoWithdraw deadlock...");
            test.testAutoWithdrawDeadlock();
            
            System.out.println("5. Testing deposit-transfer race condition...");
            test.testDepositTransferRaceCondition();
            
            System.out.println("All concurrent faults tests completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            test.tearDown();
            cleanup();
        }
    }
    
    public void setUp() {
        sharedAccount = new Account("Shared Account", "SH001", new BigDecimal("1000.00"));
        depositAgent = new DepositAgent(sharedAccount, new BigDecimal("10.00"), 20);
        withdrawAgent = new WithdrawAgent(sharedAccount, new BigDecimal("5.00"), 20);
    }

    public void tearDown() {
        sharedAccount = null;
        depositAgent = null;
        withdrawAgent = null;
    }
    
    public static void cleanup() {
        // Shutdown the thread pool to prevent resource leaks
        AgentImpl.shutdownAndAwaitTermination();
    }

    /**
     * Test for race conditions during concurrent deposit and withdraw operations
     * This test verifies that the Account class maintains thread safety under
     * concurrent access from multiple agents.
     */
    public void testConcurrentAgentOperations() throws Exception {
        // Create multiple deposit and withdraw agents
        List<DepositAgent> depositAgents = new ArrayList<>();
        List<WithdrawAgent> withdrawAgents = new ArrayList<>();
        
        // Create 5 deposit agents and 5 withdraw agents
        for (int i = 0; i < 5; i++) {
            depositAgents.add(new DepositAgent(sharedAccount, new BigDecimal("5.00"), 10));
            withdrawAgents.add(new WithdrawAgent(sharedAccount, new BigDecimal("3.00"), 10));
        }
        
        // Start all agents
        List<Thread> threads = new ArrayList<>();
        
        for (DepositAgent agent : depositAgents) {
            Thread t = new Thread(agent);
            threads.add(t);
            t.start();
        }
        
        for (WithdrawAgent agent : withdrawAgents) {
            Thread t = new Thread(agent);
            threads.add(t);
            t.start();
        }
        
        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }
        
        // Calculate expected final balance
        // Deposits: 5 agents * 10 iterations * $5.00 = $250.00
        // Withdrawals: 5 agents * 10 iterations * $3.00 = $150.00
        // Net change: $250.00 - $150.00 = $100.00
        // Final balance: $1000.00 + $100.00 = $1100.00
        BigDecimal expectedBalance = new BigDecimal("1100.00");
        
        if (!sharedAccount.getBalance().equals(expectedBalance)) {
            throw new AssertionError("Final balance should be " + expectedBalance + ", but was " + sharedAccount.getBalance());
        }
        
        // Verify that no overdraw exceptions occurred
        // This would indicate race conditions in balance checking
        BigDecimal totalDeposited = BigDecimal.ZERO;
        BigDecimal totalWithdrawn = BigDecimal.ZERO;
        
        for (DepositAgent agent : depositAgents) {
            totalDeposited = totalDeposited.add(agent.getTransferred());
        }
        
        for (WithdrawAgent agent : withdrawAgents) {
            totalWithdrawn = totalWithdrawn.add(agent.getTransferred());
        }
        
        if (!totalDeposited.equals(new BigDecimal("250.00"))) {
            throw new AssertionError("Total deposited amount should be $250.00, but was " + totalDeposited);
        }
        
        if (!totalWithdrawn.equals(new BigDecimal("150.00"))) {
            throw new AssertionError("Total withdrawn amount should be $150.00, but was " + totalWithdrawn);
        }
        
        System.out.println("Concurrent agent operations test passed!");
    }

    /**
     * Test for deadlocks during concurrent transfer operations between multiple accounts
     * This test creates a scenario where multiple threads perform transfers between
     * multiple accounts in different orders, which could potentially lead to deadlocks.
     */
    public void testTransferDeadlockScenario() throws Exception {
        // Create multiple accounts with different ID orders
        Account account1 = new Account("Account 1", "001", new BigDecimal("1000.00"));
        Account account2 = new Account("Account 2", "002", new BigDecimal("1000.00"));
        Account account3 = new Account("Account 3", "003", new BigDecimal("1000.00"));
        Account account4 = new Account("Account 4", "004", new BigDecimal("1000.00"));
        
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(4);
        
        // Thread 1: Transfer pattern 1->2, 2->3, 3->4
        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    for (int i = 0; i < 50; i++) {
                        account1.transfer(account2, new BigDecimal("5.00"));
                        account2.transfer(account3, new BigDecimal("5.00"));
                        account3.transfer(account4, new BigDecimal("5.00"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            }
        });
        
        // Thread 2: Transfer pattern 4->3, 3->2, 2->1
        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    for (int i = 0; i < 50; i++) {
                        account4.transfer(account3, new BigDecimal("5.00"));
                        account3.transfer(account2, new BigDecimal("5.00"));
                        account2.transfer(account1, new BigDecimal("5.00"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            }
        });
        
        // Thread 3: Transfer pattern 1->3, 2->4
        Thread thread3 = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    for (int i = 0; i < 50; i++) {
                        account1.transfer(account3, new BigDecimal("5.00"));
                        account2.transfer(account4, new BigDecimal("5.00"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            }
        });
        
        // Thread 4: Transfer pattern 3->1, 4->2
        Thread thread4 = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    for (int i = 0; i < 50; i++) {
                        account3.transfer(account1, new BigDecimal("5.00"));
                        account4.transfer(account2, new BigDecimal("5.00"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            }
        });
        
        // Start all threads
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        
        // Signal all threads to start simultaneously
        startLatch.countDown();
        
        // Wait for completion with timeout
        boolean completed = completionLatch.await(15, TimeUnit.SECONDS);
        
        if (!completed) {
            throw new AssertionError("Potential deadlock detected - threads did not complete within timeout period");
        }
        
        // Verify final balances (they should remain at original values due to circular transfers)
        if (!account1.getBalance().equals(new BigDecimal("1000.00"))) {
            throw new AssertionError("Balance of account1 should remain 1000.00, but was " + account1.getBalance());
        }
        if (!account2.getBalance().equals(new BigDecimal("1000.00"))) {
            throw new AssertionError("Balance of account2 should remain 1000.00, but was " + account2.getBalance());
        }
        if (!account3.getBalance().equals(new BigDecimal("1000.00"))) {
            throw new AssertionError("Balance of account3 should remain 1000.00, but was " + account3.getBalance());
        }
        if (!account4.getBalance().equals(new BigDecimal("1000.00"))) {
            throw new AssertionError("Balance of account4 should remain 1000.00, but was " + account4.getBalance());
        }
        
        System.out.println("Transfer deadlock scenario test passed!");
    }

    /**
     * Test for race conditions during concurrent balance checks and modifications
     * This test specifically targets the balance checking and modification operations
     * to ensure they are atomic and thread-safe.
     */
    public void testBalanceRaceCondition() throws Exception {
        final int numThreads = 10;
        final int operationsPerThread = 100;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(numThreads);
        final AtomicInteger successfulOperations = new AtomicInteger(0);
        final AtomicInteger failedOperations = new AtomicInteger(0);
        
        // Create threads that will perform concurrent balance checks and modifications
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        startLatch.await();
                        
                        for (int j = 0; j < operationsPerThread; j++) {
                            try {
                                // Perform a balance check followed by a modification
                                BigDecimal currentBalance = sharedAccount.getBalance();
                                
                                if (threadId % 2 == 0) {
                                    // Even threads deposit
                                    sharedAccount.deposit(new BigDecimal("1.00"));
                                    successfulOperations.incrementAndGet();
                                } else {
                                    // Odd threads withdraw (if sufficient funds)
                                    if (currentBalance.compareTo(new BigDecimal("1.00")) >= 0) {
                                        sharedAccount.withdraw(new BigDecimal("1.00"));
                                        successfulOperations.incrementAndGet();
                                    } else {
                                        failedOperations.incrementAndGet();
                                    }
                                }
                            } catch (OverdrawException e) {
                                failedOperations.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        completionLatch.countDown();
                    }
                }
            });
            thread.start();
        }
        
        // Signal all threads to start simultaneously
        startLatch.countDown();
        
        // Wait for completion
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
        
        if (!completed) {
            throw new AssertionError("All threads should complete within timeout");
        }
        
        // Verify that the account balance is consistent
        // The final balance should be reasonable (not negative, not excessively large)
        BigDecimal finalBalance = sharedAccount.getBalance();
        if (finalBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AssertionError("Final balance should be non-negative, but was " + finalBalance);
        }
        if (finalBalance.compareTo(new BigDecimal("10000.00")) > 0) {
            throw new AssertionError("Final balance should not be unreasonably large, but was " + finalBalance);
        }
        
        System.out.println("Final balance: " + finalBalance);
        System.out.println("Successful operations: " + successfulOperations.get());
        System.out.println("Failed operations: " + failedOperations.get());
        System.out.println("Balance race condition test passed!");
    }

    /**
     * Test for deadlocks during concurrent autoWithdraw operations
     * This test creates a scenario where multiple threads call autoWithdraw
     * simultaneously, which could potentially lead to deadlocks due to wait() calls.
     */
    public void testAutoWithdrawDeadlock() throws Exception {
        // Create an account with limited funds
        Account limitedAccount = new Account("Limited Account", "LIM001", new BigDecimal("50.00"));
        
        final int numThreads = 5;
        final int attemptsPerThread = 3;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(numThreads);
        final AtomicInteger successfulWithdrawals = new AtomicInteger(0);
        final AtomicInteger failedWithdrawals = new AtomicInteger(0);
        
        // Create threads that will perform autoWithdraw operations
        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        startLatch.await();
                        
                        // Create a simple agent for autoWithdraw
                        WithdrawAgent agent = new WithdrawAgent(limitedAccount, new BigDecimal("20.00"), attemptsPerThread);
                        
                        // Perform autoWithdraw operations
                        for (int j = 0; j < attemptsPerThread; j++) {
                            try {
                                limitedAccount.autoWithdraw(new BigDecimal("20.00"), agent);
                                successfulWithdrawals.incrementAndGet();
                            } catch (InterruptedException e) {
                                failedWithdrawals.incrementAndGet();
                            } catch (Exception e) {
                                failedWithdrawals.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        completionLatch.countDown();
                    }
                }
            });
            thread.start();
        }
        
        // Signal all threads to start simultaneously
        startLatch.countDown();
        
        // Wait for completion with timeout
        boolean completed = completionLatch.await(15, TimeUnit.SECONDS);
        
        if (!completed) {
            throw new AssertionError("Threads did not complete within timeout period");
        }
        
        // Verify that the account balance is reasonable
        BigDecimal finalBalance = limitedAccount.getBalance();
        int totalWithdrawn = successfulWithdrawals.get() * 20;
        if (finalBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AssertionError("Final balance should be non-negative, but was " + finalBalance);
        }
        if (finalBalance.add(new BigDecimal(totalWithdrawn)).compareTo(new BigDecimal("50.00")) != 0) {
            throw new AssertionError("Total withdrawn + final balance should equal initial balance");
        }
        // Only two successful withdrawals of $20 are possible from $50
        if (successfulWithdrawals.get() != 2) {
            throw new AssertionError("Should be exactly 2 successful withdrawals, but got " + successfulWithdrawals.get());
        }
        System.out.println("AutoWithdraw test: Successful withdrawals: " + successfulWithdrawals.get() + ", Failed withdrawals: " + failedWithdrawals.get());
        System.out.println("AutoWithdraw deadlock/starvation test passed!");
    }

    /**
     * Test for race conditions during concurrent deposit and transfer operations
     * This test combines deposit operations with transfer operations to test
     * for race conditions in the shared account state.
     */
    public void testDepositTransferRaceCondition() throws Exception {
        // Create a target account for transfers
        Account targetAccount = new Account("Target Account", "TAR001", new BigDecimal("0.00"));
        
        final int numDepositThreads = 5;
        final int numTransferThreads = 3;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(numDepositThreads + numTransferThreads);
        
        // Create deposit threads
        for (int i = 0; i < numDepositThreads; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        startLatch.await();
                        
                        for (int j = 0; j < 20; j++) {
                            sharedAccount.deposit(new BigDecimal("10.00"));
                            Thread.sleep(10); // Small delay to increase interleaving
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        completionLatch.countDown();
                    }
                }
            });
            thread.start();
        }
        
        // Create transfer threads
        for (int i = 0; i < numTransferThreads; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        startLatch.await();
                        
                        for (int j = 0; j < 30; j++) {
                            try {
                                sharedAccount.transfer(targetAccount, new BigDecimal("5.00"));
                            } catch (OverdrawException e) {
                                // Expected when insufficient funds
                            }
                            Thread.sleep(10); // Small delay to increase interleaving
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        completionLatch.countDown();
                    }
                }
            });
            thread.start();
        }
        
        // Signal all threads to start simultaneously
        startLatch.countDown();
        
        // Wait for completion
        boolean completed = completionLatch.await(20, TimeUnit.SECONDS);
        
        if (!completed) {
            throw new AssertionError("All threads should complete within timeout");
        }
        
        // Verify that the final balances are consistent
        BigDecimal sharedBalance = sharedAccount.getBalance();
        BigDecimal targetBalance = targetAccount.getBalance();
        
        if (sharedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AssertionError("Shared account balance should be non-negative, but was " + sharedBalance);
        }
        if (targetBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AssertionError("Target account balance should be non-negative, but was " + targetBalance);
        }
        
        // The sum of both accounts should be reasonable
        BigDecimal totalBalance = sharedBalance.add(targetBalance);
        if (totalBalance.compareTo(new BigDecimal("10000.00")) > 0) {
            throw new AssertionError("Total balance should be reasonable, but was " + totalBalance);
        }
        
        System.out.println("Shared account final balance: " + sharedBalance);
        System.out.println("Target account final balance: " + targetBalance);
        System.out.println("Total balance: " + totalBalance);
        System.out.println("Deposit-transfer race condition test passed!");
    }
} 