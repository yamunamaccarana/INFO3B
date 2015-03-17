package unibg.saoms.MAPEKMS;

import java.util.ArrayList;
import unibg.saoms.MAPEKclass;
import unibg.saoms.msg.PosMessage;

/**
 * This class manages robots to avoid eventual crashes.
 * It is a MAPE-K class
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class CollisionManagingSystem extends MAPEKclass {

	// Fields----------------------------------------------------------------
	// Risk of collision
	private boolean risk;
	// Robots with risk of collision
	private ArrayList<PosMessage> collRob = new ArrayList<PosMessage>();
	// Need to execute some computation
	private boolean exec;
	
	
	// MAPE------------------------------------------------------------------
	@Override
	public void monitor(){
		// CollisionManagingSystem MAPE
		// @M
		//System.out.println("CollisionManagingSystem says: Monitoring...");
		
		// Monitor others' position
	}

	@Override
	public void analyze() {
		// CollisionManagingSystem MAPE
		// @A
		//System.out.println("CollisionManagingSystem says: Analyzing...");
		
		// Analyze risk of collision
		this.collRob.clear();
		this.risk=false;
		this.collRob = this.getRob().checkRiskOfCollision();
		if(!collRob.isEmpty()){
			this.risk=true;
		}
	}

	@Override
	public void plan() {
		// CollisionManagingSystem MAPE
		// @P
		//System.out.println("CollisionManagingSystem says: Planning...");
		
		// If there is a risk of collision: plan
		if(risk){
			this.exec=false;
			// For each risk of collision
			for(PosMessage otherColl : this.collRob){
				// If my ID is not the minor: stop a period 
				// Safety check: this implicitly manages also conflicts with the robot itself
				if(getRob().getRobotID() > otherColl.robID){
					exec=true;
				}
			}
		}
		else{
			exec=false;
		}
	}

	@Override
	public void execute() {
		// CollisionManagingSystem MAPE
		// @E
		//System.out.println("CollisionManagingSystem says: Execute...");
		
		getRob().setStopAPeriod(exec);
	}
}
