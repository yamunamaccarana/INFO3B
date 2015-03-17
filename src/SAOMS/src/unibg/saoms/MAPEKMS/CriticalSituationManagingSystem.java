package unibg.saoms.MAPEKMS;

import unibg.robotics.tca.TopicNotFoundException;
import unibg.saoms.MAPEKclass;
import unibg.saoms.exception.CriticalSituationException;
import unibg.saoms.msg.CSMessage;
import unibg.saoms.msg.RLMessage;

/**
 * This is the class used to manage critical situation. 
 * It is a MAPE-K class
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class CriticalSituationManagingSystem extends MAPEKclass{
	
	// Fields----------------------------------------------------------------
	private boolean critic;
	private int exec;
	private boolean shutDown;
	private boolean sendforhelp;

	
	// MAPE------------------------------------------------------------------
	// Possible planning after analyzing:
//	Plan:
//	if(critical situation){ // Robot cannot move
//		if(robot hasLuggage()) : execution 1
//		if(!robot hasLuggage() && robot isBusy() && !robot isInCharge()) : execution 2
//		if(!robot hasLuggage() && !robot isBusy()) : execution 3
//	}
//	if(!critical situation && low battery level) : execution 4
	
	// Possible executions:
//	Execution 1:
//		callForHelp();
//	Execution 2:
//		callForFinishPath();
//	Execution 3:
//		callSubstitute();
//	Execution 4:
//		setReadyToGoUnderCharge(true);
//	Execution 5:
//		collaborateWithCSMS();


	@Override
	public void monitor(){
		// CriticalSituationManagingSystem MAPE
		// @M
		//System.out.println("CriticalSituationManagingSystem says: Monitoring...");
				
		// Monitor of critical situation variable
	}

	@Override
	public void analyze() {
		// CriticalSituationManagingSystem MAPE
		// @A
		//System.out.println("CriticalSituationManagingSystem says: Analyzing...");
		
		// Analyzing critical situation variable
		critic=getRob().isCritical();
	}

	@Override
	public void plan() {
		// CriticalSituationManagingSystem MAPE
		// @P
		//System.out.println("CriticalSituationManagingSystem says: Planning...");
		shutDown=false;
		sendforhelp=false;
		// If critic select specific execution
		if(critic){
			if(getRob().hasLuggage()){
				exec=1;
			}
			if(!getRob().hasLuggage() && getRob().isBusy() && !getRob().isInCharge()){
				exec=2;
			}
			if(!getRob().hasLuggage() && !getRob().isBusy()){
				exec=3;
			}
			// If this robot was a healer set send for help true
			if(getRob().isHealer() && getRob().getDest1().x == 0 && getRob().getDest1().y == 0){
				sendforhelp=true;
			}
			shutDown=true;
		}
		else{
			if((getRob().getBatteryLev() <= 35 && !getRob().getPos().equals(getRob().getChargePos())) && !getRob().isHealer()){
				exec=4;
			}
			else{
				exec=5;
			}
		}
	}

	@Override
	public void execute() {
		// CriticalSituationManagingSystem MAPE
		// @E
		//System.out.println("CriticalSituationManagingSystem says: Execute...");
		
		if(this.exec==1 || this.exec==2 || this.exec==3){
			// Send a message to ask for help
			try{
				getRob().send(getRob().getCSMSTopic(),new CSMessage(getRob().getRobotID(),
																getRob().getPos().x, getRob().getPos().y,
																getRob().getDest1().x, getRob().getDest1().y,
																getRob().getDest2().x, getRob().getDest2().y, 0, exec));
			}catch(TopicNotFoundException e){e.printStackTrace();}
			// If send for help: send a help message for the robot it was healing
			if(sendforhelp){
				try{
					getRob().send(getRob().getCSMSTopic(), getRob().getMsgHelp());
				}catch(TopicNotFoundException e){e.printStackTrace();}
			}
			// Ask for human maintenance
			try{
				throw new CriticalSituationException();
			}catch(CriticalSituationException e){e.printStackTrace();}
		}
		if(exec==4){
			getRob().setReadyToGoUnderCharge(true);
		}
		
		// If robot shut down is true
		if(shutDown){
			// Send a message to say the robot will not be active anymore
			try{
				this.getRob().send(this.getRob().getRMSTopic(),new RLMessage(getRob().getRobotID(), false, 0, false));
			}catch(TopicNotFoundException e){e.printStackTrace();}

			// Shut down Robot
			getRob().unscheduleTask();
		}

		if(exec==5){
			// Help critical situations, if present and not busy or already a healer
			if((!getRob().getHelpReqList().isEmpty() && !getRob().isBusy() && !getRob().isReadyToGoUnderCharge()) || getRob().isHealer()){
				getRob().collaborateWithCSMS();
			}
		}
	}
}
