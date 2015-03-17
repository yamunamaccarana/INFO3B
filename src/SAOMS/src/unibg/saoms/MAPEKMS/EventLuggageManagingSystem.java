package unibg.saoms.MAPEKMS;

import unibg.robotics.tca.TopicNotFoundException;
import unibg.saoms.MAPEKclass;
import unibg.saoms.msg.ELMessage;

/**
 * This is the class used to manage luggage transfers.
 * It is a MAPE-K class
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class EventLuggageManagingSystem extends MAPEKclass {

	// Fields----------------------------------------------------------------
	private boolean oldTransp;
	private boolean getReadyToGo;
	private boolean lugListEmpty;
	
	// MAPE------------------------------------------------------------------
	@Override
	public void monitor(){
		// EventLuggageManagingSystem MAPE
		// @M
		//System.out.println(this.getRob().toString()+ ":EventLuggageManagingSystem says: Monitoring...");
		
		// Monitoring of luggage request list
		lugListEmpty=getRob().getLuggageRequestList().isEmpty();
	}

	@Override
	public void analyze() {
		// EventLuggageManagingSystem MAPE
		// @A
		//System.out.println(this.getRob().toString()+ ":EventLuggageManagingSystem says: Analyzing...");
		
		// Analyze if the robot is already a transporter
		oldTransp=getRob().isTransporter();
	}

	@Override
	public void plan() {
		// EventLuggageManagingSystem MAPE
		// @P
		//System.out.println(this.getRob().toString()+ ":EventLuggageManagingSystem says: Planning...");
		
		getReadyToGo=false;
		if(!lugListEmpty){
			// If not a transporter: take interest in the first luggage in the luggage list
			if(!oldTransp){
				getRob().setLuggageMessage(getRob().getLuggageRequestList().get(0));
			}
		}
		// If already transporter: get ready to check conflicts and go for the luggage
		if(oldTransp){
			getReadyToGo=true;
		}
	}



	@Override
	public void execute() {
		// EventLuggageManagingSystem MAPE
		// @E
		//System.out.println(this.getRob().toString()+ ":EventLuggageManagingSystem says: Execute...");
		
		if(!lugListEmpty){
			// If new transporter
			if(!oldTransp){
				// Remove request from luggage request list
				getRob().getLuggageRequestList().remove(getRob().getLuggageMessage());
				// Prepare the message to be sent to others
				ELMessage t=getRob().getLuggageMessage();
				t.robID=getRob().getRobotID();
				// Send a message to say this robot will collect the selected luggage
				try {
					System.out.println(t.toString());
					this.getRob().send(this.getRob().getELMSTopic(), t);				
				} catch (TopicNotFoundException e) {e.printStackTrace();}
				// Now the robot is a transporter
				getRob().setTransporter(true);
			}
		}
		// If already a transporter check conflicts and go to collect the luggage
		if(getReadyToGo){
			getRob().checkLuggageConflictsAndGo();
		}
	}
}