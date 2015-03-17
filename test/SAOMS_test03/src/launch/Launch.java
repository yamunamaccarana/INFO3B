package launch;

import org.apache.tuscany.sca.host.embedded.SCADomain;

import unibg.robotics.tca.TaskInterface;

public class Launch extends Thread{
    public static void main(String[] args) throws Exception
    {
        Launch launch = new Launch();
        launch.start();
    }

	public void run()
	{
		//Starting...
		System.out.println("Starting...");
		//Instantiating..
		SCADomain scaDomain = SCADomain.newInstance("launchtasks.composite");
		
		TaskInterface MasterEnvironmentManagingSystem=scaDomain.getService(TaskInterface.class, "MasterEnvironmentManagingSystem/TaskInterface");
		TaskInterface MasterRobotManagingSystem=scaDomain.getService(TaskInterface.class, "MasterRobotManagingSystem/TaskInterface");
		TaskInterface Environment=scaDomain.getService(TaskInterface.class, "Environment/TaskInterface");
		TaskInterface robot001=scaDomain.getService(TaskInterface.class, "Robot001/TaskInterface");
		TaskInterface robot002=scaDomain.getService(TaskInterface.class, "Robot002/TaskInterface");
		
		MasterEnvironmentManagingSystem.scheduleTask();
		MasterRobotManagingSystem.scheduleTask();
		Environment.scheduleTask();
		robot001.scheduleTask();
		robot002.scheduleTask();
	}
}

