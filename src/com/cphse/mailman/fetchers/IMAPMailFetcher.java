package com.cphse.mailman.fetchers;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

import com.cphse.mailman.connection.MailConnectionDetails;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.FetchProfileItem;

public class IMAPMailFetcher extends MailFetcher {

	public IMAPMailFetcher(MailConnectionDetails details) {
		super(details);
	}

	private FetchProfile getFetchProfile() {
		FetchProfile prof = new FetchProfile();
		prof.add(FetchProfileItem.HEADERS);
		prof.add(UIDFolder.FetchProfileItem.UID);
		return prof;
	}

	public Message[] getMessages(long lastUID) throws MessagingException {
		IMAPFolder defaultFolder = (IMAPFolder)this.getDefaultFolder();
		if(!defaultFolder.isOpen()) defaultFolder.open(Folder.READ_ONLY);
		return defaultFolder.getMessagesByUID(lastUID+1, lastUID + this.maxFetchAmount);
	}
	
	public void fetchHeaders(Message[] msgs) throws MessagingException {
		FetchProfile prof = this.getFetchProfile();
		IMAPFolder defaultFolder = (IMAPFolder)this.getDefaultFolder();
		if(!defaultFolder.isOpen()) defaultFolder.open(Folder.READ_ONLY);
		defaultFolder.fetch(msgs, prof);
	}

}
