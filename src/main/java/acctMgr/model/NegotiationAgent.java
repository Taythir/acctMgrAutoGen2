package acctMgr.model;

import java.math.BigDecimal;

public class NegotiationAgent extends DepositAgent {
    private String currency;
    
    public NegotiationAgent(Account account, BigDecimal amount, String currency) {
        super(account, amount);
        this.currency = currency;
    }

    public boolean negotiateAndTrade(NegotiationAgent other, String targetCurrency) {
        BigDecimal myRate = CurrencyExchange.getRate(this.currency, targetCurrency);
        BigDecimal otherRate = CurrencyExchange.getRate(other.currency, targetCurrency);

        // Ensure both agents can profit
        if (myRate.compareTo(otherRate) < 0) {
            System.out.println(this.getName() + " wants a better rate.");
            return false; // No agreement
        }

        // Transfer and update balances
        System.out.println("myRate: " + myRate + " getTransferred: " + this.getTransferred());
        BigDecimal convertedAmount = this.getTransferred().multiply(myRate);
        try {
        	System.out.println("Converted amount :" + convertedAmount.toString());
            this.getAccount().transfer(other.getAccount(), convertedAmount);
            System.out.println("Trade Executed: " + this.getName() + " traded with " + other.getName());
            return true;
        } catch (OverdrawException e) {
            System.out.println("Trade Failed: Insufficient funds.");
            return false;
        }
    }

    public String getCurrency() {
        return currency;
    }
}
