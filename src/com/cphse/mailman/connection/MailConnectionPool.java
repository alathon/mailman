package com.cphse.mailman.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.MessagingException;

public class MailConnectionPool {
	private static MailConnectionPool _instance;

	private Map<String, MailConnection> connections;

	public MailConnectionPool() {
		this.connections = new HashMap<String, MailConnection>();
	}

	private MailConnection addConnection(MailConnectionDetails details, Properties props) {
		MailConnection connection = null;
		try {
			connection = new MailConnection(details, props);
		} catch (MessagingException e) {
			e.printStackTrace();
			return null;
		}

		return connection;
	}

	public synchronized static MailConnectionPool instance() {
		if(_instance == null)
			_instance = new MailConnectionPool();
		return _instance;
	}

	public static MailConnection getConnection(MailConnectionDetails details, Properties props) {
		MailConnectionPool pool = MailConnectionPool.instance();

		for(String s : pool.connections.keySet()) {
			if(s.equals(details.toString())) return pool.connections.get(s);
		}

		return pool.addConnection(details, props);
	}

	public void closed(MailConnection conn) {
		System.out.println("Closed" + conn.toString());
		this.connections.remove(conn.toString());
	}

	public void disconnected(MailConnection conn) {
		System.out.println("Disconnected" + conn.toString());
		this.connections.remove(conn.toString());
	}

	public void opened(MailConnection conn) {
		System.out.println("Connected " + conn.toString());
		this.connections.put(conn.toString(), conn);
	}
	
}
