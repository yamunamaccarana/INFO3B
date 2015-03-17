package unibg.saoms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import org.osoa.sca.annotations.Scope;

import unibg.robotics.tca.Message;
import unibg.robotics.tca.Task;
import unibg.robotics.tca.TaskInterface;
import unibg.robotics.tca.TopicNotFoundException;
import unibg.robotics.tca.TopicObject;
import unibg.saoms.MAPEKMS.AwakeAsleepManagingSystem;
import unibg.saoms.msg.EEMessage;
import unibg.saoms.msg.RLMessage;
import unibg.saoms.msg.RMessage;
import unibg.saoms.util.MRcounter;

/**
 * 
 * This is the master managing class of robot components.
 * It is used to awake/asleep them while running.
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

@Scope("COMPOSITE")
public class MasterRobotManagingSystem extends Task implements TaskInterface{

	// Fields----------------------------------------------------------------
	// Counter List of robots and their execution time
	protected ArrayList<MRcounter> counter = new ArrayList<MRcounter>();
	// Counter Limit to indicate how many periods a robot will need to stay awaken
	protected int counterLimit;
	// Number of pieces of luggage a single robot will take care of
	protected int numOfLugForRob;
	
	// MAPE and MAPEtopics--------------------------------------------------
	private AwakeAsleepManagingSystem AAMS;
	// System topic to receive messages about exceptional events
	protected TopicObject EETopic = new TopicObject("EETOPIC");
	
	// System topic to know which robot needs to activate/deactivate
	public TopicObject RMSTopic = new TopicObject("RMSTOPIC");
	// RLMS topic to receive active robots list
	protected TopicObject RLMSTopic = new TopicObject("RLMSTOPIC");
	// Active Robot List
	protected ArrayList<RLMessage> activeRobList = new ArrayList<RLMessage>();
	// Already awaken Robot List
	protected ArrayList<RLMessage> awakenRobList = new ArrayList<RLMessage>();
	// Already aslept Robot List
	protected ArrayList<RLMessage> asleepRobList = new ArrayList<RLMessage>();
	// Exceptional Event List
	protected ArrayList<EEMessage> EEList = new ArrayList<EEMessage>();

	// Constructor---------------------------------------------------------
	/**
	 * Constructor method for MasterRobotManagingSystem
	 */
	public MasterRobotManagingSystem(){
		this.AAMS = new AwakeAsleepManagingSystem();
		this.setCounterLimit(50);
		this.setNumOfLugForRob(2);
	}
	
	// Methods---------------------------------------------------------------
	@Override
	public void initialize() {
		// RMS Topics------------------------------------------------
		// Publish on the ActiveRobot topic
		publish(RMSTopic);
		// Subscribe to ExceptionalEvents topic
		subscribe(EETopic);
		
		// RLMS Topics-----------------------------------------------
		// Subscribe to RobotListManagingSystem topic
		subscribe(RLMSTopic);
		
		java.util.Date date= new java.util.Date();
		System.out.println("MasterRobotManagingSystem starts at "+new Timestamp(date.getTime()));
	}

	@Override
	public void execute() {	
		updateActiveRobotList();
		updateExceptionalEventList();
		AAMS.callMAPE(this);
		
		// Increment counters
		for(MRcounter mc : counter){
			mc.setCount((mc.getCount()+1));
		}
	}
	
	/**
	 * Method to update exceptional event list. It has to be call once each period.
	 */
	private void updateExceptionalEventList() {
		EEList.clear();
		// Receive new exceptional event messages
		Message msg;
		EEMessage eemsg;
		Iterator<Message> iterator = null;
		try{
			iterator=receive(EETopic);
		}catch(TopicNotFoundException e){e.printStackTrace();}
		
		// Update list
		while(iterator.hasNext()){
			msg=iterator.next();
			eemsg=(EEMessage)msg.getMessage();
//			System.out.println("MasterRobotManagingSystem - Message from topic: "+msg.getTopic().getTopicName()+": \""+eemsg.toString()+"\"");
			EEList.add(eemsg);
		}	
		if(!EEList.isEmpty()){
			System.out.println("MasterRobotManagingSystem updated active robot list:		"+activeRobList);
		}

	}

	/**
	 * Method to update active robots list. It has to be call once each period.
	 */
	private void updateActiveRobotList() {
		awakenRobList.clear();
		asleepRobList.clear();
		
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
//			System.out.println("MasterRobotManagingSystem - Message from topic: "+msg.getTopic().getTopicName()+": \""+rlmsg.toString()+"\"");
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
			activeRobList.add(rlmsg);
		}
		
		//System.out.println("MasterRobotManagingSystem updated active robot list:		"+activeRobList);
	}

	/**
	 * Method to awake or asleep robot components.
	 * Awake n robots if n>0.
	 * Asleep n robots if n<0.
	 */
	public void awakeasleep(int n) {
		// Awake robots (priority: most charged, minor ID)
		if(n>0){
			for(int i=0; i<n; i++){
				awakeRobot();
			}
		}
		// Asleep robots (priority: less charged, major ID)
		if(n<0){
			for(int i=0; i<n; i++){
				asleepRobot();
			}
		}
	}

	/**
	 * Method to awake a robot.
	 * Priority: battery level, minor ID number.
	 */
	private void awakeRobot() {
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
						if((actR1.battery < actR2.battery && !awakenRobList.contains(actR2)) || awakenRobList.contains(actR1)){
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
					if((r1.robID > r2.robID && !awakenRobList.contains(r2))|| awakenRobList.contains(r1)){
						go=false;
					}
				}
				if(go){
					IDnumToGo = r1.robID;
					awakenRobList.add(r1);
					// Send a message to awake the selected Robot
					try {
						send(RMSTopic,new RMessage(IDnumToGo, true));
					}catch(TopicNotFoundException e){e.printStackTrace();}
				}
			}
		}
		else{
			// Send a message to awake the selected Robot
			try {
				send(RMSTopic,new RMessage(charged.get(0).robID, true));
			}catch(TopicNotFoundException e){e.printStackTrace();}
		}
	}
	
	/**
	 * Method to asleep a robot.
	 * Priority: battery level, major ID number.
	 */
	private void asleepRobot() {
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
						if((actR1.battery > actR2.battery && !asleepRobList.contains(actR2)) || asleepRobList.contains(actR1)){
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
					if((r1.robID < r2.robID && !asleepRobList.contains(r2))|| asleepRobList.contains(r1)){
						go=false;
					}
				}
				if(go){
					IDnumToGo = r1.robID;
					asleepRobList.add(r1);
					// Send a message to awake the selected Robot
					try {
						send(RMSTopic,new RMessage(IDnumToGo, false));
					}catch(TopicNotFoundException e){e.printStackTrace();}
				}
			}
		}
		else{
			// Send a message to awake the selected Robot
			try {
				send(RMSTopic,new RMessage(charged.get(0).robID, false));
			}catch(TopicNotFoundException e){e.printStackTrace();}
		}	
	}
	
	// Getters and Setters-------------------------------------------
	// Getters
	/**
	 * Getter method for the counter variable
	 * @return returns the counter variable
	 */
	public ArrayList<MRcounter> getCounter() {
		return counter;
	}

	/**
	 * Getter method for the EETopic variable
	 * @return returns the EETopic variable
	 */
	public TopicObject getEETopic() {
		return EETopic;
	}

	/**
	 * Getter method for the counterLimit variable
	 * @return returns the counterLimit variable
	 */
	public int getCounterLimit() {
		return counterLimit;
	}

	/**
	 * Getter method for the numOfLugForRob variable
	 * @return returns the numOfLugForRob variable
	 */
	public int getNumOfLugForRob() {
		return numOfLugForRob;
	}

	// Setters
	/**
	 * Setter method for the counter variable
	 */
	public void setCounter(ArrayList<MRcounter> counter) {
		this.counter = counter;
	}

	/**
	 * Setter method for the EETopic variable
	 */
	public void setEETopic(TopicObject eETopic) {
		EETopic = eETopic;
	}

	/**
	 * Setter method for the counterLimit variable
	 */
	public void setCounterLimit(int counterLimit) {
		this.counterLimit = counterLimit;
	}

	/**
	 * Setter method for the numOfLugForRob variable
	 */
	public void setNumOfLugForRob(int numOfLugForRob) {
		this.numOfLugForRob = numOfLugForRob;
	}

	public ArrayList<EEMessage> getEEList() {
		return EEList;
	}

	public void setEEList(ArrayList<EEMessage> eEList) {
		EEList = eEList;
	}
}
