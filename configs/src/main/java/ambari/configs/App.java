package ambari.configs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;



/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args ) throws Exception
	{
		//String action=null;
		String hostname=null;
		String port=null;
		String clustername=null;
		boolean isSSL=false;
		//boolean isBackup=true;
		String username=null;
		String password=null;
		BufferedReader inputReader=new BufferedReader(new InputStreamReader(System.in));
		if(args.length<6&&args.length>0)
		{
			System.out.println("Incorrect Usage: java -jar configs.jar <ambari server> <port> <ambari cluster> <ssl> <username> <password>");
			//System.out.println("action is backup or restore");
			System.out.println("ambari server is the hostname of Ambari Server");
			System.out.println("ambari cluster is the Ambari cluster name");
			System.out.println("port is Ambari server port");
			System.out.println("ssl if ssl enabled (true/false)");
			System.out.println("username amd password for ambari server");
			System.exit(1);
		}
		else if(args.length==0)
		{
			System.out.println("Enter ambari hostname:");
			hostname=inputReader.readLine();
			System.out.println("Enter ambari port:");
			port=inputReader.readLine();
			System.out.println("Enter ambari cluster name:");
			clustername=inputReader.readLine();
			System.out.println("Is SSL Enabled? (true or false)");
			isSSL=Boolean.parseBoolean(inputReader.readLine());
			System.out.println("Enter ambari username:");
			username=inputReader.readLine();
			System.out.println("Enter ambari password:");
			password=inputReader.readLine();
		}
		else
		{

			hostname=args[0];
			port = args[1];
			clustername=args[2];
			isSSL = Boolean.parseBoolean(args[3]);
			username = args[4];
			password = args[5];
		}

		String tempuri="";
		tempuri+=(isSSL)?"https://":"http://";
		tempuri+=hostname+":"+port+"/api/v1/clusters/"+clustername;

		final String uri=tempuri;
	
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			public String handleResponse(
					final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}

		};
		HttpClient client=ConnectionFactory.getConnection(username, password);
		HttpGet get=new HttpGet(uri+"?fields=Clusters/desired_configs");
		get.addHeader(BasicScheme.authenticate(
				new UsernamePasswordCredentials(username,password),
				"UTF-8", false));
		String response=client.execute(get,responseHandler);
		get.completed();
		//System.out.println(response);
		BufferedWriter writer=new BufferedWriter(new FileWriter("configs_detected.txt"));
		JSONObject jobj=new JSONObject(response);
		JSONObject clusters=jobj.getJSONObject("Clusters");
		JSONObject desired_configs=clusters.getJSONObject("desired_configs");
		Iterator<String> iter=desired_configs.keys();
		while(iter.hasNext())
		{
			String nameOfConfig=iter.next();
			String tag=desired_configs.getJSONObject(nameOfConfig).getString("tag");
			writer.write(nameOfConfig+"\n");
			get=new HttpGet(uri+"/configurations?type="+nameOfConfig+"&tag="+tag);
			get.addHeader(BasicScheme.authenticate(
					new UsernamePasswordCredentials(username,password),
					"UTF-8", false));
			String configBody=client.execute(get,responseHandler);
			String processedJson="";
			JSONObject configJSON=new JSONObject(configBody);
			JSONArray jsonArray=configJSON.getJSONArray("items");
			for(int i=0;i<jsonArray.length();i++)
			{
				JSONObject properties=(JSONObject)jsonArray.get(i);
				if(properties.has("properties"))
					processedJson+="\"properties\":"+properties.getJSONObject("properties").toString();
				if(properties.has("properties_attributes"))
				{
					if(processedJson.charAt(processedJson.length()-1)=='}')
					{
						processedJson+=",";
						processedJson+="\"properties_attributes\":"+properties.getJSONObject("properties_attributes").toString();
					}
				}

			}
			BufferedWriter configWriter=new BufferedWriter(new FileWriter(nameOfConfig+System.currentTimeMillis()+".json"));
			configWriter.write(processedJson);
			configWriter.flush();
			configWriter.close();
			System.out.println(processedJson);
			System.out.println("Configuration for "+nameOfConfig+" is saved successfully");

		}
		writer.flush();
		writer.close();
		//		}
		//		else
		//		{
		//			final HttpClient client=ConnectionFactory.getConnection(username, password);
		//			//CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		//			BufferedReader reader=new BufferedReader(new FileReader("configs_detected.txt"));
		//			 String line="";
		//			while((line=reader.readLine())!=null)
		//			{
		//				final String lineread=line;
		//				
		//				//line=reader.readLine();
		//				String newTag="version"+System.nanoTime();
		//				String content;
		//				
		//					content = new String(Files.readAllBytes(Paths.get(lineread+".json")));
		//				
		//				String finalJson="{ \"Clusters\": { \"desired_config\": {\"type\": \""+lineread+"\", \"tag\":\""+newTag+"\",\"properties\":"+ content+"}}}";
		//				
		//			//	JSONObject jsonObject=new JSONObject(finalJson);
		//			//	System.out.println(finalJson.toString());
		//				HttpPut putRequest = new HttpPut(uri);
		//				//putRequest.
		//				putRequest.addHeader(BasicScheme.authenticate(
		//						new UsernamePasswordCredentials(username,password),
		//						"UTF-8", false));
		//				StringEntity requestEntity;
		//				
		//					requestEntity = new StringEntity(
		//							finalJson);
		//				
		//				putRequest.addHeader("X-Requested-By","ambari");
		//				putRequest.setEntity(requestEntity);
		//				HttpResponse rawResponse=null;
		//				try{
		//				rawResponse = client.execute(putRequest);
		//				}
		//				catch(ConnectionPoolTimeoutException connectexception)
		//				{
		//					System.out.println("Could not update configuration for "+lineread);
		//					System.out.println("Connection timeout");
		//					putRequest.releaseConnection();
		//					continue;
		//				}
		//				if(rawResponse.getStatusLine().getStatusCode()==200)
		//				{
		//					System.out.println("Configuration for "+lineread+" updated successfully");
		//				}
		//				else
		//				{
		//					System.out.println("Could not update configuration for "+lineread);
		//					System.out.println("Return code:"+rawResponse.getStatusLine().getStatusCode());
		//				}
		//					
		//				putRequest.completed();
		//				putRequest.releaseConnection();
		//				Thread.currentThread().sleep(6000);
		//			}
		//			reader.close();
		//		}



	}
}
