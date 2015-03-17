package unibg.saoms.msg;

import java.io.Serializable;

/**
 * This is the message containing robots to asleep/awake
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class RMessage implements Serializable {

	private static final long serialVersionUID = 8570162909391618438L;
	
	// Robot ID
	public int robID;
	// Robot's status
	public boolean status;

	// Constructor
	public RMessage(int idnum, boolean s){
		this.robID=idnum;
		this.status=s;
	}
	
	@Override
	public String toString(){
		if(this.status){
			return "The robot with number ID: "+robID+" needs to awake";
		}
		else{
			return "The robot with number ID: "+robID+" needs to asleep";
		}
	}
	
	public boolean equals(RMessage r){
		if((robID == r.robID) && ((status && r.status) || (!status && !r.status))){
			return true;
		}
		else{
			return false;
		}
	}
}
