package unibg.saoms.MAPEKMS;

import unibg.saoms.MAPEKclass;
import unibg.saoms.util.BlockPos;

/**
 * This class is used to make a robot move through a path to reach a destination, step by step.
 * It is a MAPE-K class
 * 
 * @author Yamuna Maccarana
 * @author Umberto Paramento
 * 
 */

public class PathFollowerManagingSystem extends MAPEKclass {

	// Fields----------------------------------------------------------------
	private boolean stop;
	private BlockPos destination;
	private int exec;
	private boolean goUndCh;
	
	
	// MAPE------------------------------------------------------------------
	@Override
	public void monitor(){
		// CollisionManagingSystem MAPE
		// @M
		//System.out.println("CollisionManagingSystem says: Monitoring...");
		
		// Monitor stopAPeriod, busy, readyToGoUnderCharge, isWorkingOnLuggage and isInCharge variables
	}

	@Override
	public void analyze() {
		// CollisionManagingSystem MAPE
		// @A
		//System.out.println("CollisionManagingSystem says: Analyzing...");
		
		// Analyze if this robot has to stop a period or is not busy or is working on luggage: stop=true
		stop=(getRob().isStopAPeriod() || (!getRob().isBusy() && !getRob().isReadyToGoUnderCharge()) || getRob().isWorkingOnLug() || getRob().isInCharge());
		goUndCh=((!getRob().isBusy() && getRob().isReadyToGoUnderCharge()) || getRob().isReachingCharge());
	}

	@Override
	public void plan() {
		// CollisionManagingSystem MAPE
		// @P
		//System.out.println("CollisionManagingSystem says: Planning...");
		
		// If stop: do not do anything
		if(stop){
			exec=0;
			return;
		}
		if(goUndCh){
			exec=5;
			return;
		}
		// Else: select destination
		else{
			// If robot is in dest1: collect luggage
			if(getRob().getPos().equals(getRob().getDest1()) && !getRob().getDest1().equals(new BlockPos(0, 0))){
				exec=1;	
			}
			// If robot is in dest2: deposit luggage
			if(getRob().getPos().equals(getRob().getDest2()) && !getRob().getDest2().equals(new BlockPos(0, 0))){
				exec=2;
			}
			// If robot has a first destination and is not in that destination: select destination1
			if(!getRob().getDest1().equals(new BlockPos(0, 0)) && !getRob().getPos().equals(getRob().getDest1())){
				exec=3;
				destination=getRob().getDest1();
			}
			// If robot has not a first destination and is not in the second destination (unless second destination is 0): select destination2
			if(getRob().getDest1().equals(new BlockPos(0, 0)) && !getRob().getPos().equals(getRob().getDest2()) && !getRob().getDest2().equals(new BlockPos(0, 0))){
				exec=4;
				destination=getRob().getDest2();
			}
		}
	}

	@Override
	public void execute() {
		// CollisionManagingSystem MAPE
		// @E
		//System.out.println("CollisionManagingSystem says: Execute...");
		switch(exec){
		case 0:
			return;
		case 1:
			getRob().collectLuggage();
			break;
		case 2:
			getRob().depositLuggage();
			break;
		case 3:	
			getRob().reachBlock(destination);
			break;
		case 4:
			getRob().reachBlock(destination);
			break;
		case 5:
			System.out.println(getRob().toString()+" has low battery and is going under charge...");
			getRob().setReachingCharge(true);
			getRob().reachCharge();
			break;
		}
	}
}
