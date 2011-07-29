package com.cphse.tasks;

import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.cphse.dto.RawMail;
import com.cphse.mailman.connection.MailConnectionDetails;
import com.cphse.mailman.fetchers.IMAPMailFetcher;
import com.cphse.mailman.fetchers.MailFetcher;
import com.cphse.mailman.fetchers.POP3MailFetcher;
import com.cphse.orders.MailAccountOrder;
import com.cphse.queue.Task;

public class MailAccountTask extends Task<MailAccountOrder>{
	private MailConnectionDetails details;

	public MailAccountTask(MailAccountOrder order) {
		super(order);
		this.details = order.details;
	}

	@Override
	public void run() {
		try {
			MailFetcher fetcher = MailFetcher.create(this.details);
			Message[] msgs = null;
			if(fetcher instanceof POP3MailFetcher) {
				msgs = ((POP3MailFetcher) fetcher).getMessages(new Date(0));
			} else if(fetcher instanceof IMAPMailFetcher) {
				msgs = ((IMAPMailFetcher) fetcher).getMessages(0);
			} else {
				System.out.println("ERROR: Unable to create proper MailFetcher from protocol.");
			}

			fetcher.fetchHeaders(msgs);
			List<RawMail> rawMails = fetcher.getRawMails(msgs);
			for(RawMail mail : rawMails) {
				System.out.println(String.format("ID: %s Subject: %s\nBody: %s",
						mail.getServerId(), mail.getSubject(), mail.getBody()));
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}	
	}
}
