package unibg.saoms.msg;

import java.io.Serializable;

/**
 * This is the message to manage new events (luggage to bring from a place to another)
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class ELMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	// Robot ID (0 if request from Master)
	public int robID;
	// Message ID
	public int lugID;
	// Destination 1
	public int x1;
	public int y1;
	// Destination 2
	public int x2;
	public int y2;
	
	//Constructors
	public ELMessage(int rID, int lID, int dx1, int dy1, int dx2, int dy2){
		this.robID=rID;
		this.lugID=lID;
		this.x1=dx1;
		this.y1=dy1;
		this.x2=dx2;
		this.y2=dy2;
	}

	public ELMessage() {
	}

	@Override
	public String toString(){
		if(this.robID == 0){
			return "Luggage to bring from ("+this.x1+","+this.y1+") to ("+this.x2+","+this.y2+")";
		}
		else{
			return "Robot"+this.robID+" is bringing luggage from ("+this.x1+","+this.y1+") to ("+this.x2+","+this.y2+")";
		}
	}
	
	public boolean equals(ELMessage e){
		if(this.robID == e.robID && this.lugID == e.lugID && this.x1 == e.x1 && this.y1 == e.y1 && this.x2 == e.x2 && this.y2 == e.y2){
			return true;
		}
		else{
			return false;
		}
	}
}
