package ambari.configs;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;



public  class ConnectionFactory {
	
public static HttpClient  getConnection(String username,String password) throws Exception
{
	
	 SSLContext ctx = SSLContext.getInstance("TLS");
     X509TrustManager tm = new X509TrustManager() {
         public X509Certificate[] getAcceptedIssuers() {
             return null;
         }

         public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
         }

         public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
         }
     };
     ctx.init(null, new TrustManager[] { (TrustManager) tm }, null);
     SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx,new ServerHostnameVerifier());
     PlainConnectionSocketFactory plainConn = new PlainConnectionSocketFactory();
     Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory> create()
         .register("https", ssf).register("http", plainConn).build();
     BasicCredentialsProvider basicProvider=new BasicCredentialsProvider();
     basicProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			
     HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);  
	
	 HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(7000).build()).setDefaultCredentialsProvider(basicProvider).setConnectionManager(cm).build();
	 return client;

}
}
