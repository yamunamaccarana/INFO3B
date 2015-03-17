package unibg.saoms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import unibg.robotics.tca.Message;
import unibg.robotics.tca.Task;
import unibg.robotics.tca.TaskInterface;
import unibg.robotics.tca.TopicNotFoundException;
import unibg.robotics.tca.TopicObject;
import unibg.saoms.MAPEKMS.CollisionManagingSystem;
import unibg.saoms.MAPEKMS.CriticalSituationManagingSystem;
import unibg.saoms.MAPEKMS.EventLuggageManagingSystem;
import unibg.saoms.MAPEKMS.PathFollowerManagingSystem;
import unibg.saoms.MAPEKMS.RobotManagingSystem;
import unibg.saoms.MAPEKMS.RobotsListManagingSystem;
import unibg.saoms.msg.CSMessage;
import unibg.saoms.msg.ELMessage;
import unibg.saoms.msg.PosMessage;
import unibg.saoms.msg.RLMessage;
import unibg.saoms.msg.RMessage;
import unibg.saoms.util.BlockPos;

/**
 * 
 * This is the main class of the project, which represents a Robot component
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public abstract class RobotImpl extends Task implements TaskInterface{
	
	// Fields----------------------------------------------------------------
	// Robot ID
	protected String robotName;
	protected int robotID;
	// Robot position
	protected BlockPos mypos;
	// Robot next position
	protected BlockPos nextpos = new BlockPos(0, 0);
	// Robot charge position
	protected BlockPos chargepos;
	// Robot first destination
	protected BlockPos dest1 = new BlockPos(0, 0);
	// Robot second destination
	protected BlockPos dest2 = new BlockPos(0, 0);
	// Battery level
	protected double batteryLev;
	// Battery parameter: how much robot discharge each execution
	protected int batteryDischarge;
	// True if robot is in charge
	protected boolean inCharge = true;
	// Execution number
	int it = 0;

	// Personal topic
	protected TopicObject positionTopic = new TopicObject("POSTOPIC");

	// MAPE and MAPEtopics--------------------------------------------------
	// RMS-------------------------------------------------------
	private RobotManagingSystem RMS = new RobotManagingSystem();
	// System topic to know which robot needs to activate/deactivate
	public TopicObject RMSTopic = new TopicObject("RMSTOPIC");
	// Boolean variable to know if the robot has to go under charge
	protected boolean readyToGoUnderCharge = false;
	
	// RLMS----------------------------------------------------
	private RobotsListManagingSystem RLMS = new RobotsListManagingSystem();
	// RLMS topic to send/receive active robots
	protected TopicObject RLMSTopic = new TopicObject("RLMSTOPIC");
	// Active Robot List
	protected ArrayList<RLMessage> activeRobList = new ArrayList<RLMessage>();

	// CSMS----------------------------------------------------
	private CriticalSituationManagingSystem CSMS = new CriticalSituationManagingSystem();
	// True if critical situation
	protected boolean critical = false;
	// CMSM topic to send/receive help requests and heal response
	private TopicObject CSMSTopic = new TopicObject("CSMSTOPIC");
	// True if the robot is ready to heal other robots
	protected boolean healer = false;
	// Message containing the actual help request of the healer
	protected CSMessage msgHelp = new CSMessage();
	// Help requests list
	protected ArrayList<CSMessage> helpReqList = new ArrayList<CSMessage>();
	// Heal conflict list
	protected ArrayList<CSMessage> healConflictList = new ArrayList<CSMessage>();

	// ELMS-------------------------------------------------------
	private EventLuggageManagingSystem ELMS = new EventLuggageManagingSystem();
	// ELSM topic to receive new luggage managing request
	protected TopicObject ELMSTopic = new TopicObject("ELMSTOPIC");
	// True if robot is working
	protected boolean busy = false;
	// True if robot is a transporter
	protected boolean transporter = false;
	// True if robot has a luggage
	protected boolean hasLuggage = false;
	// Luggage requests list
	protected ArrayList<ELMessage> luggageRequestList = new ArrayList<ELMessage>();
	// Luggage conflict list
	protected ArrayList<ELMessage> luggageConflictList = new ArrayList<ELMessage>();
	// Luggage the robot is managing
	protected ELMessage luggageMessage = new ELMessage();

	// CMS-----------------------------------------------------
	private CollisionManagingSystem CMS = new CollisionManagingSystem();
	// List of other robots' positions
	protected ArrayList<PosMessage> otherRobotPositionList = new ArrayList<PosMessage>();
	// Boolean variable to avoid collision
	protected boolean stopAPeriod = false;

	// PFMS-----------------------------------------------------
	private PathFollowerManagingSystem PFMS = new PathFollowerManagingSystem();
	// Boolean variable for working on luggage
	protected boolean workingOnLug = false;
	// Boolean variable for working on luggage
	protected boolean reachingCharge = false;
	
	// Constructors---------------------------------------------------------
	/**
	 * Constructor method for Robot (empty)
	 */
	public RobotImpl(){
	}

	// Initialize and Execute----------------------------------------------
	/**
	 * This method runs only one time before the first execution.
	 * Can be overridden by the user.
	 */
	@Override
	public void initialize(){
		
		// Initialize, subscribing and publishing on topics
		System.out.println("Initialization of "+this.robotName);
				
		// RMS Topic------------------------------------------------
		// Subscribe to the ActiveRobot topic
		subscribe(this.RMSTopic);
		// Publish on the ActiveRobot topic
		publish(this.RMSTopic);
		
		// RLMS Topic-----------------------------------------------
		// Subscribe to RobotListManagingSystem topic
		subscribe(this.RLMSTopic);
		// Publish on RobotListManagingSystem topic
		publish(this.RLMSTopic);
		
		// CSMS Topic----------------------------------------------
		// Subscribe to CriticalSituationManagingSystem topic
		subscribe(this.CSMSTopic);
		// Publish on CriticalSituationManagingSystem topic
		publish(this.CSMSTopic);
		
		// ELMS Topic----------------------------------------------
		// Subscribe to EventLuggageManagingSystem topic
		subscribe(this.ELMSTopic);
		// Publish on EventLuggageManagingSystem topic
		publish(this.ELMSTopic);
		
		// CMS Topic-----------------------------------------------
		subscribe(this.positionTopic);
		
		// Position Topic----------------------------------------------
		// Publish on Position topic
		publish(this.positionTopic);
	}
	
	/**
	 * This method contains the instructions run by the Task at each execution.
	 * Can be overridden by the user.
	 */
	@Override
	public void execute(){
		// Periodic execution of the robot
		java.util.Date date= new java.util.Date();
		System.out.println("Execution "+it+" of "+robotName+" starts at "+new Timestamp(date.getTime()));

		// Receive messages and update lists
		updateLists();
//		System.out.println("LIST "+it+" of "+robotName+" says: Updated... ");

		// RMS MAPE----------------------------------------------------------------------
		// Check if this robot needs to asleep/awake
		RMS.callMAPE(this);
//		System.out.println("RMS  "+it+" of "+robotName+" says: Done... ");
		
		// If not asleep
		if(!inCharge){
			// If not in charge position, simulate battery discharge
			if(!mypos.equals(chargepos)){
				lowerBattery();
			}
		}
		// If in charge: recharge battery
		else{
			rechargeBattery();
		}
					
		// Send a message to say that this robot is active
		if(!critical){
			try {
				send(RLMSTopic, new RLMessage(robotID, true, batteryLev, inCharge));
			} catch (TopicNotFoundException e) {e.printStackTrace();}
		}

		// RLMS MAPE---------------------------------------------------------------------
		// Check if there are new active robots
		this.RLMS.callMAPE(this);
//		System.out.println("RLMS "+this.it+" of "+robotName+" says: Done...");
				
		// CSMS MAPE---------------------------------------------------------------------
		// Check critical situations and collaborate with CSMS
		CSMS.callMAPE(this);
//		System.out.println("CSMS "+this.it+" of "+robotName+" says: Done...");
		
		// ELMS MAPE---------------------------------------------------------------------
		// If not busy, not ready to go under charge and luggage list is not empty: collaborate with ELMS
		if(!busy && !readyToGoUnderCharge && !inCharge && !healer){
			ELMS.callMAPE(this);
//			System.out.println("ELMS "+this.it+" of "+robotName+" says: Done...");
		}

			
		// CMS MAPE----------------------------------------------------------------------
		// If busy: check conflicts while moving
		if(busy){
			CMS.callMAPE(this);
//			System.out.println("CMS  "+this.it+" of "+robotName+" says: Done...");
		}
			
		// PFMS MAPE----------------------------------------------------------------------
		// If no risk of collision: follow the path
		if(!stopAPeriod){
			PFMS.callMAPE(this);
		}
//		System.out.println("PFMS "+this.it+" of "+robotName+" says: Done...");
				
		// If not in charge and busy send next position to position topic
		if(!inCharge && (busy || reachingCharge || readyToGoUnderCharge) &&!nextpos.equals(chargepos)){
			try{
				send(this.getPosTopic(), new PosMessage(robotID, nextpos.x, nextpos.y));
			}catch (TopicNotFoundException e){e.printStackTrace();}
		}
//		System.out.println("Execution "+it+" of "+robotName+" ends at "+new Timestamp(date.getTime()));
		it++;
		
		// Print variables---------------------------------------------------------------
//		System.out.println(robotName+ " luggage message: "+luggageMessage);
//		System.out.println(robotName+ " is busy? "+busy);
//		System.out.println(robotName+ " is a transporter? "+transporter);
//		System.out.println(robotName+ " is a healer? "+healer);
//		System.out.println(robotName+ " is inCharge? "+inCharge);
//		System.out.println(robotName+ " is readyToGoUnderCharge? "+readyToGoUnderCharge);
//		System.out.println(robotName+ " is reachingCharge? "+reachingCharge);
//		System.out.println(robotName+ " is in a critical situation? "+critical);
		if(batteryLev > 10){
			System.out.println(robotName+ " battery level: "+batteryLev);
		}
		else{
			if(!mypos.equals(chargepos)){
				System.out.println(robotName+ " needs to reach charge position as soon as possible!");
			}
		}
	}

	// Methods------------------------------------------------------------------
	/**
	 * Method to simulate the battery discharge
	 */
	public void lowerBattery(){
		if((batteryLev - batteryDischarge) >= 0){
		batteryLev -= batteryDischarge;
		}
		else{
			batteryLev = 0;
		}
	}	
	
	/**
	 * Method to update lists. It has to be call once each period.
	 */
	private void updateLists() {
		// Update active robots list
		updateActiveRobotList();
//		System.out.println("LIST activeRobot "+it+" of "+robotName+" says: Updated... ");
		// Update other robots' position list
		updateOtherRobotPositionList();
//		System.out.println("LIST OtherRobotPosition "+it+" of "+robotName+" says: Updated... ");
		// Update help request list
		updateHelpHealReqRespLists();
//		System.out.println("LIST HelpReq "+it+" of "+robotName+" says: Updated... ");
		// Update luggage list
		updateLuggageLists();
//		System.out.println("LIST luggage "+it+" of "+robotName+" says: Updated... ");
	}
	
	/**
	 * Method to update active robots list. It has to be call once each period.
	 */
	private void updateActiveRobotList() {
		// Receive new active robot messages
		Message msg;
		RLMessage rlmsg;		
		Iterator<Message> iterator=null;
		try{
			iterator = receive(RLMSTopic);
		}catch(TopicNotFoundException e){e.printStackTrace();}

		// Update list
		while(iterator.hasNext()){
			msg=iterator.next();
			rlmsg=(RLMessage)msg.getMessage();
//			System.out.println(robotName+" - Message from topic: "+msg.getTopic().getTopicName()+": \""+rlmsg.toString()+"\"");
			ArrayList<RLMessage> temp = new ArrayList<RLMessage>();
			// For each id already in the list: remove old message
			for(RLMessage i : activeRobList){
				if(i.robID == rlmsg.robID){
					temp.add(i);
				}
			}
			for(RLMessage t : temp){
				activeRobList.remove(t);
			}
			// Add new message, if not of this robot
			if(rlmsg.robID != robotID){
				activeRobList.add(rlmsg);
			}
		}
		System.out.println(robotName+" updated active robot list:		"+activeRobList);
	}

	/**
	 * Method to update other robots' position list. It has to be call once each period.
	 */
	private void updateOtherRobotPositionList() {
		// Receive new PosMessages
		Message msg;
		PosMessage posmsg;		
		Iterator<Message> iterator=null;
		try{
			iterator = receive(positionTopic);
		}catch(TopicNotFoundException e){e.printStackTrace();}

		// Update list
		while(iterator.hasNext()){
			msg=iterator.next();
			posmsg=(PosMessage)msg.getMessage();
//			System.out.println(robotName+" - Message from topic: "+msg.getTopic().getTopicName()+": \""+posmsg.toString()+"\"");
			ArrayList<PosMessage> temp = new ArrayList<PosMessage>();
			// For each position already in the list: remove old position of this robot, if present
			for(PosMessage p : otherRobotPositionList){
				if(p.robID == posmsg.robID){
					temp.add(p);
				}
			}
			for(PosMessage p : temp){
				otherRobotPositionList.remove(p);
			}
			// Add new position, if not of this robot
			if(posmsg.robID != robotID){
				otherRobotPositionList.add(posmsg);
				System.out.println(robotName+" updated other robot position list:	"+otherRobotPositionList);
			}
		}
	}
	
	/**
	 * Method to update help requests and heal responses lists. It has to be call once each period.
	 */
	private void updateHelpHealReqRespLists() {
		// Receive new RMessages
		Message msg;
		CSMessage csmsg;		
		Iterator<Message> iterator=null;
		try{
			iterator = receive(CSMSTopic);
		}catch(TopicNotFoundException e){e.printStackTrace();}

		// Heal responses list
		ArrayList<CSMessage> healResponseList = new ArrayList<CSMessage>();		
		// Add new messages to help request list
		while(iterator.hasNext()){
			msg=iterator.next();
			csmsg=(CSMessage)msg.getMessage();
//			System.out.println(robotName+" - Message from topic: "+msg.getTopic().getTopicName()+": \""+csmsg.toString()+"\"");
			// If help request (CSMessage.hrobID == 0) then add the message to help request list
			if(csmsg.hrobID == 0){
				helpReqList.add(csmsg);
			}
			// Else add the message to heal response list (unless it is my message: otherwise ignore it)
			else{
				if(csmsg.hrobID != robotID){
					healResponseList.add(csmsg);
				}
			}
		}

		// Managing responses
		ArrayList<CSMessage> temp = new ArrayList<CSMessage>();
		// For each response...
		for(CSMessage resp : healResponseList){
			// ...if another robot is healing my same robot
			if(resp.crobID == msgHelp.crobID){
				// Move message in the heal conflicts list
				healConflictList.add(resp);
			}
			else{
				// ...for each request...
				for(CSMessage req : helpReqList){
					// ...if request has a response
					if(req.crobID == resp.crobID){
						temp.add(req);
					}
				}
			}
		}
		// Remove request from luggage request list
		if(!temp.isEmpty()){
			for(CSMessage req : temp){
				helpReqList.remove(req);
			}
		}
		System.out.println(robotName+" help request list: 			"+helpReqList);
	}

	/**
	 * Method to update luggage lists. It has to be call once each period.
	 */
	private void updateLuggageLists() {		
		// Receive new ELMessages
		Message msg;
		ELMessage elmsg;		
		Iterator<Message> iterator=null;
		try{
			iterator = receive(ELMSTopic);
		}catch(TopicNotFoundException e){e.printStackTrace();}
		
		// Luggage responses list
		ArrayList<ELMessage> luggageResponseList = new ArrayList<ELMessage>();		
		// Add new messages to luggage list
		while(iterator.hasNext()){
			msg=iterator.next();
			elmsg=(ELMessage)msg.getMessage();
//			System.out.println(robotName+" - Message from topic: "+msg.getTopic().getTopicName()+": \""+elmsg.toString()+"\"");
			// If Master request (ELMessage.robID == 0) then add the message to luggage request list
			if(elmsg.robID == 0){
				luggageRequestList.add(elmsg);
			}
			// Else add the message to luggage response list (unless it is my message: otherwise ignore it)
			else{
				if(elmsg.robID != robotID){
					luggageResponseList.add(elmsg);
				}
			}
		}

		// Managing responses
		ArrayList<ELMessage> temp = new ArrayList<ELMessage>();
		// For each response...
		for(ELMessage resp : luggageResponseList){
			// ...if another robot is working on my same luggage
			if(resp.lugID == luggageMessage.lugID){
				// Move message in the luggage conflicts list
				luggageConflictList.add(resp);
			}
			else{
				// ...for each request...
				for(ELMessage req : luggageRequestList){
					// ...if request has a response
					if(req.lugID == resp.lugID){
						// Remove request from luggage request list
						temp.add(req);
					}
				}
			}
		}
		for(ELMessage req : temp){
			luggageRequestList.remove(req);
		}
		System.out.println(robotName+" luggage request list: 			"+luggageRequestList);
	}
	
	/**
	 * Method check conflicts on luggage and eventually go and complete a task.
	 * Priority: distance to destination, minor ID number.
	 */
	public void checkLuggageConflictsAndGo() {
		ArrayList<PosMessage> oth = new ArrayList<PosMessage>();
		// For each conflict...		
		for(ELMessage e : luggageConflictList){
			// ...find position of the other robots
			for(PosMessage p : otherRobotPositionList){
				if(e.robID == p.robID){
					oth.add(p);
				}
			}
		}
		// Check if this is the closest
		BlockPos dest = new BlockPos(luggageMessage.x1, luggageMessage.y1);
		boolean closest = amITheClosest(oth, dest);
		// If this robot is the closest: go to collect luggage
		if(closest){
			dest1 = dest;
			dest2 = new BlockPos(luggageMessage.x2, luggageMessage.y2);
			busy=true;
		}
		else{
			transporter=false;
		}
	}
	
	/**
	 * Method to find out who is the closest robot to a given destination
	 */
	public boolean amITheClosest(ArrayList<PosMessage> others, BlockPos destination) {
		if(others.isEmpty() || destination.equals(mypos)){
			return true;
		}
		// Using Pythagorean theorem, calculate the distance to the destination
		// Calculus of my distance
		double distX = getPos().x - destination.x;
		double distY = getPos().y - destination.y;
		double mydistance = Math.hypot(distX, distY);
		// Variables for each other robot
		double otdistance, otX, otY;
		boolean sameDist=false;
		ArrayList<PosMessage> sameDRob = new ArrayList<PosMessage>();
		// For each other robot position
		for(PosMessage o : others){			
			otX = o.x - destination.x ;
			otY = o.y - destination.y;
			otdistance = Math.hypot(otX, otY);
			// If my distance is bigger than any other distance, return false
			if (mydistance > otdistance){
				return false;
			}
			// If there is another robot with the same distance
			if (mydistance == otdistance){
				sameDist=true;
				sameDRob.add(o);
			}
		}
		// If sameDist=true (and did not return), there are two (or more) robots at the same minimum distance
		if(sameDist){
			// For each other robot at the same distance
			for(PosMessage o : sameDRob){
				// If my ID is major than someone else's one: do not go
				if(o.robID < robotID){
					return false;
				}
			}
		}
		// Else: go
		return true;
	}
	

	/**
	 * Method to collaborate with the CriticalSituationMS to heal other robots.
	 */
	public void collaborateWithCSMS() {
		boolean h = healer;
		// If available (not busy and not in a critical situation), get ready to help robot in critical situation
		if(!h){
			// Set message help
			if(!helpReqList.isEmpty()){
				msgHelp = helpReqList.get(0);
				// Remove request from help request list
				helpReqList.remove(msgHelp);
			}
			// Prepare the message to be sent to others
			CSMessage t = msgHelp;
			t.hrobID = robotID;
			// Send a message to say this robot will help the robot in a critical situation
			try {
				System.out.println(t.toString());
				send(CSMSTopic, t);				
			} catch (TopicNotFoundException e) {e.printStackTrace();}
			// Now the robot is a healer
			healer = true;
			busy = true;
		}
		// If robot is already a healer, check conflicts and go
		else{
			checkHealConflictsAndGoHeal();
		}
	}
	
	/**
	 * Method check conflicts on heal responses and eventually go and complete to help.
	 * Priority: distance to destination, minor ID number.
	 * Eventually awake another robot.
	 */
	private void checkHealConflictsAndGoHeal(){
		// Anyway, this robot is not a healer anymore
		healer=false;
		ArrayList<PosMessage> oth = new ArrayList<PosMessage>();
		// For each conflict...		
		for(CSMessage c : healConflictList){
			// ...find position of the other healing robots
			for(PosMessage p : otherRobotPositionList){
				if(c.hrobID == p.robID){
					oth.add(p);
				}
			}
		}
		// Check if this robot is the closest
		BlockPos cRobPos = new BlockPos(msgHelp.x, msgHelp.y);
		boolean closest = amITheClosest(oth, cRobPos);
		// If this is the closest: go to heal and awake another robot
		if(closest){
			busy=true;
			// If this robot is not in charge, awake another robot, else awake itself
			if(!inCharge){
				System.out.println(robotName+" is a healer and awakes another robot");
				awakeRobot();
			}
			else{
				System.out.println(robotName+" is a healer and now is awake");
				inCharge=false;
			}
			goToHeal();
		}
	}

	/**
	 * Method to make the healer understand the problem of the critical robot.
	 */
	private void goToHeal(){
		switch(msgHelp.exec){
		case 1:
			callForGetLuggage();
			break;
		case 2:
			callForFinishPath();
			break;
		case 3:
			callSubstitute();
			break;
		default:		
		}
		return;
	}

	/**
	 * Method to help a robot that call for a substitute:
	 * need to collect luggage from it and complete its path.
	 */
	public void callForGetLuggage(){
		// Get its position and it second destination:
		dest1 = new BlockPos(msgHelp.x, msgHelp.y);
		dest2 = new BlockPos(msgHelp.x2, msgHelp.y2);
	}
	
	/**
	 * Method to help a robot that call for a substitute:
	 * no need to collect luggage from it, but need to complete its path.
	 */
	public void callForFinishPath(){
		// Get its destinations:
		dest1 = new BlockPos(msgHelp.x1, msgHelp.y1);
		dest2 = new BlockPos(msgHelp.x2, msgHelp.y2);
	}

	/**
	 * Method to help a robot that call for a substitute:
	 * no need to collect luggage from it, or to complete its path.
	 */
	public void callSubstitute(){
		// Get its destinations: null
		busy = false;
	}

	/**
	 * Method to awake a robot.
	 * Priority: battery level, minor ID number.
	 */
	public void awakeRobot() {
		boolean go=true;
		int IDnumToGo = 0;
		ArrayList<RLMessage> charged = new ArrayList<RLMessage>();
		boolean maxCharged;
		// For each robot in the active list...
		for(RLMessage actR1 : activeRobList){
			maxCharged=true;
			//... that is in charge
			if(actR1.inCh){
				// For each other robot in the list...
				for(RLMessage actR2 : activeRobList){
					//... that is in charge (and not itself)
					if(actR1.robID != actR2.robID && actR2.inCh){
						// If its battery level is minor than others, it is not the maxCharged
						if(actR1.battery < actR2.battery){
							maxCharged=false;
						}
					}
				}
				if(maxCharged){
					charged.add(actR1);
				}
			}
		}
		// If more than one robot are recharged, call the minor ID one
		if(charged.size() != 1){
			for(RLMessage r1 : charged){
				go = true;
				for(RLMessage r2 : charged){
					if(r1.robID > r2.robID){
						go=false;
					}
				}
				if(go){
					IDnumToGo = r1.robID;
				}
			}
		}
		// Send a message to awake the selected Robot
		try {
			send(RMSTopic,new RMessage(IDnumToGo, true));
		}catch(TopicNotFoundException e){e.printStackTrace();}
	}
	
	/**
	 * Method to check risk of collision with other robots.
	 * @return ArrayList of PosMessage
	 */
	public ArrayList<PosMessage> checkRiskOfCollision(){
		ArrayList<PosMessage> collRob = new ArrayList<PosMessage>();
		// For each other Robots' position
		for(PosMessage oP : otherRobotPositionList){
			// If collision is possible in the next execution (safety control: and the robot position is not mine)
			if(nextpos.x == oP.x && nextpos.y==oP.y && robotID != oP.robID){
				System.out.println(robotName+ ": RISK OF COLLISION DETECTED AND AVOIDED!");
				collRob.add(oP);
			}
		}
		return collRob;
	}
		
	/**
	 * Method to collect luggage.
	 */
	public void collectLuggage(){
		workingOnLug = true;
		System.out.println(robotName+" says: Ready to collect luggage in position " + mypos.toString() + ".");
		OUTcollectLuggage();
		hasLuggage = true;
		System.out.print(robotName+" says: Luggage collected.\n");
		dest1.x=0;
		dest1.y=0;
		workingOnLug = false;
	}
	
	/**
	 * Method to manage collecting luggage movements.
	 * This method has to be overridden.
	 */
	public void OUTcollectLuggage() {
	}
	
	/**
	 * Method to deposit luggage
	 */
	public void depositLuggage() {
		workingOnLug = true;
		System.out.println(robotName+" says: Ready to deposit luggage in position " + mypos.toString());
		OUTdepositLuggage();
		hasLuggage = false;
		System.out.print(robotName+" says: Luggage deposited.\n");
		dest2.x=0;
		dest2.y=0;
		transporter = false;
		workingOnLug = false;
		busy = false;
		luggageMessage=new ELMessage();
		}
	
	/**
	 * Method to manage depositing luggage movements.
	 * This method has to be overridden.
	 */
	public void OUTdepositLuggage() {
	}
	
	/**
	 * Method to make a robot reach a block, step by step.
	 */
	public void reachBlock(BlockPos breach){
		setPos(getNext());
		int distX = (breach.x - getPos().x);
		int distY = (breach.y - getPos().y);
		// Bring Robot on the same ROW of the destination
		if(distX != 0){
			//System.out.println(robotName+" says: I'm moving to reach the correct ROW.");
			if(distX < 0)
				setNext((getPos().x - 1),getPos().y);
			else{
				setNext((getPos().x + 1),getPos().y);
			}
			System.out.println(robotName+" says: My position now is: "+mypos+ " and my next position will be: "+nextpos);
		}
		// If distX == 0, then the robot is already in correct row
		else{
			//System.out.println(this.getIDname()+" says: I'm in the correct ROW.");
			// Bring Robot on the same COLUMN of the destination
			if (distY != 0){
				//System.out.println(robotName+" says: I'm moving to reach the correct COLUMN.");
				if (distY < 0)
					setNext(getPos().x, (getPos().y - 1));
				else{
					setNext(getPos().x, (getPos().y + 1));
				}
				System.out.println(robotName+" says: My position now is: "+mypos+ " and my next position will be: "+nextpos);
			}
			// If distX == 0, then the robot is already in correct column
			else{
				System.out.println(robotName+" says: I've arrived to destination.");
			}
		}
		OUTmoveWheels();
	}
	
	/**
	 * Method to create make a robot reach its charge position, step by step.
	 */
	public void reachCharge(){
		setPos(getNext());
		int distX = (this.getChargePos().getX() - this.getPos().getX());
		int distY = (this.getChargePos().getY() - this.getPos().getY());
		// Bring Robot on the same COLUMN of the destination
		if(distY != 0){
			//System.out.println(robotName+" says: I'm moving to reach the correct COLUMN.");
				if (distY < 0)
					setNext(getPos().x, (getPos().y - 1));
				else{
					setNext(getPos().x, (getPos().y + 1));
					}
				System.out.println(robotName+" says: My position now is: "+mypos+ " and my next position will be: "+nextpos);
		}
		// If distY == 0, then the robot is already in correct column
		else{
			//System.out.println(robotName+" says: I'm in the correct COLUMN.");
			// Bring Robot on the same ROW of the destination
			if(distX != 0){
				//System.out.println(this.getIDname()+" says: I'm moving to reach the correct ROW.");
				if(distX < 0)
					setNext((getPos().x - 1),getPos().y);
				else{
					setNext((getPos().x + 1),getPos().y);
				}
				System.out.println(robotName+" says: My position now is: "+mypos+ " and my next position will be: "+nextpos);
			}
			// If distX == 0, then the robot is already in correct row
			else{
				System.out.println(robotName+" says: I've arrived to charge position.");
				inCharge = true;
				busy = true;
				reachingCharge = false;
				readyToGoUnderCharge = false;
				// Send a message to call a substitute
				try{
					send(this.getCSMSTopic(), new CSMessage(robotID, mypos.x, mypos.y, 0, 0, 0, 0, 0, 3));
				}catch(TopicNotFoundException e){e.printStackTrace();}
			}
		}
	}	

	/**
	 * Method to manage wheels movements.
	 * This method has to be overridden.
	 */
	public void OUTmoveWheels() {
	}

	/**
	 * Method to simulate the battery recharge.
	 * This method is meant to be overridden.
	 */
	public void rechargeBattery(){
		setBatteryLevel(getBatteryLev()+getBatDischarge()*5);
		if(batteryLev > 100){
			batteryLev = 100;
		}
		if(busy && batteryLev == 100 && inCharge){
			System.out.println(robotName+ " is now fully charged and ready (but still asleep)");
			busy = false;
		}
	}
	
	@Override
	public String toString(){
		return robotName;	
	}
	
	public boolean equals(RobotImpl r){
		if(robotID == r.getRobotID()){
			return true;
		}
		else{
			return false;
		}
	}

	// Getters and setters ----------------------------------------------------
	// Getters:
	/**
	 * Getter method for the robot name variable 
	 * @return returns the the robot name
	 */
	public String getRobotName() {
		return this.robotName;
	}
	
	/**
	 * Getter method for the robot ID variable 
	 * @return returns the the robot ID
	 */
	public int getRobotID() {
		return this.robotID;
	}
	
	/**
	 * Getter method for the mypos variable
	 * @return returns the position of the robot
	 */
	public BlockPos getPos(){
		return this.mypos;
	}
	
	/**
	 * Method to know the next block a robot will go to
	 * @return returns the next BlockPos position
	 */
	public BlockPos getNext() {
		return this.nextpos;
	}
	
	/**
	 * Getter method for the chargpos variable
	 * @return returns the charge position of the robot
	 */
	public BlockPos getChargePos() {
		return this.chargepos;
	}
	
	/**
	 * Getter method for the first destination variable
	 * @return returns the first destination of the robot
	 */
	public BlockPos getDest1() {
		return this.dest1;
	}
	
	/**
	 * Getter method for the second destination variable
	 * @return returns the second destination of the robot
	 */
	public BlockPos getDest2() {
		return this.dest2;
	}
	
	/**
	 * Getter method for the battery level
	 * @return returns the level of the robot's battery
	 */
	public double getBatteryLev(){
		return this.batteryLev;
	}
	
	/**
	 * Getter method for the battery discharge parameter
	 * @return returns the battery discharge parameter
	 */
	public int getBatDischarge() {
		return this.batteryDischarge;
	}

	/**
	 * Getter method to know if the robot is busy
	 * @return returns true if the robot is busy
	 */
	public  boolean isBusy() {
		return this.busy;
	}
	
	/**
	 * Getter method to know if the robot has luggage
	 * @return returns true if the robot has luggage
	 */
	public boolean hasLuggage() {
		return this.hasLuggage;
	}

	/**
	 * Getter method to know if the robot is in charge 
	 * @return returns true if the robot is in charge
	 */
	public boolean isInCharge() {
		return this.inCharge;
	}
	
	/**
	 * Getter method to know if the robot is in a critical situation 
	 * @return returns true if the robot is in a critical situation
	 */
	public boolean isCritical() {
		return this.critical;
	}
	
	/**
	 * Getter method to know if the robot will go to help, next 
	 * @return returns true if the robot will go to help, next
	 */
	public boolean isHealer() {
		return this.healer;
	}
	
	/**
	 * Getter method for the robot position topic variable
	 * @return returns the robot position topic
	 */
	public TopicObject getPosTopic(){
		return this.positionTopic;
	}
	
	/**
	 * Getter method for the msgHelp variable
	 * @return returns the msgHelp variable
	 */
	public CSMessage getMsgHelp() {
		return this.msgHelp;
	}

	/**
	 * Getter method for the CSMSTopic variable
	 * @return returns the CSMSTopic topic
	 */
	public TopicObject getCSMSTopic() {
		return this.CSMSTopic;
	}
	
	/**
	 * Getter method for the ELMSTopic variable
	 * @return returns the ELMSTopic topic
	 */
	public TopicObject getELMSTopic() {
		return this.ELMSTopic;
	}
	
	/**
	 * Getter method for the RLMSTopic variable
	 * @return returns the RLMSTopic topic
	 */
	public TopicObject getRLMSTopic() {
		return this.RLMSTopic;
	}
	
	/**
	 * Getter method for the RMSTopic variable
	 * @return returns the RMSTopic topic
	 */
	public TopicObject getRMSTopic() {
		return this.RMSTopic;
	}

	/**
	 * Getter method for the transporter variable
	 * @return returns true if the robot is a transporter
	 */
	public boolean isTransporter() {
		return this.transporter;
	}
	
	/**
	 * Getter method for the luggage request list variable
	 * @return returns the luggageRequestList variable
	 */
	public ArrayList<ELMessage> getLuggageRequestList() {
		return this.luggageRequestList;
	}
	
