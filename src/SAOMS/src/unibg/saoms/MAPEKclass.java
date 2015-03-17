package unibg.saoms;

/**
 * This is the MAPE-K loop managing class.
 * It is a MAPE-K class.
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public abstract class MAPEKclass {
	
	// Fields----------------------------------------------------------------
	// Robot to work on
	protected RobotImpl rob;

	// MAPE------------------------------------------------------------------
	/**
	 * Method to call a MAPE loop
	 */
	public void callMAPE(RobotImpl r){
		this.setRob(r);
		monitor();
		analyze();
		plan();
		execute();
	}
	
	/**
	 * Method to call a MAPE loop.
	 * This method is intended to override.
	 */
	public void callMAPE(Object obj){
		//..use Object somehow
		monitor();
		analyze();
		plan();
		execute();
	}
	
	/**
	 * Method to call a MAPE loop.
	 * 
	 */
	public void callMAPE(){
		monitor();
		analyze();
		plan();
		execute();
	}

	/**
	 * Method to monitor the system.
	 * This method is intended to override.
	 */
	public void monitor() {
		//..do some computation to monitor system
	}

	/**
	 * Method to analyze the system.
	 * This method is intended to override.
	 */
	public void analyze() {
		//..do some computation to analyze system		
	}

	/**
	 * Method to plan actions.
	 * This method is intended to override.
	 */
	public void plan() {
		//..do some computation to plan actions
	}

	/**
	 * Method to execute actions.
	 * This method is intended to override.
	 */
	public void execute() {
		//..do some computation to execute actions		
	}

	//Getters and Setters--------------------------------------------------------------------------
	/**
	 * Method to get the RobotImpl variable
	 * @return returns the rob variable
	 */
	protected RobotImpl getRob() {
		return this.rob;	
	}
	
	/**
	 * Method to set the robot variable
	 */
	protected void setRob(RobotImpl r) {
		this.rob=r;	
	}
}
