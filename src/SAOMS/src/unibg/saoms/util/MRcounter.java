package unibg.saoms.util;

/**
 * This class is used to create a list of operating robots associated with a counter.
 *  
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class MRcounter{
	
	private int count;
	private int numOfRobots;
	
	// Constructor----------------------------------------------------------
	/**
	 * Constructor method for MRcounter
	 */
	public MRcounter(int c, int n){
		this.setCount(c);
		this.setNumOfRobots(n);
	}

	// Getters and Setters---------------------------------------------------
	public int getNumOfRobots() {
		return numOfRobots;
	}

	public void setNumOfRobots(int numOfRobots) {
		this.numOfRobots = numOfRobots;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}