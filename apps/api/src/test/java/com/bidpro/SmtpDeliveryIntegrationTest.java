package com.bidpro;

import com.bidpro.dao.EmailOutboxDao;
import com.bidpro.entities.EmailOutbox;
import com.bidpro.service.EmailOutboxService;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

/** Exercises JavaMail against a local SMTP protocol listener, never an external inbox. */
@SpringBootTest(properties={"app.mail.mode=smtp","app.mail.from=no-reply@bidpro.test",
    "app.mail.initial-delay-ms=3600000","spring.mail.properties.mail.smtp.auth=false",
    "spring.mail.properties.mail.smtp.starttls.enable=false"})
@ActiveProfiles("test")
class SmtpDeliveryIntegrationTest {
    static final LocalSmtp smtp=new LocalSmtp();
    @DynamicPropertySource static void smtpProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.mail.host",()->"127.0.0.1");properties.add("spring.mail.port",smtp::port);
        properties.add("spring.mail.username",()->"");properties.add("spring.mail.password",()->"");
    }
    @Autowired EmailOutboxService delivery;
    @Autowired EmailOutboxDao emails;
    @AfterAll static void close() throws Exception {smtp.close();}

    @Test void queuedResetInstructionsAreDeliveredAsRealSmtpAndSensitiveBodyIsCleared() throws Exception {
        String recipient="smtp-"+System.nanoTime()+"@example.test";
        delivery.enqueue(recipient,"Reset your BidPro password","Open http://127.0.0.1:5173/reset-password?token=loopback-only",true);
        EmailOutbox before=emails.findAll().stream().filter(mail->mail.getRecipient().equals(recipient)).findFirst().orElseThrow();
        assertThat(before.getStatus()).isEqualTo(EmailOutbox.Status.QUEUED);
        delivery.deliver(before.getId());
        String mime=smtp.messages.poll(10,TimeUnit.SECONDS);
        assertThat(mime).contains("Subject: Reset your BidPro password").contains(recipient)
            .contains("From: no-reply@bidpro.test").contains("reset-password?token=loopback-only");
        EmailOutbox sent=emails.findById(before.getId()).orElseThrow();
        assertThat(sent.getStatus()).isEqualTo(EmailOutbox.Status.SENT);
        assertThat(sent.getSentAt()).isNotNull();assertThat(sent.getAttempts()).isEqualTo(1);
        assertThat(sent.getBody()).doesNotContain("loopback-only");
        delivery.deliver(before.getId());
        assertThat(emails.findById(before.getId()).orElseThrow().getAttempts()).isEqualTo(1);
    }

    static class LocalSmtp implements AutoCloseable {
        final ServerSocket listener;
        final BlockingQueue<String> messages=new LinkedBlockingQueue<>();
        final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread thread=new Thread(r,"bidpro-test-smtp");thread.setDaemon(true);return thread;});
        LocalSmtp() {
            try {listener=new ServerSocket(0,10,InetAddress.getByName("127.0.0.1"));}
            catch(IOException error){throw new IllegalStateException(error);}
            worker.submit(()->{while(!listener.isClosed()) {try(Socket client=listener.accept()){serve(client);}catch(IOException ignored){if(listener.isClosed())return;}}});
        }
        int port(){return listener.getLocalPort();}
        void serve(Socket client) throws IOException {
            client.setSoTimeout(10000);
            var input=new BufferedReader(new InputStreamReader(client.getInputStream(),StandardCharsets.UTF_8));
            var output=new PrintWriter(new OutputStreamWriter(client.getOutputStream(),StandardCharsets.UTF_8),true);
            reply(output,"220 localhost BidPro test SMTP");
            String line;
            while((line=input.readLine())!=null) {
                if(line.startsWith("EHLO")||line.startsWith("HELO"))reply(output,"250 localhost");
                else if(line.startsWith("MAIL FROM:")||line.startsWith("RCPT TO:")||line.equals("RSET")||line.equals("NOOP"))reply(output,"250 OK");
                else if(line.equals("DATA")) {
                    reply(output,"354 End data with <CRLF>.<CRLF>");
                    StringBuilder message=new StringBuilder();
                    while((line=input.readLine())!=null&&!line.equals("."))message.append(line).append('\n');
                    messages.add(message.toString());reply(output,"250 Message accepted");
                } else if(line.equals("QUIT")){reply(output,"221 Bye");return;}
                else reply(output,"502 Command not implemented");
            }
        }
        void reply(PrintWriter writer,String text){writer.print(text+"\r\n");writer.flush();}
        public void close() throws IOException {listener.close();worker.shutdownNow();}
    }
}
