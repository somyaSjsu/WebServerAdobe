import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {

	private ServerSocket serverSocket;
	private ExecutorService threadPoolSvc;

	
	public static void main(String[] args) throws IOException {
		new Thread(new Server()).start();
	}

	@Override
	public void run() {
		try {
			//keeping the port as 8080 
			serverSocket = new ServerSocket(HttpConstants.SERVER_PORT);
			
			//keeping a max count of 10 threads
			threadPoolSvc = Executors.newFixedThreadPool(HttpConstants.MAX_THREAD);
		} catch (IOException e) {
			System.err.println("Cannot listen on port " + HttpConstants.SERVER_PORT);
			System.exit(1);
		}
		
		System.out.println("Server Listening on port: "+ HttpConstants.SERVER_PORT);
		
		//Loop until the main thread is not interupted
		while (!Thread.interrupted()) {
			try {
				threadPoolSvc.execute(new HttpRequest(serverSocket.accept()));
			} catch (IOException e) {
				System.err.println("Cannot accept requests from client.");
			}
		}
		
		//closing the server socket now
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		threadPoolSvc.shutdown();
		
		//wait for 20 more seconds so that tasks which are in execution also get completed before termination
		try {
			if (!threadPoolSvc.awaitTermination(20, TimeUnit.SECONDS)) 
				threadPoolSvc.shutdownNow();
		} catch (InterruptedException e) {}
	}
}
