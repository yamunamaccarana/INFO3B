package unibg.saoms;

import java.sql.Timestamp;
import java.util.Iterator;

import org.osoa.sca.annotations.Scope;

import unibg.robotics.tca.Message;
import unibg.robotics.tca.Task;
import unibg.robotics.tca.TaskInterface;
import unibg.robotics.tca.TopicNotFoundException;
import unibg.robotics.tca.TopicObject;
import unibg.saoms.msg.EEMessage;
import unibg.saoms.msg.ELMessage;

/**
 * 
 * This is the master managing class of enviroment events.
 * It is used to classify and retrieve messages on corrected topics.
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

@Scope("COMPOSITE")
public class MasterEnvironmentManagingSystem extends Task implements TaskInterface {

	// Fields----------------------------------------------------------------
	// System topic to send messages about exceptional events
	protected TopicObject EETopic = new TopicObject("EETOPIC");
	// ELSM topic to send new luggage managing request
	protected TopicObject ELMSTopic = new TopicObject("ELMSTOPIC");
	// EnvironmentTopic to receive new events
	protected TopicObject ETopic = new TopicObject("ETOPIC");
	
	// Constructor---------------------------------------------------------
	/**
	 * Constructor method for MasterRobotManagingSystem
	 */
	public MasterEnvironmentManagingSystem(){
	}
	
	// Methods---------------------------------------------------------------
	@Override
	public void initialize() {
		// Publish on EventLuggageManagingSystem topic
		publish(this.getELMSTopic());
		// Publish on ExceptionalEvents topic
		publish(this.getEETopic());
		// Subscribe on Environment topic
		subscribe(this.getETopic());
		java.util.Date date= new java.util.Date();
		System.out.println("MasterEnviromentManagingSystem starts at "+new Timestamp(date.getTime()));
	}
	
	@Override
	public void execute() {
		java.util.Date date= new java.util.Date();
		// Receive new event messages from the environment
		Iterator<Message> iterator = null;
		try {
			//System.out.println("MasterEnviromentManagingSystem receiving messages... "+new Timestamp(date.getTime()));
			iterator=this.receive(ETopic);
		} catch (TopicNotFoundException e) {e.printStackTrace();}
		ELMessage elmsg = new ELMessage();
		EEMessage eemsg = new EEMessage();
		// If there are new messages:
		while(iterator.hasNext()){
			System.out.println("MasterEnviromentManagingSystem: new event received and managed at "+new Timestamp(date.getTime()));
			Message msg;
			msg=iterator.next();
			// If EventLuggageMsg: retrieve it to EventLuggageMSTopic
			if(msg.getMessage().getClass().equals(elmsg.getClass())){
				//System.out.println("MasterEnviromentManagingSystem: retrieving new luggage message at "+new Timestamp(date.getTime()));
				elmsg=(ELMessage) msg.getMessage();
				try {
					this.send(this.getELMSTopic(), elmsg);
					//System.out.println("MasterEnviromentManagingSystem: message sent at "+new Timestamp(date.getTime()));
				} catch (TopicNotFoundException e) {e.printStackTrace();}
			}
			// If ExceptionalEventMsg: retrieve it to ExceptionalEventTopic
			if(msg.getMessage().getClass().equals(eemsg.getClass())){
				eemsg=(EEMessage) msg.getMessage();
				try {
					//System.out.println("MasterEnviromentManagingSystem sending new messages "+new Timestamp(date.getTime()));
					this.send(this.getEETopic(), eemsg);
				} catch (TopicNotFoundException e) {e.printStackTrace();}
			}
		}
	}

	// Getters and Setters--------------------------------------------------
	// Getters
	/**
	 * Method to get the ELMSTopic variable
	 * @return returns the ELMSTopic variable
	 */
	public TopicObject getEETopic() {
		return EETopic;
	}

	/**
	 * Method to get the ELMSTopic variable
	 * @return returns the ELMSTopic variable
	 */
	public TopicObject getELMSTopic() {
		return ELMSTopic;
	}
	
	/**
	 * Method to get the ETopic variable
	 * @return returns the ETopic variable
	 */
	public TopicObject getETopic() {
		return ETopic;
	}

	// Setters
	/**
	 * Method to set the EETopic variable
	 */
	public void setEETopic(TopicObject eETopic) {
		EETopic = eETopic;
	}

	/**
	 * Method to set the ELMSTopic variable
	 */
	public void setELMSTopic(TopicObject eLMSTopic) {
		ELMSTopic = eLMSTopic;
	}
	
	/**
	 * Method to set the ETopic variable
	 */
	public void setETopic(TopicObject eTopic) {
		ETopic = eTopic;
	}
}
