package scheduler;

import floorSubSystem.DataStorage;

/**
 * @author Aayush Mallya & Harshil Verma
 */
public class Scheduler implements Runnable {

	@Override
	public void run() {
		while(true){
			setRequest(data);
			try{
				Thread.sleep(500);
			} catch (InterruptExecption e){
				e.printStackTrace();
			}
		}
	}
	
	public DataStorage getRequest() {
		getRequestTime();
		getCurrentFloor();
		getGoingUp();
		getDestinationFloor();
	}
	
	public synchronized void setRequest(DataStorage data) {
		this.data = data;
		
	}

}
