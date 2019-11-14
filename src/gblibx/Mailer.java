
package gblibx;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class Mailer {
    public static class Credentials {
        public Credentials(String hostname, String username, String password) {
            this.hostname = hostname;
            this.username = username;
            this.password = password;
        }

        public final String hostname, username, password;
    }

    // Thanx: https://www.baeldung.com/java-email
    public static void sendMessage(String to, String from, String subject, String body, Credentials credentials) throws MessagingException {
        final Session session = __setupSession(credentials);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        String msg = body;

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);
        Transport.send(message);
    }

    private static Session __setupSession(Credentials credentials) {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", credentials.hostname);
        prop.put("mail.smtp.port", "25");
        prop.put("mail.smtp.ssl.trust", credentials.hostname);
        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(credentials.username, credentials.password);
            }
        });
    }
}
