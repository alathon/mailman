package com.cphse.mailman.fetchers;

import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.cphse.dto.RawMail;
import com.cphse.mailman.connection.ConnectionProtocol;
import com.cphse.mailman.connection.MailConnection;
import com.cphse.mailman.connection.MailConnectionDetails;
import com.cphse.mailman.connection.MailConnectionPool;

public abstract class MailFetcher {
	protected static int DEFAULT_FETCH_AMOUNT = 100;
	
	protected int maxFetchAmount;
	protected final MailConnectionDetails connectionDetails;
	protected final MailConnection connection;
	protected final Properties properties;

	private static Properties getProtocolProperties(MailConnectionDetails details) {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", details.mailProtocol);
		props.setProperty("mail.imaps.connectionpooltimeout", "30000");
		props.setProperty("mail.imaps.connectiontimeout", "30000");
		props.setProperty("mail.imaps.timeout", "30000");
		props.setProperty("mail.host", details.mailHost);
		props.setProperty("mail.user", details.mailUser);
		return props;
	}

	public MailFetcher(MailConnectionDetails details) {
		this(details, MailFetcher.getProtocolProperties(details));
	}

	public MailFetcher(MailConnectionDetails details, Properties props) {
		this(details, props, MailFetcher.DEFAULT_FETCH_AMOUNT);
	}

	public MailFetcher(MailConnectionDetails details, Properties properties, int fetchAmt) {
		this.connectionDetails = details;
		this.properties = properties;
		this.maxFetchAmount = fetchAmt;
		this.connection = MailConnectionPool.getConnection(details, properties);
	}

	protected Folder getDefaultFolder() throws MessagingException {
		return this.connection.getFolder(this.connectionDetails.mailDefFolder);
	}

	public boolean isConnected() {
		return this.connection.isConnected();
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws MessagingException 
	 */
	public static void main(String[] args) throws InterruptedException, MessagingException {
		String host = "host";
		int port = 993;
		String user = "mailaddress@place.com";
		String pass = "n1";
		MailConnectionDetails details = new MailConnectionDetails(host, port, user, pass, ConnectionProtocol.IMAPS, "INBOX");
		
		IMAPMailFetcher fetcher = new IMAPMailFetcher(details);
		Message[] msgs = fetcher.getMessages(0);
		fetcher.fetchHeaders(msgs);
		List<RawMail> rawMails = fetcher.getRawMails(msgs);
		for(RawMail mail : rawMails) {
			System.out.println(String.format("ID: %s Subject: %s\nBody: %s",
					mail.getServerId(), mail.getSubject(), mail.getBody()));
		}
	}

}
