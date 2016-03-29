import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import web.HttpResponse.ContentType;

public class HttpResponse {

	//http status code
	private String statusCode;
	
	//file whose contents are asked by the client.
	File outFile;
	
	//hashmap which will store the response headers
	public HashMap<String, String>responseHeaders = new HashMap<>();
	private byte[] body = null;
	
	public HttpResponse (File outFile) {
		this.outFile = outFile;
	}

	/*This function will form the http response - error or success*/
	public HttpResponse formHttpResponse() {
		if(outFile == null) {
			System.err.println("Invalid HttpRequest, sending back error response");
			String errorStr = "<html><body> Bad Protocol Request, Either Version or the Method not supported </body></html>";
			statusCode = HttpConstants.STATUS_400;
			return sendHttpErrorResponse(errorStr);
		} else {
			if(outFile.isFile()) {
				statusCode = HttpConstants.STATUS_SUCCESS;
				return sendHttpSuccessResponseWithFile(outFile);
			}
			else {
				System.err.println("Invalid File name or location");
				String errorStr = "<html><body>File " + outFile + " not found.</body></html>";
				statusCode = HttpConstants.STATUS_404;
				return sendHttpErrorResponse(errorStr);
			}
				
		}
	}
	
	/*Function to set the Date header in the header response map*/
	public void setDate() {
		responseHeaders.put("Date", new Date().toString());
	}

	/*Function to set the Content length in the header response map*/
	public void setContentLength(long value) {
		responseHeaders.put("Content-Length", String.valueOf(value));
	}

	/*Function to set the Content type in the header response map*/
	public void setContentType(String value) {
		responseHeaders.put("Content-Type", value);
	}

	/*Function which will form the response headers and body in case of an error.
	 * ErrorStr passed as a param will become the error message or body of the response*/
	private HttpResponse sendHttpErrorResponse(String errorStr) {
		setContentLength(errorStr.getBytes().length);
		setContentType(ContentType.HTML);
		setDate();
		body = null;
		body = errorStr.getBytes();
		
		System.out.println("Here.....");
		return this;
	}
	
	/*Function which will form the response headers and body in case of an success.
	 * The contents of the outFile passed as a parameter will be the body of the response*/
	private HttpResponse sendHttpSuccessResponseWithFile(File outFile) {
		try {
			FileInputStream reader = new FileInputStream(outFile);
			int length = reader.available();

			// read the contents of the file in the body
			body = new byte[length];
			reader.read(body);
			reader.close();

			setContentLength(length);
			setDate();
			statusCode = HttpConstants.STATUS_SUCCESS;
			
			//set the content type based on the file extension
			if (outFile.getName().endsWith(".htm") || outFile.getName().endsWith(".html")) {
				setContentType(ContentType.HTML);
			} else {
				setContentType(ContentType.TEXT);
			}
		} catch (IOException e) {
			System.err.println("Exception caught while reading from file" + e);
		}
		
		return this;
	}
	
	//over ride the string method of this class to form the Http response body
	@Override
	public String toString() {
		String responseStr = HttpConstants.HTTP_Protocol + " " + statusCode +"\n";
		for (Entry<String, String> key : (responseHeaders).entrySet()) {
			responseStr += key + ": " + responseHeaders.get(key) + "\n";
		}
		responseStr += "\r\n";
		if (body != null) {
			responseStr += new String(body);
		}
		return responseStr;
	}
}
