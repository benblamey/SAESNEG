package benblamey.tests;

import benblamey.core.Email;
import javax.mail.MessagingException;

public class EmailTest {

    // cd /var/lib/tomcat7/webapps/benblamey.evaluation/WEB-INF
    // java -classpath classes:lib/*:/var/lib/tomcat7/shared/lib/*:/var/lib/tomcat7/shared/lib/gate-lib/* benblamey.tests.EmailTest
    public static void main(String[] args) throws MessagingException {
        Email.send("blamey.ben"+"@"+"gmail.com", "Test Email 1 of 2", "Test email.", "noreply@socialworld.co.uk");
        Email.send("ben"+"@"+"benblamey.com", "Test Email 2 of 2", "Test email.", "noreply@socialworld.co.uk");
    }
}
