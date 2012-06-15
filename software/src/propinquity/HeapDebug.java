package propinquity;

/**
 * This is a simple low-fi threaded utility to debug the heap size. When it is running it prints information about the heap size at regular intervals.
 *
 */
public class HeapDebug implements Runnable {

	static int MB = 1024*1024;

	Runtime runtime;

	boolean running;
	Thread debugThread;

	public HeapDebug() {
		runtime = Runtime.getRuntime();
	}

	public void run() {		
		//Getting the runtime reference from system
		while(running) {
			System.out.println("Heap Stats [MB]:");
			System.out.println("\tUsed:" + (float)(runtime.totalMemory() - runtime.freeMemory()) / MB);
			System.out.println("\tFree Memory:" + (float)runtime.freeMemory() / MB);
			System.out.println("\tTotal Memory:" + (float)runtime.totalMemory() / MB);
			System.out.println("\tMax Memory:" + (float)runtime.maxMemory() / MB);

			try {
				Thread.sleep(1000);
			} catch(Exception e) {

			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void stop() {
		running = false;
		if(debugThread != null) while(debugThread.isAlive()) Thread.yield();
	}

	public void start() {
		running = true;
		debugThread = new Thread(this);
		debugThread.setDaemon(true);
		debugThread.start();
	}

}
