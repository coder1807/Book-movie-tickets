package com.example.movietickets.demo.mailing;

import com.example.movietickets.demo.model.Booking;
import com.example.movietickets.demo.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class DefaultEmailService implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public void sendMail(AbstractEmailContext email) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        Context context = new Context();
        context.setVariables(email.getContext());
        System.out.println(email.getTemplateLocation());
        String emailContent = templateEngine.process(email.getTemplateLocation(), context);

        messageHelper.setTo(email.getTo());
        messageHelper.setFrom(email.getFrom());
        messageHelper.setSubject(email.getSubject());
        messageHelper.setText(emailContent, true);

        emailSender.send(message);
    }

    public void sendSimpleMessage(String to, User user, Booking booking) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        Context context = new Context();
        String[] posterPath = booking.getPoster().split("/");
        String posterName = posterPath[posterPath.length - 1];
        context.setVariable("userName", user.getFullname());
        context.setVariable("movieName", booking.getFilmName());
        context.setVariable("id", booking.getId());
        context.setVariable("time", booking.getStartTime());
        context.setVariable("room", booking.getRoomName());
        context.setVariable("cinema", booking.getCinemaName());
        context.setVariable("seat", booking.getSeatName());
        context.setVariable("price", booking.getPrice());
        context.setVariable("payment", booking.getPayment());
        String emailContent = templateEngine.process("mailing/paymentSuccess", context);
        messageHelper.setTo(to);
        messageHelper.setSubject("Thông tin đặt vé xem phim - Mã Thanh toán " + booking.getId());
        messageHelper.setText(emailContent, true);
        emailSender.send(message);
    }

    public String formatDateTime(Date dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime localDateTime = dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.format(formatter);
    }

    public void sendResetPasswordEmail(String toEmail, String token) {
        String subject = "Khôi phục mật khẩu";
        String resetUrl = "movieapp://com.example.movie_app/reset-password?token=" + token;

        // Nội dung email
                String content = """
            <html>
                <body>
                    <p>Xin chào,</p>
                    <p>Bạn đã yêu cầu đặt lại mật khẩu. Nhấn vào liên kết dưới đây để đặt lại mật khẩu của bạn:</p>
                    <p>
                        <a href="%s">%s</a>
                    </p>
                    <p><strong>Lưu ý:</strong> Để tiếp tục, vui lòng mở liên kết trên thiết bị có ứng dụng của chúng tôi.</p>
                    <p>Sau khi truy cập liên kết, bạn sẽ được hướng dẫn để nhập mật khẩu mới.</p>
                    <p>Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>
                    <p>Trân trọng,<br>Đội ngũ hỗ trợ Ba Anh Em</p>
                </body>
            </html>
            """.formatted(resetUrl, resetUrl);

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // Set to true to indicate HTML content
            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }
}
