package com.cphse.mailman.connection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public final class MailConnectionDetails implements Serializable{
	private static final long serialVersionUID = -630780385887491187L;

	public final String mailHost;
	public final String mailUser;
	public final String mailPassword;
	public final String mailProtocol;
	public final String mailDefFolder;
	public final int mailPort;
	
	@Override
	public String toString() {
		return String.format("%s@%s", this.mailUser, this.mailHost);
	}

	public MailConnectionDetails(String fileName) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(fileName));
		this.mailHost = props.getProperty("host");
		this.mailPort = Integer.parseInt(props.getProperty("port"));
		this.mailUser = props.getProperty("user");
		this.mailPassword = props.getProperty("password");
		this.mailDefFolder = props.getProperty("defaultFolder");
		this.mailProtocol = props.getProperty("protocol");
	}

	public MailConnectionDetails(String mailHost, 
			int mailPort, String mailUser, String mailPassword, 
			ConnectionProtocol protocol, String defaultFolder) {
		this.mailHost = mailHost;
		this.mailPort = mailPort;
		this.mailUser = mailUser;
		this.mailPassword = mailPassword;
		this.mailProtocol = protocol.name().toLowerCase();
		this.mailDefFolder = defaultFolder;
	}
}
