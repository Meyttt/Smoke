package mail;

import sql.User;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.Properties;

import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import org.apache.commons.io.FilenameUtils;
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
            store.close();
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
            store.close();
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

    public static void sendMessage(String to,  String from, File file) throws MessagingException, UnsupportedEncodingException {
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
        DataSource source = new FileDataSource(file.getAbsolutePath()); //todo:костыль
        msgBodyPart.setDataHandler(new DataHandler(source));

        String attachmentName = MimeUtility.encodeText(FilenameUtils.getName(file.getName()), "UTF-8", "Q");
        msgBodyPart.setFileName(attachmentName);
        multipart.addBodyPart(msgBodyPart);
        msg.setContent(multipart);

        Transport.send(msg);



    }

    public static String getContentFrom(String email, String password, String from) {
        String content = null;
        try {
            String host = "mail."+ email.split("@")[from.split("@").length-1];
            String name = email.split("@")[0];
            //TODO: добавить ошибку, если последнее письмо уже прочитано!
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.store.protocol", "imap");
            int i = 0;
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imap");
            store.connect(host, name, password);

            waiting:
            while (true) {
                try {
                    Folder inbox = store.getFolder("INBOX");
                    inbox.open(Folder.READ_ONLY);
                    Thread.sleep(1000);
                    Message response = inbox.getMessage(inbox.getMessageCount());
                    if ((!response.getFlags().contains(Flags.Flag.SEEN)) && response.getFrom()[0].toString().equals(from)) {
                        if (response.getContent().getClass().equals(String.class)) {
                            content = (String) response.getContent();
                        } else {
                            Multipart mp = (Multipart) response.getContent();
                            BodyPart bp = mp.getBodyPart(0);
                            content = (String) bp.getContent();
                            break waiting;
                        }

                    } else {
                        Thread.sleep(1000);
                        continue waiting;
                    }
                }catch (MessagingException e2){
                    e2.printStackTrace();
                    store.close();
                    store.connect(host, name, password);
                    continue waiting;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
}
