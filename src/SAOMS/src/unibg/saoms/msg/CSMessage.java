package unibg.saoms.msg;

import java.io.Serializable;

/**
 * This is the message containing ID and IDnumber of active robots
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class CSMessage implements Serializable {

	private static final long serialVersionUID = 7818595914681924963L;
	
	// Critical situation robotID
	public int crobID;
	// Position of the robot in critical situation
	public int x;
	public int y;
	// First destination of the robot in critical situation
	public int x1;
	public int y1;
	// Second destination of the robot in critical situation
	public int x2;
	public int y2;
	// Healer robotID (0 if help request)
	public int hrobID;
	// Execution needed
	public int exec;

	// Constructor
	public CSMessage(int cid, int nx, int ny, int nx1, int ny1, int nx2, int ny2, int hid, int e){
		this.crobID=cid;
		this.x=nx;
		this.y=ny;
		this.x1=nx1;
		this.y1=ny1;
		this.x2=nx2;
		this.y2=ny2;
		this.hrobID=hid;
		this.exec=e;
	}
	
	public CSMessage() {
	}

	@Override
	public String toString(){
		if(hrobID==0){
			return "New help request from Robot"+crobID;
		}
		else{
			return "Robot"+hrobID+" is a new healer for Robot"+crobID;
		}
	}
	
	public boolean equals(CSMessage c){
		if((crobID==c.crobID) && (x==c.x) && (y==c.y) && (x1==c.x1) && (y1==c.y1) && (x2==c.x2) && (y2==c.y2) && (hrobID==c.hrobID) && (exec==c.exec)){
			return true;
		}
		else{
			return false;
		}
	}
}
