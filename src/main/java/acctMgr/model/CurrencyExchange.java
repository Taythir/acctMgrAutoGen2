package acctMgr.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CurrencyExchange {
    private static final Map<String, BigDecimal> rates = new HashMap<>();

    static {
        rates.put("USD/EUR", new BigDecimal("0.92"));
        rates.put("EUR/USD", new BigDecimal("1.09"));
        rates.put("USD/GBP", new BigDecimal("0.78"));
        rates.put("GBP/USD", new BigDecimal("1.28"));
    }

    public static BigDecimal getRate(String from, String to) {
        return rates.getOrDefault(from + "/" + to, BigDecimal.ONE);
    }
    
    public static void updateRate(String from, String to, BigDecimal newRate) {
        rates.put(from + "/" + to, newRate);
    }
}
