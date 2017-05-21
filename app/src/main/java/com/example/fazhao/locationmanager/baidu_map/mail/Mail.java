package com.example.fazhao.locationmanager.baidu_map.mail;

/**
 * Created by fazhao on 2017/3/17.
 */

import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static android.R.attr.path;
import static android.R.id.message;

public class Mail {
    private String host;//smtp服务器
    private String auth;//验证

    private String mailAcount;//邮件用户名
    private String password;//密码

    //共同变量
    private MimeMessage mimeMessage;
    private Properties props;
    private Session session;
    /**
     * 构造函数
     */
    public Mail(String host,String mailAcount,String password){
        this.host=host;
        this.auth="true";
        this.mailAcount=mailAcount;
        this.password=password;
    }

    /**
     * 变量初始化
     */
    protected void initialize(){
        props = System.getProperties();
        // 指定smtp服务器
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", auth);
        // 新建一个包含smtp会话的MimeMessage对象
        session=Session.getDefaultInstance(props,null);
        mimeMessage=new MimeMessage(session);
    }

    /**
     * 设定邮件信息
     * @param from,to,subject
     * @throws MessagingException
     * @throws AddressException
     * @throws UnsupportedEncodingException
     */
    public void create(String from,String to,String subject) throws AddressException, MessagingException, UnsupportedEncodingException{
        //初始化
        initialize();
        if (from.equals("") || to.equals("") ||subject.equals(""))
        {
            System.out.println("输入有误");
        }else{
            //指定送信人
            mimeMessage.setFrom(new InternetAddress(from));
            //对方邮件地址
            mimeMessage.setRecipients(Message.RecipientType.TO, to);
            //邮件标题
            mimeMessage.setSubject(subject,"GBK");
        }
    }

    /**
     * 邮件格式，和内容指定
     * @param content
     * @throws MessagingException
     */
    public void addContent(String content,@Nullable String path) throws MessagingException{
        // 指定邮件格式
        mimeMessage.setHeader("Content-Type", "text/html");
        // 邮件内容
        mimeMessage.setText(content);
        if(path != null) {
            MimeBodyPart text = new MimeBodyPart();
            // setContent(“邮件的正文内容”,”设置邮件内容的编码方式”)
            text.setContent(content+"<img src='cid:a'><img src='cid:b'>",
                    "text/html;charset=gb2312");

            MimeBodyPart img = new MimeBodyPart();
            DataHandler dh = new DataHandler(new FileDataSource(path));//图片路径
            img.setDataHandler(dh);
            // 创建图片的一个表示用于显示在邮件中显示
            img.setContentID("a");

            MimeMultipart mm = new MimeMultipart();
            mm.addBodyPart(text);
            mm.addBodyPart(img);
            mm.setSubType("related");// 设置正文与图片之间的关系
            // 图班与正文的 body
            MimeBodyPart all = new MimeBodyPart();
            all.setContent(mm);
//            // 附件与正文（text 和 img）的关系
//            MimeMultipart mm2 = new MimeMultipart();
//            mm2.addBodyPart(all);
//            mm2.addBodyPart(img2);
//            mm2.setSubType("mixed");// 设置正文与附件之间的关系

            mimeMessage.setContent(mm);
            mimeMessage.saveChanges(); // 保存修改
        }

    }

    /**
     * 发信
     * @throws MessagingException
     */
    public void send() throws MessagingException{
// 指定送信日期
        mimeMessage.setSentDate(new Date());
        // 送信
        Transport transport = session.getTransport("smtp");
        transport.connect(host,mailAcount,password);
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        transport.close();
    }
}
