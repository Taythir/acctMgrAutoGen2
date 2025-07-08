package acctMgr.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    AccountTest.class,
    AccountThreadSafetyTest.class
})
public class AccountTests {
}
