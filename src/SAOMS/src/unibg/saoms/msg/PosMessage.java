package unibg.saoms.msg;

import java.io.Serializable;

/**
 * This is the message containing positions
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class PosMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Robot
	public int robID;
	// Robot's next position
	public int x;
	public int y;

	// Constructor
	public PosMessage(int r, int nx, int ny){
		this.robID=r;
		this.x=nx;
		this.y=ny;
	}
	
	@Override
	public String toString(){
		return "Position of Robot"+this.robID+" will be: ("+this.x+","+this.y+")";
	}
	
	public boolean equals(PosMessage p){
		if(robID == p.robID && x == p.x && y == p.y){
			return true;
		}
		else{
			return false;
		}
	}
}
