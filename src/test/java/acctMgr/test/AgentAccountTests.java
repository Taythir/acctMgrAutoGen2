package acctMgr.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    AccountTests.class,
    AgentTests.class
})
public class AgentAccountTests {
}
