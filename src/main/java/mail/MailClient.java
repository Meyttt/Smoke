package mail;

import sql.User;

import javax.mail.*;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;

import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;


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
}
