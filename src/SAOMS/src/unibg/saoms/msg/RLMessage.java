package unibg.saoms.msg;

import java.io.Serializable;

/**
 * This is the message is used to say if a robot is active or not
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class RLMessage implements Serializable {

	private static final long serialVersionUID = 7818595914681924963L;
	
	// Robot ID
	public int robID;
	// true if robot is active
	public boolean active;
	// Battery level
	public double battery;
	// true if robot is in charge
	public boolean inCh;
	
	// Constructor
	public RLMessage(int id, boolean active, double batteryLev, boolean inCharge){
		this.robID=id;
		this.active=active;
		this.battery=batteryLev;
		this.inCh=inCharge;
	}
	
	@Override
	public String toString(){
		if(this.active){
			return "Robot"+robID+" is active";
		}
		else{
			return "Robot"+robID+" will be not active";
		}
	}
	
	public boolean equals(RLMessage r){
		if((robID == r.robID) && ((active && r.active) || (!active && !r.active))){
			return true;
		}
		else{
			return false;
		}
	}
}
