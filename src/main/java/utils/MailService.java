package utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailService {

    private final String username = "siwarchouanine5@gmail.com"; // Ton email
    private final String password = "ivkmopnzrafonnuk";    // Ton mot de passe applicatif Gmail

    public void sendMail(String toEmail, String subject, String body) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
