package acctMgr.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    DepositAgentTest.class,
    WithdrawAgentTest.class
})
public class AgentTests {
}
