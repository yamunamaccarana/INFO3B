package unibg.saoms.msg;

import java.io.Serializable;

/**
 * This is the message to manage new events (luggage to bring from a place to another)
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class EEMessage implements Serializable {

	private static final long serialVersionUID = 7818595914681924963L;
	
	// Event
	public Event event;
	// Expected luggage
	public int expLug;
	
	//Constructor
	public EEMessage(Event e, int lug){
		this.event=e;
		this.expLug=lug;
	}
	
	public EEMessage() {
		this.event=null;
		this.expLug=0;
	}

	@Override
	public String toString(){
		return "New event: "+event+" with "+expLug+" expected pieces of luggage.";
	}
	
	public enum Event{
		LANDING,
		TAKEOFF
	}
}
