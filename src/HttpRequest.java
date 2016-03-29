import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpRequest implements Runnable {

	Socket socket;
	Server server;
	HttpResponse response;

	//stores the http method
	private String method;
	
	//request uri in the request sent by the client
	private String request_uri;
	private String http_version;
	
	//it will store the http request header params after parsing
	HashMap<String, String> headerParams = new HashMap<>();
	
	public HttpRequest(Socket socket) {
		this.socket = socket;
	}

	//thread will execute this run method, which will call the processRequest method
	@Override
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.err.println("Exception caught while processing request" + e);
		}
	}

	// this method will be called from run to process the request
	private void processRequest() throws IOException {
		try {
			// obtain the input and output stream of the socket
			InputStream inp = socket.getInputStream();
			OutputStream op = socket.getOutputStream();

			// parse the httprequest
			if(!parseHttpRequest(inp)) {
				System.err.println("Invalid http request parameters.");
				response = new HttpResponse(null);
				
				//send the response back to the client
				sendResponse(response.formHttpResponse());
			}
			
			//if the request is valid, send the response back
			File f = new File(HttpConstants.RESOURCE_DIR+ request_uri);
			response = new HttpResponse(f);
			
			//added for testing purpose
			/*try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			sendResponse(response.formHttpResponse());
			
			//closing the input and output streams
			inp.close();
			op.close();
		}

		catch (IOException e) {
			System.err.println("IOException caught" + e);
		}
		// closing the socket connection in finally.
		finally {
			//even the socket closing can throw IOException
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Error in closing the socket connection" + e);
			}
		}

	}

	/*Function to parse the http request*/
	private boolean parseHttpRequest(InputStream inp) {
		BufferedReader br = new BufferedReader(new InputStreamReader(inp));
		try {
			
			//read the first request line
			String requestLine = br.readLine();
			String requestLineContents[] = requestLine.split(" ");
			if(requestLineContents.length != 3) {
				System.err.println("Invalid number of parameters received in request line");
				return false;
			} else {
				method = requestLineContents[0];
				request_uri = requestLineContents[1];
				http_version = requestLineContents[2];
				
				//proceed only if the valid values are received in the request line
				if(!validateRequestLineContents(method, http_version)) {
					System.err.println("Invalid parameters received in request line");
					return false;
				}
			}
			
			//read the headers now in the next lines.
			// Get and display the header lines. Add the param name and value in the hashmap
	        String headerLine = null;
	        //System.out.println("Header Lines:");
	        while ((headerLine = br.readLine()).length() != 0) {
	        		//System.out.println(headerLine);
	                String Parampair[] = headerLine.split(":");
	                headerParams.put(Parampair[0], Parampair[1]);
	        }
	        
	        //Since it is a GET Request, so not using body contents.
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
		
	}

	//this function will send the prepared response back to the client
	public void sendResponse(HttpResponse response) {
		String responseString = response.toString();		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		writer.write(responseString);
		writer.flush();
	}
	
	//validate if the method was Get and HTTP version 1.1
	private boolean validateRequestLineContents(String method, String http_version) {
		if(method.equals(HttpConstants.HTTP_GET) && http_version.startsWith(HttpConstants.HTTP_Protocol)) {
			return true;
		} else 
			return false;
	}
}
