package robot;

import java.io.Serializable;

import org.osoa.sca.annotations.Scope;

import unibg.saoms.util.BlockPos;
import unibg.saoms.RobotImpl;

@Scope("COMPOSITE")
public class Robot002 extends RobotImpl implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public Robot002(){
		super();
		BlockPos cp = new BlockPos(0, 2);
		this.robotName = "Robot002";
		this.robotID = 2;
		this.mypos = new BlockPos(cp);
		this.chargepos = new BlockPos(cp);
		this.nextpos = new BlockPos(cp);
		this.batteryLev=100;
		this.setBusy(false);
		this.setInCharge(true);
		this.setCritical(false);
		this.setBatDischarge(2);
	}

	/*	public void initialize(){
		super.initialize();
		//...do some computation: this will be run once at the initialization of the Robot
		//...e.g. say Hello to the World once ready to start
	}

	public void execute(){
		super.execute();
		//...do some computation: this will be execute once every period or the Robot (1 second now)
		//...e.g. caption of pictures from the camera
		//...e.g. control hardware
	}*/
	
	@Override
	public void OUTcollectLuggage() {
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	@Override
	public void OUTdepositLuggage() {
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {e.printStackTrace();}
	}
}
