package acctMgr.model;

import java.util.List;

public class MarketAgentInteraction {
    private List<NegotiationAgent> agents;

    public MarketAgentInteraction(List<NegotiationAgent> agents) {
        this.agents = agents;
    }

    public void startTrading() {
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                NegotiationAgent a1 = agents.get(i);
                NegotiationAgent a2 = agents.get(j);
                if (!a1.getCurrency().equals(a2.getCurrency())) {
                    a1.negotiateAndTrade(a2, "USD"); // Trading based on USD as a reference currency
                }
            }
        }
    }
}
