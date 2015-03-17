package unibg.saoms.MAPEKMS;

import java.util.Iterator;

import unibg.robotics.tca.Message;
import unibg.robotics.tca.TopicNotFoundException;
import unibg.saoms.MAPEKclass;
import unibg.saoms.msg.RMessage;

/**
 * This is the class used to manage a asleep/awake requests. 
 * It is a MAPE-K class
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class RobotManagingSystem extends MAPEKclass {
	
	// Fields----------------------------------------------------------------
	private Iterator<Message> rl;
	private boolean changeStatus;
	private int exec;
	private RMessage rmsg;

	
	// MAPE------------------------------------------------------------------
	@Override
	public void monitor(){
		// RobotManagingSystem MAPE
		// @M
		//System.out.println("RobotManagingSystem says: Monitoring...");
		
		rl=null;
		// Monitor awake/asleep robot list
		try {
			rl=getRob().receive(getRob().getRMSTopic());
		} catch (TopicNotFoundException e) {e.printStackTrace();}		
	}

	@Override
	public void analyze(){
		// RobotManagingSystem MAPE
		// @A
		//System.out.println("RobotManagingSystem says: Analyzing...");
		
		changeStatus = false;
		// Analyze messages and find my id
		while(this.rl.hasNext()){
			Message msg;
			msg=rl.next();
			RMessage t = (RMessage)msg.getMessage();
			// If this robot ID is in the list, change status
			if(getRob().getRobotID() == t.robID){
				rmsg = t;
				changeStatus = true;
			}
		}
	}

	@Override
	public void plan(){
		// RobotManagingSystem MAPE
		// @P
		//System.out.println("RobotManagingSystem says: Planning...");
		
		exec = 0;
		// Plan how to change status: active -> exec1, else exec2
		if(changeStatus){
			if(rmsg.status){
				exec=1;
			}
			else{
				exec=2;
			}
		}
	}

	@Override
	public void execute(){
		// RobotManagingSystem MAPE
		// @E
		//System.out.println("RobotManagingSystem says: Execute...");

		// New robots to add
		switch(this.exec){
		case 1:
			getRob().setInCharge(false);
			break;
		case 2:
			getRob().setReadyToGoUnderCharge(true);
			break;
		}
	}
}
