package com.benblamey.core;

import com.benblamey.core.ExceptionHandler;
import com.benblamey.core.SystemInfo;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class Email {

	public static final String GILES_EMAIL_ADDRESS = "giles.oatley@????";
	public static final String BEN_EMAIL_ADDRESS = "ben"+"@"+"benblamey"+"."+"com";

    public static void send(final String to, String subject, String body, String fromName) throws MessagingException {
    	send(new ArrayList<String>() {{ add(to); }},subject,body,fromName);
    }

    public static void send(List<String> tos, String subject, String body, String fromName) throws MessagingException {

        // Sender's email ID needs to be mentioned
        InternetAddress from;
        InternetAddress replyto;
        try {
            from = new InternetAddress("noreply@" + SystemInfo.getEmailDomainName(), fromName);
            replyto = new InternetAddress(BEN_EMAIL_ADDRESS, "Ben Blamey");

        } catch (UnsupportedEncodingException ex) {
            // Static exception.
            throw new RuntimeException(ex);
        }

        // Assuming you are sending email from localhost
        String host = "localhost";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(from);

        message.setReplyTo(new Address[] {replyto});

        boolean benInTo = false;
        for (String to : tos) {
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));

            if (!to.toLowerCase().equals(BEN_EMAIL_ADDRESS)) {
            	benInTo = true;
                // DON'T BREAK HERE.
            }

        }

        if (!benInTo) {
        	// Mail not being send to me, so BCC myself.
        	try {
				message.setRecipient(RecipientType.BCC, new InternetAddress(BEN_EMAIL_ADDRESS, "Ben Blamey"));
			} catch (UnsupportedEncodingException e) {
	            // Static exception.
	            throw new RuntimeException(e);
			}
        }

        // Set Subject: header field
        message.setSubject(subject);

        // Now set the actual message
        message.setText(body);

        // Send message
        Transport.send(message);
    }

    public static void sendExceptionReport(Throwable exception) throws MessagingException {
    	sendExceptionReport(exception, "<empty>");
    }

    public static void sendExceptionReport(Throwable exception, String message) throws MessagingException {

    	if (SystemInfo.doesServerSendEmail()) {
	    	String subject = "Unhandled exception on server: " + SystemInfo.detectServer().toString();
	    	String body = message;

	    	body += "\n\n";

	    	body += "Exception summary: \n\n";

	    	body += ExceptionHandler.getDetailedExceptionSummary(exception);


	    	ArrayList<String> tos = new ArrayList<>();

	    	tos.add(BEN_EMAIL_ADDRESS);

	    	Email.send(tos, subject, body, "noreply");

    	} else {
    		System.err.println("Cannot email exception report - this server is not known to send email.");
    	}


    }
}
