package ambari.configs;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class ServerHostnameVerifier implements HostnameVerifier{

	public boolean verify(String arg0, SSLSession arg1) {
		// TODO Auto-generated method stub
		return true;
	}

}
