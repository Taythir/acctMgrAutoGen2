package acctMgr.controller;

import java.math.BigDecimal;
import java.util.concurrent.Future;

import acctMgr.model.AgentImpl;
import acctMgr.model.AgentStatus;
import acctMgr.model.IAgent;
import acctMgr.view.AgentView;

public class AgentController extends AbstractController {

	/**
	 * Calls model method according to view event.
	 * @param option - event type
	 */
	public void operation (String option)
	{
		if (option == AgentView.START) {
			BigDecimal amount = ((AgentView)getView()).getAmount();
			((IAgent)getModel()).setAmount(amount);
			Future<?> task = AgentImpl.getFixedPool().submit((IAgent)getModel());
			((IAgent)getModel()).setTask(task);
			AgentImpl.addTask(task);
			((AgentView)getView()).setStatusField(AgentStatus.Pending.toString());
			((AgentView)getView()).disableStartButton();
		}
		else if (option == AgentView.PAUSE) { ((IAgent)getModel()).onPause(); }
		else if (option == AgentView.RESUME) { 
			BigDecimal amount = ((AgentView)getView()).getAmount();
			((IAgent)getModel()).setAmount(amount);
			((IAgent)getModel()).onResume(); 
			}
		else if (option == AgentView.DISMISS) { ((AgentView)getView()).stopAgent(); }
	}
}
