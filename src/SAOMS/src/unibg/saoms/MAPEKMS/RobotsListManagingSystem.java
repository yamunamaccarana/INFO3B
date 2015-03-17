package unibg.saoms.MAPEKMS;

import unibg.saoms.MAPEKclass;

/**
 * This is the class used to manage a List containing all actually existent Robots. 
 * It is a MAPE-K class
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class RobotsListManagingSystem extends MAPEKclass {
	
	// Fields----------------------------------------------------------------
	
	// MAPE------------------------------------------------------------------
	@Override
	public void monitor(){
		// RobotListManagingSystem MAPE
		// @M
		//System.out.println("RobotListManagingSystem says: Monitoring...");

		// Monitor exceptional active robot lists from Masters
	}
	
	@Override
	public void analyze(){
		// RobotListManagingSystem MAPE
		// @A
		//System.out.println("RobotListManagingSystem says: Analyzing...");
		
		// Analyze exceptional active robot lists from Masters

	}
	
	@Override
	public void plan(){
		// RobotListManagingSystem MAPE
		// @P
		//System.out.println("RobotListManagingSystem says: Planning...");
		
		// Plan how to manage exceptional active robot lists from Masters
	}
	
	@Override
	public void execute(){
		// RobotListManagingSystem MAPE
		// @E
		//System.out.println("RobotListManagingSystem says: Execute...");
		
		// Manage exceptional active robot lists from Masters
	}
}
