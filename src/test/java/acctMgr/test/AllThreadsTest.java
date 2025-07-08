package acctMgr.test;



import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import acctMgr.model.Account;
import acctMgr.model.OverdrawException;
import acctMgr.model.DepositAgent;
import acctMgr.model.WithdrawAgent;
import acctMgr.model.AgentImpl;

public class AllThreadsTest {
    
    private Account sharedAccount;
    private Account targetAccount;
    
    public static void main(String[] args) {
        AllThreadsTest test = new AllThreadsTest();
        
        System.out.println("Running AllThreadsTest...");
        
        try {
            test.setUp();
            
            System.out.println("1. Testing deposit-transfer race condition...");
            test.testDepositTransferRaceCondition();
            
            System.out.println("2. Testing all threads with dynamic deadlock...");
            test.testAllThreads();
            
            System.out.println("All tests completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                test.tearDown();
            } catch (Exception e) {
                System.err.println("Error during tearDown: " + e.getMessage());
            }
            cleanup();
        }
    }
    
    public void setUp() throws Exception {
        sharedAccount = new Account("Shared Account", "SH001", new BigDecimal("1000.00"));
        targetAccount = new Account("Target Account", "TAR001", new BigDecimal("0.00"));
    }

    public void tearDown() throws Exception {
        sharedAccount = null;
        targetAccount = null;
    }
    
    public static void cleanup() {
        // Shutdown the thread pool to prevent resource leaks
        AgentImpl.shutdownAndAwaitTermination();
    }

    /**
     * Test for race conditions during concurrent deposit and transfer operations
     * This test combines deposit operations with transfer operations to test
     * for race conditions in the shared account state.
     */
    public void testDepositTransferRaceCondition() throws Exception {
        final int numDepositThreads = 5;
        final int numTransferThreads = 3;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(numDepositThreads + numTransferThreads);
        final AtomicInteger successfulTransfers = new AtomicInteger(0);
        final AtomicInteger failedTransfers = new AtomicInteger(0);
        
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
                                successfulTransfers.incrementAndGet();
                            } catch (OverdrawException e) {
                                // Expected when insufficient funds
                                failedTransfers.incrementAndGet();
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
            throw new AssertionError("Shared account balance should be non-negative");
        }
        if (targetBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AssertionError("Target account balance should be non-negative");
        }
        
        // The sum of both accounts should be reasonable
        BigDecimal totalBalance = sharedBalance.add(targetBalance);
        if (totalBalance.compareTo(new BigDecimal("10000.00")) > 0) {
            throw new AssertionError("Total balance should be reasonable");
        }
        
        // Calculate expected values
        // Deposits: 5 threads * 20 iterations * $10.00 = $1000.00
        // Transfers: successfulTransfers * $5.00
        BigDecimal expectedDeposits = new BigDecimal("1000.00");
        BigDecimal expectedTransfers = new BigDecimal(successfulTransfers.get() * 5 + ".00");
        BigDecimal expectedSharedBalance = new BigDecimal("1000.00").add(expectedDeposits).subtract(expectedTransfers);
        
        if (!sharedBalance.equals(expectedSharedBalance)) {
            throw new AssertionError("Shared account final balance should match expected");
        }
        if (!targetBalance.equals(expectedTransfers)) {
            throw new AssertionError("Target account final balance should match expected");
        }
        
