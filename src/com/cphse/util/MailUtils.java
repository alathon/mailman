package com.cphse.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.activation.UnsupportedDataTypeException;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import com.cphse.dto.EmailAddress;
import com.cphse.dto.EmailRecipient;
import com.cphse.dto.EmailRecipient.RecipientType;
import com.cphse.dto.RawMail;
import com.sun.mail.imap.IMAPInputStream;
import com.sun.mail.imap.IMAPNestedMessage;

public class MailUtils {
	   /**
     * Creates a Mail
     * @param inboxHelper
     * @param message
     * @param mUId
     * @return
	 * @throws FolderClosedException 
     * @throws MessagingException
     * @throws IOException
     */
    public static RawMail createMail(Message message) throws FolderClosedException{
        try {
            // fill the message objects with messagedata
            String[] fromInfo = MailUtils.getFromInfo(message.getFrom());

			if (fromInfo[0] == null || fromInfo[0].isEmpty()) {
				fromInfo = MailUtils.getFromInfo(message.getReplyTo());
			}
			if (fromInfo[0] == null || fromInfo[0].isEmpty()) {
				String[] returnPath = message.getHeader("Return-Path");
				if (returnPath.length > 0) {
					fromInfo[0] = returnPath[0].trim();
					if(fromInfo[0].charAt(0) == '<') 
						fromInfo[0].substring(1);
					if(fromInfo[0].charAt(fromInfo[0].length()-1) == '>')
						fromInfo[0].substring(0, fromInfo[0].length()-2);
				}
			}

			String mFromAddress = (fromInfo[0] != null) ? fromInfo[0] : "";
			RawMail mail = new RawMail();
            //Note: Mail ids are generated based on contents - do not set an id

            String mSubject = message.getSubject();
            mail.setSubject(mSubject);
			mail.setFrom(new EmailAddress(mFromAddress, fromInfo[1]));
            mail.setCreated(new Date());

			// Prevent evil mail servers from sending from the future
			Date now = new Date();
			Date sentDate = message.getSentDate();
			Date recDate = message.getReceivedDate();
			
			if(sentDate == null || now.before(sentDate)) sentDate = new Date();
			if(recDate == null || now.before(recDate)) recDate = new Date();

			mail.setSentDate(sentDate);
            mail.setRecievedDate(recDate);
			
            mail.setServerId(String.valueOf(message.getMessageNumber()));

			try {
				mail.setBody(MailUtils.getBody(message));
			} catch(Throwable ex) {
				System.out.println("Failed when reading mail body");
			}
			
            // Headers
            Enumeration<?> allHeaders = message.getAllHeaders();
            HashMap<String, String> mailHeaders = new HashMap<String, String>();
            while (allHeaders.hasMoreElements()) {
                Header header = (Header) allHeaders.nextElement();
                mailHeaders.put(header.getName(), header.getValue());
            }
			mail.setHeaders(mailHeaders);

			try {
				mail.setRecipients(MailUtils.getRecipients(Message.RecipientType.TO, message));
			} catch(Throwable ex) {
				System.out.println("Throwable during setRecipients");
			}
			try {
				mail.setRecipients(MailUtils.getRecipients(Message.RecipientType.CC, message));
			} catch(Throwable ex) {
				System.out.println("Throwable during setRecipients");
			}
			try {
				mail.setRecipients(MailUtils.getRecipients(Message.RecipientType.BCC, message));
			} catch(Throwable ex) {
				System.out.println("Throwable during setRecipients");
			}
            return mail;
        } catch (FolderClosedException ex) {
			throw ex;
		} catch (Throwable ex) {
			System.out.println("Could not read mail");
        }
        return null;
    }	

    public static String[] getFromInfo(Address[] messageAddresses) {
        String info[] = new String[3];
        if (messageAddresses != null) {
            Address firstMessageAddress = messageAddresses[0];
            if (firstMessageAddress instanceof InternetAddress) {
                info[0] = ((InternetAddress) firstMessageAddress).getAddress();
                if (info[0] == null) {
                    info[0] = "";
                }
                info[1] = ((InternetAddress) firstMessageAddress).getPersonal();
                if (info[1] == null) {
                    info[1] = "";
                }
                info[2] = info[1] + " " + info[0];
            }
        }
        return info;
    }