//	/**
//	 * Getter method for the luggage response list variable
//	 * @return returns the luggageResponseList variable
//	 */
//	public ArrayList<ELMessage> getLuggageResponseList() {
//		return this.luggageResponseList;
//	}
	
	/**
	 * Getter method for the message luggage variable
	 * @return returns the msgLug variable.
	 */
	public ELMessage getLuggageMessage() {
		return this.luggageMessage;
	}

	/**
	 * Getter method for other robots' position list variable
	 * @return returns a list of PosMessage containing otherRobPos variable
	 */
	public ArrayList<PosMessage> getOtherRobPos() {
		return this.otherRobotPositionList;
	}

	/**
	 * Getter method to know if the robot has to stop a period
	 * @return returns true if the robot has to stop a period
	 */
	public boolean isStopAPeriod() {
		return this.stopAPeriod;
	}

	/**
	 * Getter method to know if the robot is working on a luggage
	 * @return returns true if the robot is working on a luggage
	 */
	public boolean isWorkingOnLug() {
		return this.workingOnLug;
	}

	/**
	 * Getter method to know if the robot is ready to go under charge
	 * @return returns true if the robot is ready to go under charge
	 */
	public boolean isReadyToGoUnderCharge() {
		return this.readyToGoUnderCharge;
	}

	/**
	 * Getter method to know if the robot is reaching charge position
	 * @return returns true if the robot is reaching charge position
	 */
	public boolean isReachingCharge() {
		return this.reachingCharge;
	}

	/**
	 * Getter method for active robots list variable
	 * @return returns the activeRobList variable.
	 */
	public ArrayList<RLMessage> getActiveRobList() {
		return this.activeRobList;
	}
	
	/**
	 * Getter method for help requests list variable
	 * @return returns the helpReqList variable.
	 */
	public ArrayList<CSMessage> getHelpReqList() {
		return this.helpReqList;
	}
	
	/**
	 * Getter method for luggage conflict list variable
	 * @return returns the luggageConflictList variable.
	 */
	public ArrayList<ELMessage> getLuggageConflictList() {
		return this.luggageConflictList;
	}

