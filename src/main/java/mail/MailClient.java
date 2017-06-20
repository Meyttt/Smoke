package mail;

import sql.User;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;

import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import utils.Config;

import static org.apache.poi.hssf.record.FtPioGrbitSubRecord.length;


/**
 * Created by a.chebotareva on 17.05.2017.
 */
public class MailClient {
    private static String url;

    public static void checkEmail(String host, String name, String password) {
        try {
            Thread.sleep(10000);
            //TODO: добавить ошибку, если последнее письмо уже прочитано!
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.store.protocol", "imap");
            int i = 0;
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imap");
            store.connect(host, name, password);
            Thread.sleep(100);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message response = inbox.getMessage(inbox.getMessageCount());
            if (!response.getFlags().contains(Flags.Flag.SEEN)) {
                String content;
                if (response.getContent().getClass().equals(String.class)) {
                    content = (String) response.getContent();
                } else {
                    Multipart mp = (Multipart) response.getContent();
                    BodyPart bp = mp.getBodyPart(0);
                    content = (String) bp.getContent();
                }

                LinkExtractor linkExtractor = LinkExtractor.builder()
                        .linkTypes(EnumSet.of(LinkType.URL))
                        .build();
                Iterable<LinkSpan> links = linkExtractor.extractLinks(content);
                LinkSpan link = links.iterator().next();
                url = content.substring(link.getBeginIndex(), link.getEndIndex());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUrl() {
        return url;
    }
    public  static String getContent(String host, String name, String password){
        String content=null;
        try {
            Thread.sleep(5000);
            //TODO: добавить ошибку, если последнее письмо уже прочитано!
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.store.protocol", "imap");
            int i = 0;
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imap");
            store.connect(host, name, password);
            Thread.sleep(100);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message response = inbox.getMessage(inbox.getMessageCount());
            if (!response.getFlags().contains(Flags.Flag.SEEN)) {

                if (response.getContent().getClass().equals(String.class)) {
                    content = (String) response.getContent();
                } else {
                    Multipart mp = (Multipart) response.getContent();
                    BodyPart bp = mp.getBodyPart(0);
                    content = (String) bp.getContent();
                }
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return content;
    }
    public static void sendMessage( String to,  String from, String password){
        String username= from.split("@")[0];

        // Assuming you are sending email through relay.jangosmtp.net
        String host = "mail."+ from.split("@")[from.split("@").length-1];

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "25");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject("Testing Subject");

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText("This is message body");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            String filename = "F1-05.0331.9.0M.xls";
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            // Send message
            Transport.send(message);//TODO: https://stackoverflow.com/questions/21076179/pkix-path-building-failed-and-unable-to-find-valid-certification-path-to-requ

            System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    public static void sendMessage2(String to,  String from, String password) throws MessagingException {
        Properties props = new Properties();
        String host = "mail."+ from.split("@")[from.split("@").length-1];
        props.put("mail.smtp.host", host);
        props.put("mail.store.protocol", "imap");
        Session session = Session.getInstance(props, null);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(InternetAddress.parse(from)[0]);
        msg.setRecipients(Message.RecipientType.TO, to);
        msg.setSubject("test");
        BodyPart msgBodyPart = new MimeBodyPart();
        msgBodyPart.setText("main text");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(msgBodyPart);

        msgBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(new File("F1-05.0331.9.0M.xls").getAbsolutePath()); //todo:костыль
        msgBodyPart.setDataHandler(new DataHandler(source));

//        String attachmentName = MimeUtility.encodeText(FilenameUtils.getName("F1-05.0331.9.0M.xls"), "UTF-8", "Q");
        msgBodyPart.setFileName("name");
        multipart.addBodyPart(msgBodyPart);
        msg.setContent(multipart);

        Transport.send(msg);



    }
}
