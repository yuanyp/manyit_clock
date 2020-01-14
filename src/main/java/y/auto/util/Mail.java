package y.auto.util;

import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * author:�ױ�
 * Date:2020/1/1312:45
 **/
public class Mail {
    private static Logger logger = LoggerFactory.getLogger(Mail.class);
    /**
     * @Title: resceive
     * @Description: �����ʼ������������渽����ɾ��
     * @throws Exception
     *             void
     */
    public static boolean resceive(String mailName,String passWord,String curreDay) {

        boolean rel = true;
        try
        {
            // ʹ��163����,163�� pop3��ַ�ǡ�pop3.163.com,�� �˿��ǡ�110���� ����
            String port = "995"; // �˿ں�
            String servicePath = "pop.qq.com"; // ��������ַ

            // ׼�����ӷ������ĻỰ��Ϣ
            Properties props = new Properties();

            props.setProperty("mail.store.protocol", "pop3"); // ʹ��pop3Э��
            props.setProperty("mail.pop3.port", port); // �˿�

            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);

            props.put("mail.pop3.ssl.enable",true);
            props.put("mail.pop3.ssl.socketFactory",sf);

            props.setProperty("mail.pop3.host", servicePath); // pop3������
            // ����Sessionʵ������
            Session session = Session.getInstance(props);
            //pop3Store = (POP3Store) session.getStore("pop3");

            Store store = session.getStore("pop3");
            store.connect(mailName, passWord); // 163��������¼���ڵ�������¼���������������163������Ȩ�����������ͨ�ĵ�¼����

            // ����ռ���
            Folder folder = store.getFolder("INBOX");
            /*
             * Folder.READ_ONLY��ֻ��Ȩ�� Folder.READ_WRITE���ɶ���д�������޸��ʼ���״̬��
             */
            folder.open(Folder.READ_WRITE); // ���ռ���

            // ����POP3Э���޷���֪�ʼ���״̬,����getUnreadMessageCount�õ������ռ�����ʼ�����
            // System.out.println("δ���ʼ���: " + folder.getUnreadMessageCount());
            //
            // // ����POP3Э���޷���֪�ʼ���״̬,��������õ��Ľ��ʼ�ն���Ϊ0
            // System.out.println("ɾ���ʼ���: " + folder.getDeletedMessageCount());
            // System.out.println("���ʼ�: " + folder.getNewMessageCount());
            //
            // // ����ռ����е��ʼ�����
            // System.out.println("�ʼ�����: " + folder.getMessageCount());

            // �õ��ռ����е������ʼ�,������
            int size = folder.getMessageCount();
            Message[] messages = folder.getMessages((size > 10) ? size -10 : 1,folder.getMessageCount());
            folder.getUnreadMessageCount();
            folder.getMessageCount();

            //parseMessage(messages);

            // �õ��ռ����е������ʼ�����ɾ���ʼ�
            //deleteMessage(messages);
            for (int i = 0, count = messages.length; i < count; i++) {
                MimeMessage msg = (MimeMessage) messages[i];
                if(curreDay.equals(getSubject(msg))){
                    rel = false;
                    break;
                }
            }
            // �ͷ���Դ
            folder.close(true);
            store.close();
        }catch (Exception e){
            logger.error("��ȡ�����б����", e);
        }
        return rel;
    }

    /**
     * @Title: parseMessage
     * @Description: �����ʼ�
     * @param messages
     *            Ҫ�������ʼ��б�
     * @throws MessagingException
     * @throws IOException
     *             void
     */
    public static void parseMessage(Message... messages)
            throws MessagingException, IOException {
        if (messages == null || messages.length < 1) {
            throw new MessagingException("δ�ҵ�Ҫ�������ʼ�!");
        }
        // ���������ʼ�
        for (int i = 0, count = messages.length; i < count; i++) {
            MimeMessage msg = (MimeMessage) messages[i];
            System.out.println("------------------������" + msg.getMessageNumber()
                    + "���ʼ�-------------------- ");
            System.out.println("����: " + getSubject(msg));
            System.out.println("������: " + getFrom(msg));
            System.out.println("�ռ��ˣ�" + getReceiveAddress(msg, null));
            System.out.println("����ʱ�䣺" + getSentDate(msg, null));
            System.out.println("�Ƿ��Ѷ���" + isSeen(msg));
            System.out.println("�ʼ����ȼ���" + getPriority(msg));
            System.out.println("�Ƿ���Ҫ��ִ��" + isReplySign(msg));
            System.out.println("�ʼ���С��" + msg.getSize() * 1024 + "kb");
            boolean isContainerAttachment = isContainAttachment(msg);
            System.out.println("�Ƿ����������" + isContainerAttachment);
            if (isContainerAttachment) {
                saveAttachment(msg, "d:\\mailTest\\" + msg.getSubject() + "_"
                        + i + "_"); // ���渽��
            }
            StringBuffer content = new StringBuffer(30);
            getMailTextContent(msg, content);
            System.out.println("�ʼ����ģ�"
                    + (content.length() > 100 ? content.substring(0, 100)
                    + "..." : content));
            System.out.println("------------------��" + msg.getMessageNumber()
                    + "���ʼ���������-------------------- ");
            System.out.println();

        }
    }

    /**
     * @Title: deleteMessage
     * @Description: �����ʼ�
     * @param messages
     *            Ҫ�������ʼ��б�
     * @throws MessagingException
     * @throws IOException
     *             void
     */
    public static void deleteMessage(Message... messages)
            throws MessagingException, IOException {
        if (messages == null || messages.length < 1) {
            throw new MessagingException("δ�ҵ�Ҫ�������ʼ�!");
        }
        // ���������ʼ�
        for (int i = 0, count = messages.length; i < count; i++) {

            // �ʼ�ɾ��
            Message message = messages[i];
            String subject = message.getSubject();
            // set the DELETE flag to true
            message.setFlag(Flags.Flag.DELETED, true);
            System.out.println("Marked DELETE for message: " + subject);

        }
    }

    /**
     * @Title: getSubject
     * @Description: ����ʼ�����
     * @param msg
     *            �ʼ�����
     * @return �������ʼ�����
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     *             String
     */
    public static String getSubject(MimeMessage msg)
            throws UnsupportedEncodingException, MessagingException {
        return MimeUtility.decodeText(msg.getSubject());
    }

    /**
     * @Title: getFrom
     * @Description: ����ʼ�������
     * @param msg
     *            �ʼ�����
     * @return ���� <Email��ַ>
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     *             String
     */
    public static String getFrom(MimeMessage msg) throws MessagingException,
            UnsupportedEncodingException {
        String from = "";
        Address[] froms = msg.getFrom();
        if (froms.length < 1) {
            throw new MessagingException("û�з�����!");
        }
        InternetAddress address = (InternetAddress) froms[0];
        String person = address.getPersonal();
        if (person != null) {
            person = MimeUtility.decodeText(person) + " ";
        } else {
            person = "";
        }
        from = person + "<" + address.getAddress() + ">";

        return from;
    }

    /**
     * @Title: getReceiveAddress
     * @Description: �����ռ������ͣ���ȡ�ʼ��ռ��ˡ����ͺ����͵�ַ������ռ�������Ϊ�գ��������е��ռ���
     *               <p>
     *               Message.RecipientType.TO �ռ���
     *               </p>
     *               <p>
     *               Message.RecipientType.CC ����
     *               </p>
     *               <p>
     *               Message.RecipientType.BCC ����
     *               </p>
     * @param msg
     *            �ʼ�����
     * @param type
     *            �ռ�������
     * @return �ռ���1 <�ʼ���ַ1>, �ռ���2 <�ʼ���ַ2>, ...
     * @throws MessagingException
     *             String
     */
    public static String getReceiveAddress(MimeMessage msg,
                                           Message.RecipientType type) throws MessagingException {
        StringBuffer receiveAddress = new StringBuffer();
        Address[] addresss = null;
        if (type == null) {
            addresss = msg.getAllRecipients();
        } else {
            addresss = msg.getRecipients(type);
        }

        if (addresss == null || addresss.length < 1) {
            throw new MessagingException("û���ռ���!");
        }
        for (Address address : addresss) {
            InternetAddress internetAddress = (InternetAddress) address;
            receiveAddress.append(internetAddress.toUnicodeString())
                    .append(",");
        }

        receiveAddress.deleteCharAt(receiveAddress.length() - 1); // ɾ�����һ������

        return receiveAddress.toString();
    }

    /**
     * @Title: getSentDate
     * @Description: ����ʼ�����ʱ��
     * @param msg
     *            �ʼ�����
     * @param pattern
     *            ���ڸ�ʽ
     * @return yyyy��mm��dd�� ����X HH:mm
     * @throws MessagingException
     *             String
     */
    public static String getSentDate(MimeMessage msg, String pattern)
            throws MessagingException {
        Date receivedDate = msg.getSentDate();
        if (receivedDate == null) {
            return "";
        }
        if (pattern == null || "".equals(pattern)) {
            pattern = "yyyy��MM��dd�� E HH:mm ";
        }
        return new SimpleDateFormat(pattern).format(receivedDate);
    }

    /**
     * @Title: isContainAttachment
     * @Description: �ж��ʼ����Ƿ��������
     * @param part
     *            �ʼ�����
     * @return �ʼ��д��ڸ�������true�������ڷ���false
     * @throws MessagingException
     * @throws IOException
     *             boolean
     */
    public static boolean isContainAttachment(Part part)
            throws MessagingException, IOException {
        boolean flag = false;
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disp = bodyPart.getDisposition();
                boolean isHasAttachment = (disp != null && (disp
                        .equalsIgnoreCase(Part.ATTACHMENT) || disp
                        .equalsIgnoreCase(Part.INLINE)));
                if (isHasAttachment) {
                    flag = true;
                } else if (bodyPart.isMimeType("multipart/*")) {
                    flag = isContainAttachment(bodyPart);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("application") != -1) {
                        flag = true;
                    }

                    if (contentType.indexOf("name") != -1) {
                        flag = true;
                    }
                }

                if (flag) {
                    break;
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            flag = isContainAttachment((Part) part.getContent());
        }
        return flag;
    }

    /**
     * @Title: isSeen
     * @Description: �ж��ʼ��Ƿ��Ѷ�
     * @param msg
     *            �ʼ�����
     * @return ����ʼ��Ѷ�����true,���򷵻�false
     * @throws MessagingException
     *             boolean
     */
    public static boolean isSeen(MimeMessage msg) throws MessagingException {
        return msg.getFlags().contains(Flags.Flag.SEEN);
    }

    /**
     * @Title: isReplySign
     * @Description: �ж��ʼ��Ƿ���Ҫ�Ķ���ִ
     * @param msg
     *            �ʼ�����
     * @return ��Ҫ��ִ����true,���򷵻�false
     * @throws MessagingException
     *             boolean
     */
    public static boolean isReplySign(MimeMessage msg)
            throws MessagingException {
        boolean replySign = false;
        String[] headers = msg.getHeader("Disposition-Notification-To");
        if (headers != null) {
            replySign = true;
        }
        return replySign;
    }

    /**
     * @Title: getPriority
     * @Description: ����ʼ������ȼ�
     * @param msg
     *            �ʼ�����
     * @return 1(High):���� 3:��ͨ(Normal) 5:��(Low)
     * @throws MessagingException
     *             String
     */
    public static String getPriority(MimeMessage msg) throws MessagingException {
        String priority = "��ͨ";
        String[] headers = msg.getHeader("X-Priority");
        if (headers != null) {
            String headerPriority = headers[0];
            if (headerPriority.indexOf("1") != -1
                    || headerPriority.indexOf("High") != -1) {
                priority = "����";
            } else if (headerPriority.indexOf("5") != -1
                    || headerPriority.indexOf("Low") != -1) {
                priority = "��";
            } else {
                priority = "��ͨ";
            }
        }
        return priority;
    }

    /**
     * @Title: getMailTextContent
     * @Description: ����ʼ��ı�����
     * @param part
     *            �ʼ���
     * @param content
     *            �洢�ʼ��ı����ݵ��ַ���
     * @throws MessagingException
     * @throws IOException
     *             void
     */
    public static void getMailTextContent(Part part, StringBuffer content)
            throws MessagingException, IOException {
        // ������ı����͵ĸ�����ͨ��getContent��������ȡ���ı����ݣ����ⲻ��������Ҫ�Ľ��������������Ҫ���ж�
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }

    /**
     * @Title: saveAttachment
     * @Description: ���渽��
     * @param part
     *            �ʼ��ж��������е�����һ�������
     * @param destDir
     *            ��������Ŀ¼
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     * @throws FileNotFoundException
     * @throws IOException
     *             void
     */
    public static void saveAttachment(Part part, String destDir)
            throws UnsupportedEncodingException, MessagingException,
            FileNotFoundException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent(); // �������ʼ�
            // �������ʼ���������ʼ���
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                // ��ø������ʼ�������һ���ʼ���
                BodyPart bodyPart = multipart.getBodyPart(i);
                // ĳһ���ʼ���Ҳ�п������ɶ���ʼ�����ɵĸ�����
                String disp = bodyPart.getDisposition();
                boolean isHasAttachment = (disp != null && (disp
                        .equalsIgnoreCase(Part.ATTACHMENT) || disp
                        .equalsIgnoreCase(Part.INLINE)));
                if (isHasAttachment) {
                    InputStream is = bodyPart.getInputStream();
                    saveFile(is, destDir, decodeText(bodyPart.getFileName()));
                    System.out.println("----������"
                            + decodeText(bodyPart.getFileName()) + ","
                            + " ����·��Ϊ" + destDir);
                } else if (bodyPart.isMimeType("multipart/*")) {
                    saveAttachment(bodyPart, destDir);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("name") != -1
                            || contentType.indexOf("application") != -1) {
                        saveFile(bodyPart.getInputStream(), destDir,
                                decodeText(bodyPart.getFileName()));
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachment((Part) part.getContent(), destDir);
        }
    }

    /**
     * @Title: saveFile
     * @Description: ��ȡ�������е����ݱ�����ָ��Ŀ¼
     * @param is
     *            ������
     * @param destDir
     *            �ļ��洢Ŀ¼
     * @param fileName
     *            �ļ���
     * @throws FileNotFoundException
     * @throws IOException
     *             void
     */
    private static void saveFile(InputStream is, String destDir, String fileName)
            throws FileNotFoundException, IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(new File(destDir + fileName)));
        int len = -1;
        while ((len = bis.read()) != -1) {
            bos.write(len);
            bos.flush();
        }
        bos.close();
        bis.close();
    }

    /**
     * @Title: decodeText
     * @Description: �ı�����
     * @param encodeText
     *            ����MimeUtility.encodeText(String text)�����������ı�
     * @return �������ı�
     * @throws UnsupportedEncodingException
     *             String
     */
    public static String decodeText(String encodeText)
            throws UnsupportedEncodingException {
        if (encodeText == null || "".equals(encodeText)) {
            return "";
        } else {
            return MimeUtility.decodeText(encodeText);
        }
    }
}
