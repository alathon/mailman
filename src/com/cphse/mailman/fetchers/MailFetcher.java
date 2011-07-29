package com.cphse.mailman.fetchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.cphse.dto.RawMail;
import com.cphse.mailman.connection.MailConnection;
import com.cphse.mailman.connection.MailConnectionDetails;
import com.cphse.mailman.connection.MailConnectionPool;
import com.cphse.util.MailUtils;

public abstract class MailFetcher {
	protected static int DEFAULT_FETCH_AMOUNT = 100;
	
	protected int maxFetchAmount;
	protected final MailConnectionDetails connectionDetails;
	protected final MailConnection connection;
	protected final Properties properties;

	private static boolean isIMAP(MailConnectionDetails details) {
		return (details.mailProtocol.equalsIgnoreCase("imap") ||
				details.mailProtocol.equalsIgnoreCase("imaps"));
	}
	
	private static boolean isPOP3(MailConnectionDetails details) {
		return (details.mailProtocol.equalsIgnoreCase("pop3") ||
				details.mailProtocol.equalsIgnoreCase("pop3s"));
	}

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

	protected MailFetcher(MailConnectionDetails details) {
		this(details, MailFetcher.getProtocolProperties(details));
	}

	protected MailFetcher(MailConnectionDetails details, Properties props) {
		this(details, props, MailFetcher.DEFAULT_FETCH_AMOUNT);
	}

	protected MailFetcher(MailConnectionDetails details, Properties properties, int fetchAmt) {
		this.connectionDetails = details;
		this.properties = properties;
		this.maxFetchAmount = fetchAmt;
		this.connection = MailConnectionPool.getConnection(details, properties);
	}

	protected Folder getDefaultFolder() throws MessagingException {
		return this.connection.getFolder(this.connectionDetails.mailDefFolder);
	}

	public static MailFetcher create(MailConnectionDetails details) {
		if(MailFetcher.isIMAP(details)) {
			return new IMAPMailFetcher(details);
		} else if(MailFetcher.isPOP3(details)) {
			return new POP3MailFetcher(details);
		} else {
			return null;
		}
	}

	public List<RawMail> getRawMails(Message[] msgs) {
		ArrayList<RawMail> mails = new ArrayList<RawMail>();
		for(Message msg : msgs) {
			System.out.println("Fetching message " + msg.getMessageNumber());
			try {
				RawMail mail = MailUtils.createMail(msg);
				mails.add(mail);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mails;
	}

	public boolean isConnected() {
		return this.connection.isConnected();
	}
	
	public abstract void fetchHeaders(Message[] msgs) throws MessagingException;
}
