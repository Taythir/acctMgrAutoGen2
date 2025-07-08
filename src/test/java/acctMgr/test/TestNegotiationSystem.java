package acctMgr.test;

import acctMgr.model.*;
import java.math.BigDecimal;
import java.util.Arrays;

public class TestNegotiationSystem {
    public static void main(String[] args) {
        Account acc1 = new Account("Trader1", "T1", new BigDecimal("500"));
        Account acc2 = new Account("Trader2", "T2", new BigDecimal("600"));

        NegotiationAgent agent1 = new NegotiationAgent(acc1, new BigDecimal("100"), "EUR");
        NegotiationAgent agent2 = new NegotiationAgent(acc2, new BigDecimal("150"), "USD");

        MarketAgentInteraction market = new MarketAgentInteraction(Arrays.asList(agent1, agent2));
        market.startTrading();
        
        System.out.println("Final Balances: ");
        System.out.println(agent1.getAccount().getBalance());
        System.out.println(agent2.getAccount().getBalance());
    }
}
