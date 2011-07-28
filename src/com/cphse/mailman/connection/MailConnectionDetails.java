package com.cphse.mailman.connection;

public final class MailConnectionDetails implements Cloneable{
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
