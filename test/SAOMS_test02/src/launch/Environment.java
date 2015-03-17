package launch;

import java.sql.Timestamp;

import org.osoa.sca.annotations.Scope;

import unibg.robotics.tca.Task;
import unibg.robotics.tca.TaskInterface;
import unibg.robotics.tca.TopicNotFoundException;
import unibg.robotics.tca.TopicObject;
import unibg.saoms.msg.ELMessage;

@Scope("COMPOSITE")
public class Environment extends Task implements TaskInterface {

	private int it = 0;
	
	// EnvironmentTopic
	protected TopicObject ETopic = new TopicObject("ETOPIC");
	
	@Override
	public void initialize() {
		publish(ETopic);
		java.util.Date date= new java.util.Date();
		System.out.println("Enviroment tests start at "+new Timestamp(date.getTime()));
	}
	
	@Override
	public void execute() {
		java.util.Date date= new java.util.Date();
		//System.out.println("Enviroment: execution at "+new Timestamp(date.getTime()));
		// Iterator++ at each execution: new test events at each execution
		switch(it){
		case 0:
			System.out.println("Enviroment: ready to test at "+new Timestamp(date.getTime()));
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			it++;
			it++;
			break;
//		case 1:
//			// Test1: new luggage
//			EEMessage t1 = new EEMessage(Event.LANDING, 4);
//			System.out.println("Enviroment: Test0: "+t1.toString()+" at "+new Timestamp(date.getTime())+" ");
//			try {
//				send(ETopic, t1);
//			} catch (TopicNotFoundException e) {e.printStackTrace();}
//			it++;
//			break;
		case 2:
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			// Test1: new luggage
			ELMessage t2 = new ELMessage(0, 2, 3, 1, 3, 10);
			System.out.println("Enviroment: Test1: "+t2.toString()+" at "+new Timestamp(date.getTime())+" ");
			try {
				send(ETopic, t2);
			} catch (TopicNotFoundException e) {e.printStackTrace();}
			it++;
			break;
		case 3:
			// Test1: new luggage
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			ELMessage t3 = new ELMessage(0, 3, 1, 5, 10, 5);
			System.out.println("Enviroment: Test2: "+t3.toString()+" at "+new Timestamp(date.getTime())+" ");
			try {
				send(ETopic, t3);
			} catch (TopicNotFoundException e) {e.printStackTrace();}
			it++;
			break;
		case 4:
			// Test1: new luggage not in an expected position!!!
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			ELMessage t4 = new ELMessage(0, 4, 10, 8, 1, 8);
			System.out.println("Enviroment: Test3: "+t4.toString()+" at "+new Timestamp(date.getTime())+" ");
			try {
				send(ETopic, t4);
			} catch (TopicNotFoundException e) {e.printStackTrace();}
			it++;
			break;
		case 5:
			// Test1: new luggage not in an expected position!!!
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			ELMessage t5 = new ELMessage(0, 5, 3, 1, 3, 10);
			System.out.println("Enviroment: Test4: "+t5.toString()+" at "+new Timestamp(date.getTime())+" ");
			try {
				send(ETopic, t5);
			} catch (TopicNotFoundException e) {e.printStackTrace();}
			it++;
			break;

		}
	}

	


}
