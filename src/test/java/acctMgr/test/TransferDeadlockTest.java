package acctMgr.test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import acctMgr.model.Account;
import acctMgr.model.OverdrawException;
import acctMgr.model.DepositAgent;
import acctMgr.model.WithdrawAgent;

public class TransferDeadlockTest {

    public static void main(String[] args) {
        TransferDeadlockTest test = new TransferDeadlockTest();
        
        System.out.println("Running basic transfer deadlock test...");
        test.testTransferDeadlock();
        
        System.out.println("Running complex transfer deadlock test...");
        test.testComplexTransferDeadlock();
        
        System.out.println("Running wait-for cycle detection test...");
        test.testWaitForCycleDetection();
        
        System.out.println("Running all threads test...");
        test.testAllThreads();
        
        System.out.println("All tests completed successfully!");
    }

    public void testTransferDeadlock() {
        try {
            // Create two accounts with sufficient initial balances
            Account accountA = new Account("Account A", "A001", new BigDecimal("1000.00"));
            Account accountB = new Account("Account B", "B001", new BigDecimal("1000.00"));
            
            // Create a thread that transfers from accountA to accountB repeatedly
            Thread thread1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        // Perform 100 transfers of $10.00 each from accountA to accountB
                        for (int i = 0; i < 100; i++) {
                            accountA.transfer(accountB, new BigDecimal("10.00"));
                        }
                    } catch (OverdrawException e) {
                        System.err.println("OverdrawException should not be thrown in thread1: " + e.getMessage());
                    }
                }
            });
            
            // Create a thread that transfers from accountB to accountA repeatedly
            Thread thread2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        // Perform 100 transfers of $10.00 each from accountB to accountA
                        for (int i = 0; i < 100; i++) {
                            accountB.transfer(accountA, new BigDecimal("10.00"));
                        }
                    } catch (OverdrawException e) {
                        System.err.println("OverdrawException should not be thrown in thread2: " + e.getMessage());
                    }
                }
            });
            
            // Start both threads
            thread1.start();
            thread2.start();
            
            // Wait for both threads to finish
            thread1.join();
            thread2.join();
            
            // After the transfers, the net effect should be zero so each account balance remains unchanged.
            if (!accountA.getBalance().equals(new BigDecimal("1000.00"))) {
                throw new AssertionError("Balance of accountA should remain 1000.00, but was " + accountA.getBalance());
            }
            if (!accountB.getBalance().equals(new BigDecimal("1000.00"))) {
                throw new AssertionError("Balance of accountB should remain 1000.00, but was " + accountB.getBalance());
            }
            
            System.out.println("Basic transfer deadlock test passed!");
            
        } catch (Exception e) {
            System.err.println("Basic transfer deadlock test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test for potential deadlock scenarios with multiple accounts and complex transfer patterns.
     * This test creates a scenario where multiple threads perform transfers between multiple accounts
     * in different orders, which could potentially lead to deadlocks if the locking strategy is flawed.
     */
    public void testComplexTransferDeadlock() {
        try {
            // Create multiple accounts with different ID orders to test the locking strategy
            Account account1 = new Account("Account 1", "001", new BigDecimal("1000.00"));
            Account account2 = new Account("Account 2", "002", new BigDecimal("1000.00"));
            Account account3 = new Account("Account 3", "003", new BigDecimal("1000.00"));
            Account account4 = new Account("Account 4", "004", new BigDecimal("1000.00"));
            
            final CountDownLatch startLatch = new CountDownLatch(1);
            final CountDownLatch completionLatch = new CountDownLatch(4);
            
            // Thread 1: Transfer in pattern 1->2, 2->3, 3->4
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
            
            // Thread 2: Transfer in pattern 4->3, 3->2, 2->1
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
            
            // Thread 3: Transfer in pattern 1->3, 2->4
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
            
            // Thread 4: Transfer in pattern 3->1, 4->2
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
            boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
            
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
            
            System.out.println("Complex transfer deadlock test passed!");
            
        } catch (Exception e) {
            System.err.println("Complex transfer deadlock test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test that specifically tries to create a wait-for cycle by having threads
     * attempt to acquire locks in different orders simultaneously.
     */
    public void testWaitForCycleDetection() {
        try {
            // Create accounts with IDs that will test the locking order
            Account accountA = new Account("Account A", "A001", new BigDecimal("1000.00"));
            Account accountB = new Account("Account B", "B001", new BigDecimal("1000.00"));
            
            final CountDownLatch readyLatch = new CountDownLatch(2);
            final CountDownLatch startLatch = new CountDownLatch(1);
            final CountDownLatch completionLatch = new CountDownLatch(2);
            
            // Thread that will try to transfer A->B
            Thread threadAB = new Thread(new Runnable() {
                public void run() {
                    try {
                        readyLatch.countDown();
                        startLatch.await();
                        
                        // Perform multiple transfers to increase chance of deadlock
                        for (int i = 0; i < 100; i++) {
                            accountA.transfer(accountB, new BigDecimal("1.00"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        completionLatch.countDown();
                    }
                }
            });
            
            // Thread that will try to transfer B->A
            Thread threadBA = new Thread(new Runnable() {
                public void run() {
                    try {
                        readyLatch.countDown();
                        startLatch.await();
                        
                        // Perform multiple transfers to increase chance of deadlock
                        for (int i = 0; i < 100; i++) {
                            accountB.transfer(accountA, new BigDecimal("1.00"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        completionLatch.countDown();
                    }
                }
            });
            
            threadAB.start();
            threadBA.start();
            
            // Wait for both threads to be ready
            readyLatch.await();
            
            // Signal both threads to start simultaneously
            startLatch.countDown();
            
            // Wait for completion
            boolean completed = completionLatch.await(8, TimeUnit.SECONDS);
            
            if (!completed) {
                throw new AssertionError("Potential deadlock detected - threads did not complete within timeout period");
            }
            
            // Verify balances remain unchanged
            if (!accountA.getBalance().equals(new BigDecimal("1000.00"))) {
                throw new AssertionError("Balance of accountA should remain 1000.00, but was " + accountA.getBalance());
            }
            if (!accountB.getBalance().equals(new BigDecimal("1000.00"))) {
                throw new AssertionError("Balance of accountB should remain 1000.00, but was " + accountB.getBalance());
            }
            
            System.out.println("Wait-for cycle detection test passed!");
            
        } catch (Exception e) {
            System.err.println("Wait-for cycle detection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testAllThreads() {
        try {
            // 1. Create accounts
            Account account1 = new Account("Account 1", "001", new BigDecimal("1000.00"));
            Account account2 = new Account("Account 2", "002", new BigDecimal("1000.00"));
            Account account3 = new Account("Account 3", "003", new BigDecimal("1000.00"));
            Account account4 = new Account("Account 4", "004", new BigDecimal("1000.00"));

            // 2. Create DepositAgent and WithdrawAgent for each account
            int agentIters = 20;
            BigDecimal depositAmt = new BigDecimal("10.00");
            BigDecimal withdrawAmt = new BigDecimal("5.00");
            DepositAgent dep1 = new DepositAgent(account1, depositAmt, agentIters);
            DepositAgent dep2 = new DepositAgent(account2, depositAmt, agentIters);
            DepositAgent dep3 = new DepositAgent(account3, depositAmt, agentIters);
            DepositAgent dep4 = new DepositAgent(account4, depositAmt, agentIters);
            WithdrawAgent wdr1 = new WithdrawAgent(account1, withdrawAmt, agentIters);
            WithdrawAgent wdr2 = new WithdrawAgent(account2, withdrawAmt, agentIters);
            WithdrawAgent wdr3 = new WithdrawAgent(account3, withdrawAmt, agentIters);
            WithdrawAgent wdr4 = new WithdrawAgent(account4, withdrawAmt, agentIters);
            Thread depT1 = new Thread(dep1);
            Thread depT2 = new Thread(dep2);
            Thread depT3 = new Thread(dep3);
            Thread depT4 = new Thread(dep4);
            Thread wdrT1 = new Thread(wdr1);
            Thread wdrT2 = new Thread(wdr2);
            Thread wdrT3 = new Thread(wdr3);
            Thread wdrT4 = new Thread(wdr4);

            // 3. Prepare transfer threads (as in testComplexTransferDeadlock)
            final CountDownLatch startLatch = new CountDownLatch(1);
            final CountDownLatch completionLatch = new CountDownLatch(4);
            Thread t1 = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 50; i++) {
                        account1.transfer(account2, new BigDecimal("5.00"));
                        account2.transfer(account3, new BigDecimal("5.00"));
                        account3.transfer(account4, new BigDecimal("5.00"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
                finally { completionLatch.countDown(); }
            });
            Thread t2 = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 50; i++) {
                        account4.transfer(account3, new BigDecimal("5.00"));
                        account3.transfer(account2, new BigDecimal("5.00"));
                        account2.transfer(account1, new BigDecimal("5.00"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
                finally { completionLatch.countDown(); }
            });
            Thread t3 = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 50; i++) {
                        account1.transfer(account3, new BigDecimal("5.00"));
                        account2.transfer(account4, new BigDecimal("5.00"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
                finally { completionLatch.countDown(); }
            });
            Thread t4 = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 50; i++) {
                        account3.transfer(account1, new BigDecimal("5.00"));
                        account4.transfer(account2, new BigDecimal("5.00"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
                finally { completionLatch.countDown(); }
            });

            // 4. Start all agent and transfer threads
            depT1.start(); depT2.start(); depT3.start(); depT4.start();
            wdrT1.start(); wdrT2.start(); wdrT3.start(); wdrT4.start();
            t1.start(); t2.start(); t3.start(); t4.start();
            // 5. Start transfer threads simultaneously
            startLatch.countDown();

            // 6. Wait for all threads to finish (with timeout)
            depT1.join(10000); depT2.join(10000); depT3.join(10000); depT4.join(10000);
            wdrT1.join(10000); wdrT2.join(10000); wdrT3.join(10000); wdrT4.join(10000);
            boolean completed = completionLatch.await(15, TimeUnit.SECONDS);
            t1.join(10000); t2.join(10000); t3.join(10000); t4.join(10000);
            if (!completed) {
                throw new AssertionError("Potential deadlock detected - transfer threads did not complete within timeout period");
            }

            // 7. Calculate expected balances
            // Each account: +20*10 (deposit) -20*5 (withdraw) = +100
            // Transfers are circular and net to zero as in testComplexTransferDeadlock
            BigDecimal expected = new BigDecimal("1100.00");
            if (!account1.getBalance().equals(expected)) {
                throw new AssertionError("Balance of account1 should be " + expected + ", but was " + account1.getBalance());
            }
            if (!account2.getBalance().equals(expected)) {
                throw new AssertionError("Balance of account2 should be " + expected + ", but was " + account2.getBalance());
            }
            if (!account3.getBalance().equals(expected)) {
                throw new AssertionError("Balance of account3 should be " + expected + ", but was " + account3.getBalance());
            }
            if (!account4.getBalance().equals(expected)) {
                throw new AssertionError("Balance of account4 should be " + expected + ", but was " + account4.getBalance());
            }
            System.out.println("AllThreads test passed: No deadlock and all balances correct.");
        } catch (Exception e) {
            System.err.println("AllThreads test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
