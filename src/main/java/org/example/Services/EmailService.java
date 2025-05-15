package org.example.Services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Service for sending emails using Gmail SMTP
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "zwebimalak66@gmail.com";
    //private static final String PASSWORD = "f0a6403bcd339bfbfd26e54c5366413d"; // App password for Gmail

    private static final String PASSWORD = "myws ymzw ruwa cufr";

    // Email sender's address
    private static final String FROM_EMAIL = "zwebimalak66@gmail.com";
    private static final String SENDER_NAME = "Tbibi Service";

    /**
     * Send a payment confirmation email to the customer
     *
     * @param recipientEmail Customer's email address
     * @param orderNumber    The order number
     * @param total          The total amount paid
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendPaymentConfirmationEmail(String recipientEmail, int orderNumber, double total) {
        String subject = "Confirmation de Paiement - Commande #" + orderNumber;

        StringBuilder body = new StringBuilder();
        body.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        body.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e4e8f0; border-radius: 8px;'>");
        body.append("<div style='background: linear-gradient(to right, #3a7bd5, #00d2ff); color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center;'>");
        body.append("<h1 style='margin: 0;'>Paiement Confirmé</h1>");
        body.append("</div>");
        body.append("<div style='padding: 20px; background-color: white;'>");
        body.append("<p>Cher client,</p>");
        body.append("<p>Nous vous remercions pour votre achat. Votre paiement a été traité avec succès.</p>");
        body.append("<h2 style='color: #3a7bd5;'>Détails de la commande</h2>");
        body.append("<table style='width: 100%; border-collapse: collapse;'>");
        body.append("<tr><td><strong>Numéro de commande:</strong></td><td>#").append(orderNumber).append("</td></tr>");
        body.append("<tr><td><strong>Total payé:</strong></td><td>").append(String.format("%.2f TND", total)).append("</td></tr>");
        body.append("<tr><td><strong>Date:</strong></td><td>").append(java.time.LocalDate.now()).append("</td></tr>");
        body.append("<tr><td><strong>Statut:</strong></td><td style='color: #2ecc71; font-weight: bold;'>Payé</td></tr>");
        body.append("</table>");
        body.append("<div style='margin: 30px 0; padding: 15px; background-color: #f8f9fa; border-left: 4px solid #3a7bd5; border-radius: 4px;'>");
        body.append("<p style='margin: 0;'>Votre commande a été confirmée et sera traitée dans les plus brefs délais.</p>");
        body.append("</div>");
        body.append("<p>Si vous avez des questions concernant votre commande, n'hésitez pas à nous contacter.</p>");
        body.append("<p>Cordialement,<br>L'équipe Tbibi</p>");
        body.append("</div>");
        body.append("<div style='text-align: center; padding: 15px; background-color: #f8f9fa; color: #6c757d; border-radius: 0 0 8px 8px;'>");
        body.append("<p style='margin: 0; font-size: 14px;'>Ce message a été envoyé automatiquement. Merci de ne pas y répondre.</p>");
        body.append("</div>");
        body.append("</div>");
        body.append("</body></html>");

        return sendEmail(recipientEmail, subject, body.toString(), true);
    }

    /**
     * Send a payment failure email to the customer
     *
     * @param recipientEmail Customer's email address
     * @param orderNumber    The order number
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendPaymentFailureEmail(String recipientEmail, int orderNumber) {
        String subject = "Échec de Paiement - Commande #" + orderNumber;

        StringBuilder body = new StringBuilder();
        body.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        body.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e4e8f0; border-radius: 8px;'>");
        body.append("<div style='background: linear-gradient(to right, #e74c3c, #c0392b); color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center;'>");
        body.append("<h1 style='margin: 0;'>Échec de Paiement</h1>");
        body.append("</div>");
        body.append("<div style='padding: 20px; background-color: white;'>");
        body.append("<p>Cher client,</p>");
        body.append("<p>Nous vous informons que le paiement de votre commande n'a pas pu être traité.</p>");
        body.append("<h2 style='color: #e74c3c;'>Détails de la commande</h2>");
        body.append("<table style='width: 100%; border-collapse: collapse;'>");
        body.append("<tr><td><strong>Numéro de commande:</strong></td><td>#").append(orderNumber).append("</td></tr>");
        body.append("<tr><td><strong>Date:</strong></td><td>").append(java.time.LocalDate.now()).append("</td></tr>");
        body.append("<tr><td><strong>Statut:</strong></td><td style='color: #e74c3c; font-weight: bold;'>Non Payé</td></tr>");
        body.append("</table>");
        body.append("<div style='margin: 30px 0; padding: 15px; background-color: #fdf3f2; border-left: 4px solid #e74c3c; border-radius: 4px;'>");
        body.append("<p style='margin: 0;'>Votre commande n'a pas été confirmée en raison d'un problème lors du paiement. Veuillez réessayer ou utiliser une autre méthode de paiement.</p>");
        body.append("</div>");
        body.append("<p>Raisons possibles de l'échec de paiement:</p>");
        body.append("<ul>");
        body.append("<li>Solde insuffisant sur votre carte</li>");
        body.append("<li>Informations de carte incorrectes</li>");
        body.append("<li>Problème temporaire avec le service de paiement</li>");
        body.append("</ul>");
        body.append("<p>Si vous souhaitez finaliser votre commande, veuillez vous connecter à votre compte et réessayer le paiement.</p>");
        body.append("<p>Pour toute assistance, n'hésitez pas à nous contacter.</p>");
        body.append("<p>Cordialement,<br>L'équipe Tbibi</p>");
        body.append("</div>");
        body.append("<div style='text-align: center; padding: 15px; background-color: #f8f9fa; color: #6c757d; border-radius: 0 0 8px 8px;'>");
        body.append("<p style='margin: 0; font-size: 14px;'>Ce message a été envoyé automatiquement. Merci de ne pas y répondre.</p>");
        body.append("</div>");
        body.append("</div>");
        body.append("</body></html>");

        return sendEmail(recipientEmail, subject, body.toString(), true);
    }

    /**
     * Send an email
     *
     * @param to          Recipient's email address
     * @param subject     Email subject
     * @param body        Email body
     * @param isHtml      Whether the email body is HTML
     * @return true if the email was sent successfully, false otherwise
     */
    private boolean sendEmail(String to, String subject, String body, boolean isHtml) {
        try {
            // Setup mail server properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", SMTP_HOST);
            properties.put("mail.smtp.port", SMTP_PORT);

            // Create a Session object with authenticator
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            // Create a message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Set the message content
            if (isHtml) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body);
            }

            // Send the message
            Transport.send(message);
            System.out.println("Email sent successfully to: " + to);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }
}