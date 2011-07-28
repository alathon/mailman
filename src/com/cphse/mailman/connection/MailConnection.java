package com.cphse.mailman.connection;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

public class MailConnection implements ConnectionListener{
	private Store mailStore;
	private Session mailSession;
	private Properties properties;
	private MailConnectionDetails details;

	@Override
	public String toString() {
		return details.toString();
	}

	public Folder getFolder(String name) throws MessagingException {
		return mailStore.getFolder(name);
	}

	private void setup() throws MessagingException {
		mailSession = Session.getDefaultInstance(properties);
		mailStore = mailSession.getStore(properties.getProperty("mail.store.protocol").toLowerCase());
		mailStore.addConnectionListener(this);
		mailStore.connect(details.mailHost, details.mailPort, details.mailUser, details.mailPassword);
	}

	protected MailConnection(MailConnectionDetails details, Properties props) throws MessagingException {
		this.properties = props;
		this.details = details;
		setup();
	}

	public boolean isConnected() {
		return this.mailStore.isConnected();
	}

	@Override
	public void closed(ConnectionEvent arg0) {
		MailConnectionPool.instance().closed(this);
		
	}

	@Override
	public void disconnected(ConnectionEvent arg0) {
		MailConnectionPool.instance().disconnected(this);
		
	}

	@Override
	public void opened(ConnectionEvent arg0) {
		MailConnectionPool.instance().opened(this);
		
	}
}
