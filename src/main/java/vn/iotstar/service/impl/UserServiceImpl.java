package vn.iotstar.service.impl;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.security.SecureRandom;
import jakarta.mail.MessagingException;
import vn.iotstar.dao.UserDao;
import vn.iotstar.dao.impl.UserDaoImpl;
import vn.iotstar.entity.User;
import vn.iotstar.service.OtpRequestStatus;
import vn.iotstar.service.OtpResult;
import vn.iotstar.service.RegistrationStatus;
import vn.iotstar.service.UserService;
import vn.iotstar.util.MailSender;
import vn.iotstar.util.PasswordHasher;
import vn.iotstar.util.TokenUtil;

public class UserServiceImpl implements UserService {
    private static final int REMEMBER_DAYS = 30;
    private static final int OTP_LIFETIME_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserDao userDao;

    public UserServiceImpl() {
        this(new UserDaoImpl());
    }

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User login(String username, String password) throws SQLException {
        if (username == null || password == null) return null;
        User user = userDao.findByUsername(username.trim());
        if (user == null || !PasswordHasher.verifyPassword(password, user.getPasswordHash())) return null;
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public RegistrationStatus register(String fullName, String email, String username,
                                       String password, String phone) throws SQLException {
        String normalizedEmail = normalizeEmail(email);
        String normalizedUsername = username.trim();
        if (userDao.existsByUsername(normalizedUsername)) return RegistrationStatus.USERNAME_TAKEN;
        if (userDao.existsByEmail(normalizedEmail)) return RegistrationStatus.EMAIL_TAKEN;

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setUsername(normalizedUsername);
        user.setPasswordHash(PasswordHasher.hashPassword(password));
        user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        user.setActive(false);
        userDao.insert(user);
        return sendOtp(user, "ACTIVATION") == OtpRequestStatus.SENT
                ? RegistrationStatus.CREATED : RegistrationStatus.ACTIVATION_EMAIL_FAILED;
    }

    @Override
    public OtpRequestStatus requestActivation(String email) throws SQLException {
        User user = userDao.findByEmail(normalizeEmail(email));
        if (user == null) return OtpRequestStatus.NOT_FOUND;
        if (user.isActive()) return OtpRequestStatus.ALREADY_ACTIVE;
        return sendOtp(user, "ACTIVATION");
    }

    @Override
    public OtpRequestStatus requestPasswordReset(String email) throws SQLException {
        User user = userDao.findByEmail(normalizeEmail(email));
        if (user == null || !user.isActive()) return OtpRequestStatus.NOT_FOUND;
        return sendOtp(user, "PASSWORD_RESET");
    }

    @Override
    public OtpResult activate(String email, String code) throws SQLException {
        return userDao.activateWithOtp(normalizeEmail(email), cleanCode(code), now());
    }

    @Override
    public OtpResult resetPassword(String email, String code, String password) throws SQLException {
        return userDao.resetPasswordWithOtp(normalizeEmail(email), cleanCode(code),
                PasswordHasher.hashPassword(password), now());
    }

    @Override
    public String issueRememberToken(User user) throws SQLException {
        String token = TokenUtil.newToken();
        userDao.saveRememberToken(TokenUtil.hash(token), user.getId(), Instant.now().plus(REMEMBER_DAYS, ChronoUnit.DAYS));
        return token;
    }

    @Override
    public User findByRememberToken(String token) throws SQLException {
        if (token == null || token.isBlank()) return null;
        User user = userDao.findByValidRememberToken(TokenUtil.hash(token));
        if (user != null && !user.isActive()) return null;
        if (user != null) user.setPasswordHash(null);
        return user;
    }

    @Override
    public void revokeRememberToken(String token) throws SQLException {
        if (token != null && !token.isBlank()) userDao.deleteRememberToken(TokenUtil.hash(token));
    }

    private OtpRequestStatus sendOtp(User user, String purpose) throws SQLException {
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime created = now();
        boolean saved = userDao.createOtp(user.getId(), purpose, PasswordHasher.hashPassword(code),
                created, created.plusMinutes(OTP_LIFETIME_MINUTES));
        if (!saved) return OtpRequestStatus.RATE_LIMITED;
        try {
            MailSender.sendOtp(user.getEmail(), user.getFullName(), purpose, code);
            return OtpRequestStatus.SENT;
        } catch (MessagingException ex) {
            return OtpRequestStatus.EMAIL_FAILED;
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanCode(String code) {
        return code == null ? "" : code.trim();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