    public static String getBody(Message message) throws Exception {
		try {
            message.getContent(); //This might throw an exception...
        } catch(Throwable ex) {
            try {
                //Content could not be resolved for some reason - just get the raw stuff
                return ConvertUtil.inputStreamToString(message.getInputStream());
            } catch(Throwable ex2) {
                //bodystructure is corrupt - get the really raw stuff
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                message.writeTo(out);
                return out.toString();
            }
        }

        // check if message is a normal message or a Multipart message
        if (message.getContent() instanceof MimeMultipart) {
            MultipartResult res = new MailUtils.MultipartResult();
            MailUtils.readMultiPart(res, (MimeMultipart) message.getContent());
            return res.body.isEmpty() ? res.bodyHtml : res.body;
        } else {
            if (message.getContent().toString() != null) {
                return message.getContent().toString();
            }
        }
		return null;
    }

    private static class MultipartResult {
    	public String body;
    	public String bodyHtml;
    }
    
	private static void readMultiPart(MultipartResult res, MimeMultipart multipart) throws MessagingException {
		for(int i = 0; i < multipart.getCount();i++ ) {
			BodyPart part = multipart.getBodyPart(i);
			
			try {
				if (part.isMimeType("image/*") || part.isMimeType("application/*"))
					continue;
				Object content = null;
				try {
					content = part.getContent();
				} catch(UnsupportedEncodingException ex) {
					String body = ConvertUtil.inputStreamToString(part.getInputStream());
					res.body = body;
					continue;
				}
				
				if (part.isMimeType("text/plain")) {                        
					res.body = content.toString();
				} else if (part.isMimeType("text/*")) {
					res.bodyHtml = content.toString();
				} else if (content instanceof MimeMultipart) {
					readMultiPart(res,(MimeMultipart)content);
				} else  if (content instanceof IMAPNestedMessage) {
					res.body = getBody((IMAPNestedMessage)content);
					return;
				} else if (content instanceof InputStream) {
					if (content instanceof IMAPInputStream) {
						String body = ConvertUtil.inputStreamToString(part.getInputStream());
						res.body = body;
					} else 
						System.out.println(String.format("Ignoring binary content in mail: %s [%s]",part.getContentType(),content.getClass()));
				} else if (part.isMimeType("message/*")) {
					res.body = content.toString();
				} else {
					System.out.println(String.format("Unknown content type in mail: %s [%s]",part.getContentType(),content.getClass()));
				}
				
			} catch(IllegalStateException ex) {
				System.out.println(String.format("Could not read contents in mail: %s",part.getContentType()));
			} catch(UnsupportedDataTypeException ex) {
				System.out.println(String.format("Could not read contents in mail: %s",part.getContentType()));
			} catch(FolderClosedException ex) {
				throw ex;
			} catch(Throwable ex) {
				System.out.println(String.format("Error while reading mail part: %s",part.getClass().toString()));
			}
		}
	}

    public static Set<EmailRecipient> getRecipients(Message.RecipientType type, Message message) throws MessagingException {
		HashSet<EmailRecipient> out = new HashSet<EmailRecipient>();
    	try {
			if (message.getRecipients(type) != null) {
				for (Address recipient : message.getAllRecipients()) {
					String name = "";
					String address = "";
					if (recipient instanceof InternetAddress) {
						if (((InternetAddress) recipient).getPersonal() != null) {
							name = ((InternetAddress) recipient).getPersonal();
						}
						if (((InternetAddress) recipient).getAddress() != null) {
							address = ((InternetAddress) recipient).getAddress();
						}
						RecipientType newType = RecipientType.TO;
						if (type.equals(Message.RecipientType.CC)) {
							newType = RecipientType.CC;
						} else if (type.equals(Message.RecipientType.BCC)) {
							newType = RecipientType.BCC;
						}
						out.add(new EmailRecipient(address, name,newType));
					}
				}
			}
		} catch (AddressException ex) {
			//Do nothing - illegal formatting in recipient field
		}
    	return out;
    }
}
