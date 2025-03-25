package recorder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JSONManager;
import utils.RESTClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParasoftRecorder {
	
	private static final Logger log = LoggerFactory.getLogger(ParasoftRecorder.class);
	
	private String SOATEST_HOST = ""; //= "localhost";
	private String SOATEST_PORT = ""; //= "9080";
	private String RECORDER_HOST = ""; //= "localhost";
	private String RECORDER_BASE_URL = ""; //= "http://"+RECORDER_HOST+":"+"40090";
	
	private String recordingProxyPort = "";
	private String recordingSessionId = "";
	
	public ParasoftRecorder(String soatestHost, String soatestPort, String recorderHost, String recorderPort) {
		this.SOATEST_HOST = soatestHost;
		this.SOATEST_PORT = soatestPort;
		this.RECORDER_HOST = recorderHost;
		this.RECORDER_BASE_URL = "http://"+recorderHost+":"+recorderPort;
	}
	
	// use with localhost default ports execution
	public ParasoftRecorder() {
		this("localhost","9080","localhost","40090");
	}

	public String getActiveSessions(){
		return JSONManager.beautifyJSON(RESTClient.GET(RECORDER_BASE_URL + "/api/v1/sessions"));
	}

	public String deleteSession(String sessionId){
		return RESTClient.DELETE(RECORDER_BASE_URL + "/api/v1/sessions/" + sessionId);
	}

	public void deleteAllSessions(){
		String activeSessions = getActiveSessions();
		Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
		Matcher matcher = pattern.matcher(activeSessions);

		List<String> resultado = new ArrayList<>();
		while (matcher.find()){
			  resultado.add(matcher.group(1));
		}

		for (String s: resultado){
			deleteSession(s);
		}
	}

	public WebDriver startRecordingAndSetupChromeDriver(ChromeOptions opts) {
		WebDriver driver = null;
		
		// Start Recording Session
		Boolean sessionStarted = startNewSession();
		if(!sessionStarted) {
			System.out.println("could not start recording session");
		}

		
		// Setup Chrome Driver
		if(this.recordingProxyPort.isEmpty() || this.recordingSessionId.isEmpty()) {
			System.out.println("recording session has a problem, id or proxy port is empty - returning normal ChromeDriver");
			driver = new ChromeDriver(opts);
		} else {
			// initialize the proxy with the proxy port returned by the Parasoft Recorder API
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(RECORDER_HOST+":"+recordingProxyPort);
			proxy.setSslProxy(RECORDER_HOST+":"+recordingProxyPort);
			
			// tell Selenium to set the UI to use the Proxy
			opts.setCapability("proxy", proxy);
			
			driver = new ChromeDriver(opts);
		}
		
		return driver;
	}
	
	public void stopRecordingAndCreateTST(String testName) {
		Boolean sessionStopped = stopSession();
		if(sessionStopped) {
			Boolean trafficSent = sendTrafficToSOAtest(testName);
			if(trafficSent) {
				Boolean sessionEnded = endRecordingSession();
				if(!sessionEnded) {
					System.out.println("session stopped and traffic sent, but session could not be ended");
				}
			} else {
				System.out.println("session stopped but traffic was not sent");
			}
		} else {
			System.out.println("session could not be stopped, traffic was not sent");
		}
	}
	
	public String getSOAtestHost() {
		return this.SOATEST_HOST;
	}
	
	public String getSOAtestPort() {
		return this.SOATEST_PORT;
	}

	public String getRecorderBaseURL() {
		return this.RECORDER_BASE_URL;
	}

	public String getRecordingSessionId() {
		return this.recordingSessionId;
	}
	
	public String getRecordingProxyPort() {
		return this.recordingProxyPort;
	}
	
	public void setRecordingSessionId(String id) {
		this.recordingSessionId = id;
	}
	
	public void setRecordingProxyPort(String port) {
		this.recordingProxyPort = port;
	}

	
	public Boolean startNewSession() {
		Boolean sessionStarted = false;
		JSONObject responseObject = null;

		System.out.println("starting new session");
		
		// Start new session
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			try {
				String response = RESTClient.POST("http://localhost:40090/api/v1/sessions","{\"soavirt\":{\"host\":\""+SOATEST_HOST+"\",\"port\":\""+SOATEST_PORT+"\",\"secure\":false}}");


				if(response.contains("started")) {
					System.out.println("POST /api/v1/sessions - 200 response");
					sessionStarted = true;

					responseObject = new JSONObject(response);
					
					// set sessionId and proxyPort
					setRecordingSessionId(responseObject.get("id").toString());
					setRecordingProxyPort(responseObject.getJSONObject("proxySettings").get("port").toString());

					System.out.println("POST /api/v1/sessions response");
					System.out.println(responseObject.toString());

				} else {
					System.out.println("POST /api/v1/sessions - " + response + " response");
					System.out.println(response.toString());

				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("could not execute POST /api/v1/sessions");
			}
		} 
		catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("could not create CloseableHttpClient");
		}
		
		return sessionStarted;
	}
	
	private Boolean stopSession() {
		Boolean isStopped = false;

		System.out.println("stopping session");
		
		// Put call that stops the recording session
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			HttpPut httpPut = new HttpPut(RECORDER_BASE_URL + "/api/v1/sessions/" + this.recordingSessionId);
			
			StringEntity requestEntity = new StringEntity(
					"{\"state\":\"stopped\"}",
					ContentType.APPLICATION_JSON);
			httpPut.setEntity(requestEntity);
						
			try {
				response = httpClient.execute(httpPut);
				
				if(String.valueOf(response.getCode()).equals("200")) {
					isStopped = true;
					System.out.println("PUT /api/v1/sessions/" + this.recordingSessionId + " - 200 response");
				} else {
					System.out.println("PUT /api/v1/sessions/" + this.recordingSessionId + " - " + String.valueOf(response.getCode()) + " response");
					System.out.println(response.toString());
					
					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					System.out.println(responseString);
				}

				EntityUtils.consume(response.getEntity());
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("could not execute PUT /api/v1/sessions/" + this.recordingSessionId);
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("could not close CloseableHttpResponse");
					}
				}
			}
		} 
		catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("could not create CloseableHttpClient");
		}
		
		return isStopped;
	}
	
	private Boolean sendTrafficToSOAtest(String testName) {
		Boolean success = false;
		JsonObject object = null;

		System.out.println("sending API traffic to SOAtest");
		
		// Post call that sends recorded API traffic to SOAtest Server
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			HttpPost httpPost = new HttpPost(RECORDER_BASE_URL + "/api/v1/sessions/" + this.recordingSessionId + "/tsts");
			
			StringEntity requestEntity = new StringEntity(
					"{"
						+ "\"name\":\""+testName+"\""
					+ "}", ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
			
			String request = "";
			try {
				System.out.println("POST /api/v1/sessions/" + this.recordingSessionId + "/tsts - request body");
				request = EntityUtils.toString(requestEntity);
				System.out.println(request);
			} catch (ParseException e1) {
				e1.printStackTrace();
				System.out.println("could not parse request");
			}
			
			try {
				response = httpClient.execute(httpPost);
				
				if(String.valueOf(response.getCode()).equals("200")) {
					success = true;
					System.out.println("POST /api/v1/sessions/" + this.recordingSessionId + "/tsts - 200 response");
				} else {
					System.out.println("POST /api/v1/sessions/" + this.recordingSessionId + "/tsts - " + String.valueOf(response.getCode()) + " response");
					System.out.println(response.toString());
					
					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					System.out.println(responseString);
				}
				
				EntityUtils.consume(response.getEntity());
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("could not execute POST /api/v1/sessions/" + this.recordingSessionId + "/tsts");
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("could not close CloseableHttpResponse");
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("could not create CloseableHttpClient");
		}
		
		return success;
	}
	
	private Boolean endRecordingSession() {
		Boolean isEnded = false;

		System.out.println("ending recording session");
		
		// Delete call that ends the recording session
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = null;
			HttpDelete httpDelete = new HttpDelete(RECORDER_BASE_URL + "/api/v1/sessions/" + this.recordingSessionId);

			try {
				response = httpClient.execute(httpDelete);
				
				if(String.valueOf(response.getCode()).equals("200")) {
					isEnded = true;
					System.out.println("DELETE /api/v1/sessions/" + this.recordingSessionId + " - 200 response");
				} else {
					System.out.println("DELETE /api/v1/sessions/" + this.recordingSessionId + " - " + String.valueOf(response.getCode()) + " response");
					System.out.println(response.toString());
					
					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity);
					System.out.println(responseString);
				}
				
				EntityUtils.consume(response.getEntity());
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("could not execute DELETE /api/v1/sessions/" + this.recordingSessionId);
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("could not close CloseableHttpResponse");
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("could not create CloseableHttpClient");
		}
		
		return isEnded;
	}
}