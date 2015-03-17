package unibg.saoms.MAPEKMS;

import unibg.saoms.MAPEKclass;
import unibg.saoms.MasterRobotManagingSystem;
import unibg.saoms.util.MRcounter;
import unibg.saoms.msg.EEMessage;

/**
 * This class manages awake/asleep requests to be sent to robots.
 * It is a MAPE-K class
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class AwakeAsleepManagingSystem extends MAPEKclass {

	// Fields----------------------------------------------------------------
	// MasterRobotMS to work on
	protected MasterRobotManagingSystem MRMS;
	private boolean newMsg;
	private int robToAsleep;
	private int robToAwake;
	private int totalSum;

	// MAPE------------------------------------------------------------------
	/**
	 * Method to call a MAPE loop
	 */
	public void callMAPE(MasterRobotManagingSystem r){
		MRMS=r;
		monitor();
		analyze();
		plan();
		execute();
	}
	
	@Override
	public void monitor(){
		// AwakeAsleepManagingSystem MAPE
		// @M
		//System.out.println("AwakeAsleepManagingSystem says: Monitoring...");
		
		// Monitoring variables
	}

	@Override
	public void analyze() {
		// AwakeAsleepManagingSystem MAPE
		// @A
		//System.out.println("AwakeAsleepManagingSystem says: Analyzing...");
		
		newMsg = !MRMS.getEEList().isEmpty();
	}

	@Override
	public void plan() {
		// AwakeAsleepManagingSystem MAPE
		// @P
		//System.out.println("AwakeAsleepManagingSystem says: Planning...");
		
		robToAsleep=0;
		robToAwake=0;
		// If there are new messages...
		if(newMsg){
			for(EEMessage eemsg : MRMS.getEEList()){
				// Add them to the counter list
				// Increase number of robot to awake
				robToAwake=(int)(eemsg.expLug/MRMS.getNumOfLugForRob());
			}
		}

		// For each counter in the list
		for(MRcounter mc : MRMS.getCounter()){
			// If the counter has come to an end
			if(mc.getCount() == MRMS.getCounterLimit()){
				// Increase number of robot to asleep
				robToAsleep=mc.getNumOfRobots();
			}
		}

		// Calculate final number of robots to awake/asleep
		totalSum = robToAwake - robToAsleep;
	}

	@Override
	public void execute() {
		// AwakeAsleepManagingSystem MAPE
		// @E
		//System.out.println("AwakeAsleepManagingSystem says: Execute...");
		
		// Awake/Asleep totalSum robots
		if(totalSum!=0){
			MRMS.awakeasleep(totalSum);
		}
	}
}