//	// Setters:	
	/**
	 * Setter method for the robot name variable
	 */
	public void setRobotName(String rName){
		robotName=rName;	
	}
	
	/**
	 * Setter method for the robot ID variable
	 */
	public void setRobotID(int ID) {
		robotID=ID;
	}
	
	/**
	 * Setter method for the robot position variable
	 * @param BlockPosition
	 */
	public void setPos(BlockPos pos) {
		mypos=pos;
	}
	
	/**
	 * Setter method for the robot position variable
	 * @param int, int
	 */
	public void setPos(int newX, int newY) {
		mypos.x=newX;
		mypos.y=newY;
	}
	
	/**
	 * Method to input the next block a robot will go to
	 * @param BlockPosition
	 */
	public void setNext(BlockPos p){
		nextpos=p;
	}
	
	/**
	 * Method to input the next block a robot will go to
	 * @param int, int
	 */
	public void setNext(int nextX, int nextY){
		nextpos=(new BlockPos (nextX, nextY));
	}
	
	/**
	 * Setter method for the robot charge position variable
	 */
	public void setChargePos(BlockPos chargep) {
		chargepos=chargep;	
	}
	
	/**
	 * Setter method for the first destination variable
	 */
	public void setDest1(BlockPos d1) {
		dest1=d1;	
	}
	
	/**
	 * Setter method for the second destination variable
	 */
	public void setDest2(BlockPos d2) {
		dest2=d2;	
	}
	
	/**
	 * Setter method for the level of the battery
	 */
	public void setBatteryLevel(double lev){
		batteryLev=lev;
	}
	
	/**
	 * Setter method for the batDischarge parameter
	 */
	public void setBatDischarge(int batDischarge) {
		batteryDischarge = batDischarge;
	}
	
	/**
	 * Setter method for the busy boolean variable
	 */
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	/**
	 * Setter method for the hasLuggage boolean variable
	 */
	public void setHasLuggage(boolean hasLuggage) {
		this.hasLuggage = hasLuggage;
	}
		
	/**
	 * Setter method for the inCharge boolean variable
	 */
	public void setInCharge(boolean charge) {
		this.inCharge = charge;
	}
	
	/**
	 * Setter method to set if the robot is in a critical situation 
	 */
	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	/**
	 * Setter method to set if the robot will go to heal, next 
	 */
	public void setHealer(boolean goHelp) {
		this.healer = goHelp;
	}

	/**
	 * Setter method to set if the robot personalTopic
	 */
	public void setPosTopic(String s) {
		this.positionTopic = new TopicObject(s);	
	}

	/**
	 * Setter method to set if the robot CSMSTopic
	 */
	public void setCSMSTopic(TopicObject cSMSTopic) {
		this.CSMSTopic = cSMSTopic;
	}

	/**
	 * Setter method to set if the robot ELMSTopic
	 */
	public void setELMSTopic(TopicObject eLMSTopic) {
		this.ELMSTopic = eLMSTopic;
	}
	
	/**
	 * Setter method to set if the robot RLMSTopic
	 */	
	public void setRLMSTopic(TopicObject rLMSTopic) {
		this.RLMSTopic = rLMSTopic;
	}

	/**
	 * Setter method to set if the robot is a transporter
	 */
	public void setTransporter(boolean transporter) {
		this.transporter = transporter;
	}
	
	/**
	 * Setter method to set the luggage request list variable
	 */	
	public void setLuggageRequestList(ArrayList<ELMessage> lugList) {
		this.luggageRequestList = lugList;
	}

	/**
	 * Setter method to set the luggage message variable
	 */
	public void setLuggageMessage(ELMessage msgLug) {
		this.luggageMessage = msgLug;
	}
	
	/**
	 * Setter method to set the help message variable
	 */
	public void setMsgHelp(CSMessage csmsMessage) {
		this.msgHelp=csmsMessage;		
	}

	/**
	 * Setter method to set other robots' position list variable
	 */
	public void setOtherRobPos(ArrayList<PosMessage> otherRobPos) {
		this.otherRobotPositionList = otherRobPos;
	}
	
	/**
	 * Setter method to set if the robot has to stop for a period
	 */
	public void setStopAPeriod(boolean stopAPeriod) {
		this.stopAPeriod = stopAPeriod;
	}

	/**
	 * Setter method to set if the robot is working on a luggage (e.g. collect or deposit it)
	 */
	public void setWorkingOnLug(boolean workingOnLug) {
		this.workingOnLug = workingOnLug;
	}

	/**
	 * Setter method to set if the robot is ready to go under charge once finished its task
	 */
	public void setReadyToGoUnderCharge(boolean readyToGoUnderCharge) {
		this.readyToGoUnderCharge = readyToGoUnderCharge;
	}

	/**
	 * Setter method to set if the robot is reaching charge position
	 */
	public void setReachingCharge(boolean reachingCharge) {
		this.reachingCharge = reachingCharge;
	}

	/**
	 * Setter method to set the active robot list
	 */
	public void setActiveRobList(ArrayList<RLMessage> activeRobList) {
		this.activeRobList = activeRobList;
	}
	
	/**
	 * Setter method to set the help requests list
	 */
	public void setHelpReqList(ArrayList<CSMessage> helpReqList) {
		this.helpReqList = helpReqList;
	}
	
	/**
	 * Setter method to set the luggage conflict list
	 */
	public void setLugConflictList(ArrayList<ELMessage> l) {
		this.luggageConflictList = l;
	}
}