        System.out.println("Shared account final balance: " + sharedBalance);
        System.out.println("Target account final balance: " + targetBalance);
        System.out.println("Total balance: " + totalBalance);
        System.out.println("Successful transfers: " + successfulTransfers.get());
        System.out.println("Failed transfers: " + failedTransfers.get());
    }

    /**
     * Test that creates 4 Account instances and runs dynamic deadlock tests on transfer methods
     * along with DepositAgent and WithdrawAgent threads for each account.
     * Checks for absence of deadlock and verifies expected balances after all operations.
     */
    public void testAllThreads() throws Exception {
        // Create 4 Account instances
        Account account1 = new Account("Account 1", "001", new BigDecimal("1000.00"));
        Account account2 = new Account("Account 2", "002", new BigDecimal("1000.00"));
        Account account3 = new Account("Account 3", "003", new BigDecimal("1000.00"));
        Account account4 = new Account("Account 4", "004", new BigDecimal("1000.00"));

        // Create DepositAgent and WithdrawAgent for each account
        int agentIterations = 15;
        BigDecimal depositAmount = new BigDecimal("10.00");
        BigDecimal withdrawAmount = new BigDecimal("5.00");
        
        DepositAgent depAgent1 = new DepositAgent(account1, depositAmount, agentIterations);
        DepositAgent depAgent2 = new DepositAgent(account2, depositAmount, agentIterations);
        DepositAgent depAgent3 = new DepositAgent(account3, depositAmount, agentIterations);
        DepositAgent depAgent4 = new DepositAgent(account4, depositAmount, agentIterations);
        
        WithdrawAgent wdrAgent1 = new WithdrawAgent(account1, withdrawAmount, agentIterations);
        WithdrawAgent wdrAgent2 = new WithdrawAgent(account2, withdrawAmount, agentIterations);
        WithdrawAgent wdrAgent3 = new WithdrawAgent(account3, withdrawAmount, agentIterations);
        WithdrawAgent wdrAgent4 = new WithdrawAgent(account4, withdrawAmount, agentIterations);

        // Create threads for agents
        Thread depThread1 = new Thread(depAgent1);
        Thread depThread2 = new Thread(depAgent2);
        Thread depThread3 = new Thread(depAgent3);
        Thread depThread4 = new Thread(depAgent4);
        
        Thread wdrThread1 = new Thread(wdrAgent1);
        Thread wdrThread2 = new Thread(wdrAgent2);
        Thread wdrThread3 = new Thread(wdrAgent3);
        Thread wdrThread4 = new Thread(wdrAgent4);

        // Create dynamic deadlock test threads for transfer methods
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch transferCompletionLatch = new CountDownLatch(4);
        
        // Transfer thread 1: Pattern 1->2, 2->3, 3->4
        Thread transferThread1 = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    for (int i = 0; i < 30; i++) {
                        account1.transfer(account2, new BigDecimal("5.00"));
                        account2.transfer(account3, new BigDecimal("5.00"));
                        account3.transfer(account4, new BigDecimal("5.00"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    transferCompletionLatch.countDown();
                }
            }
        });

        // Transfer thread 2: Pattern 4->3, 3->2, 2->1
        Thread transferThread2 = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    for (int i = 0; i < 30; i++) {
                        account4.transfer(account3, new BigDecimal("5.00"));
                        account3.transfer(account2, new BigDecimal("5.00"));
                        account2.transfer(account1, new BigDecimal("5.00"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    transferCompletionLatch.countDown();
                }
            }
        });

        // Transfer thread 3: Pattern 1->3, 2->4
        Thread transferThread3 = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    for (int i = 0; i < 30; i++) {
                        account1.transfer(account3, new BigDecimal("5.00"));
                        account2.transfer(account4, new BigDecimal("5.00"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    transferCompletionLatch.countDown();
                }
            }
        });

        // Transfer thread 4: Pattern 3->1, 4->2
        Thread transferThread4 = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                    for (int i = 0; i < 30; i++) {
                        account3.transfer(account1, new BigDecimal("5.00"));
                        account4.transfer(account2, new BigDecimal("5.00"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    transferCompletionLatch.countDown();
                }
            }
        });

        // Start all agent threads
        depThread1.start();
        depThread2.start();
        depThread3.start();
        depThread4.start();
        
        wdrThread1.start();
        wdrThread2.start();
        wdrThread3.start();
        wdrThread4.start();

        // Start transfer threads
        transferThread1.start();
        transferThread2.start();
        transferThread3.start();
        transferThread4.start();

        // Signal all threads to start simultaneously
        startLatch.countDown();

        // Wait for all threads to complete with timeout
        depThread1.join(15000);
        depThread2.join(15000);
        depThread3.join(15000);
        depThread4.join(15000);
        
        wdrThread1.join(15000);
        wdrThread2.join(15000);
        wdrThread3.join(15000);
        wdrThread4.join(15000);

        boolean transferCompleted = transferCompletionLatch.await(20, TimeUnit.SECONDS);
        
        transferThread1.join(15000);
        transferThread2.join(15000);
        transferThread3.join(15000);
        transferThread4.join(15000);

        // Check for deadlock (all threads should complete)
        if (!transferCompleted) {
            throw new AssertionError("Potential deadlock detected - transfer threads did not complete within timeout period");
        }

        // Calculate expected balances
        // Each account starts with $1000.00
        // Deposits: 15 iterations * $10.00 = $150.00
        // Withdrawals: 15 iterations * $5.00 = $75.00
        // Net from agents: +$150.00 - $75.00 = +$75.00
        // Transfers are circular and net to zero (as they transfer equal amounts in opposite directions)
        BigDecimal expectedBalance = new BigDecimal("1075.00");

        // Verify final balances
        if (!account1.getBalance().equals(expectedBalance)) {
            throw new AssertionError("Account1 final balance should match expected");
        }
        if (!account2.getBalance().equals(expectedBalance)) {
            throw new AssertionError("Account2 final balance should match expected");
        }
        if (!account3.getBalance().equals(expectedBalance)) {
            throw new AssertionError("Account3 final balance should match expected");
        }
        if (!account4.getBalance().equals(expectedBalance)) {
            throw new AssertionError("Account4 final balance should match expected");
        }

        // Print summary
        System.out.println("AllThreads test completed successfully:");
        System.out.println("Account1 balance: " + account1.getBalance());
        System.out.println("Account2 balance: " + account2.getBalance());
        System.out.println("Account3 balance: " + account3.getBalance());
        System.out.println("Account4 balance: " + account4.getBalance());
        System.out.println("Expected balance: " + expectedBalance);
        System.out.println("No deadlock detected - all threads completed successfully");
    }
} 