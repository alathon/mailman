package com.cphse.mailman.fetchers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;

import com.cphse.mailman.connection.MailConnectionDetails;
import com.sun.mail.imap.IMAPFolder.FetchProfileItem;
import com.sun.mail.pop3.POP3Folder;

public class POP3MailFetcher extends MailFetcher{

	public POP3MailFetcher(MailConnectionDetails details) {
		super(details);
	}

	private FetchProfile getFetchProfile() {
		FetchProfile prof = new FetchProfile();
		prof.add(FetchProfileItem.HEADERS);
		return prof;
	}

	public Message[] getMessages(Date lastFetchDate) throws MessagingException {
		POP3Folder defaultFolder = (POP3Folder)this.getDefaultFolder();
		if(!defaultFolder.isOpen()) defaultFolder.open(Folder.READ_ONLY);

        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH,-1);
        Date aMonthAgo = cal.getTime();
		SearchTerm newer = new SentDateTerm(ComparisonTerm.GT, lastFetchDate);
		Message[] msgs = defaultFolder.search(newer);

		if(msgs.length > this.maxFetchAmount) {
			newer = new SentDateTerm(ComparisonTerm.GT, aMonthAgo);
			msgs = defaultFolder.search(newer);
		}

		if(msgs.length > this.maxFetchAmount) {
            cal = new GregorianCalendar();
            cal.add(Calendar.WEEK_OF_YEAR,-2);
            Date twoWeeksAgo = cal.getTime();
			newer = new SentDateTerm(ComparisonTerm.GT, twoWeeksAgo);
			msgs = defaultFolder.search(newer);
		}
		
		return msgs;
	}
	
	@Override
	public void fetchHeaders(Message[] msgs) throws MessagingException {
		FetchProfile prof = this.getFetchProfile();
		POP3Folder defaultFolder = (POP3Folder)this.getDefaultFolder();
		if(!defaultFolder.isOpen()) defaultFolder.open(Folder.READ_ONLY);
		defaultFolder.fetch(msgs, prof);
	}
}
