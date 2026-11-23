package vn.iotstar.dao.impl;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import vn.iotstar.connection.JpaSupport;
import vn.iotstar.dao.UserDao;
import vn.iotstar.entity.EmailOtp;
import vn.iotstar.entity.RememberToken;
import vn.iotstar.entity.User;
import vn.iotstar.service.OtpResult;
import vn.iotstar.util.PasswordHasher;

public class UserDaoImpl implements UserDao {
    private static final int MAX_OTP_ATTEMPTS = 5;

    @Override
    public User findByUsername(String username) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList().stream().findFirst().orElse(null));
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList().stream().findFirst().orElse(null));
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        return count("SELECT COUNT(u) FROM User u WHERE u.username = :value", username) > 0;
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        return count("SELECT COUNT(u) FROM User u WHERE u.email = :value", email) > 0;
    }

    private long count(String jpql, String value) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager.createQuery(jpql, Long.class)
                .setParameter("value", value).getSingleResult());
    }

    @Override
    public void insert(User user) throws SQLException {
        JpaSupport.write(entityManager -> {
            entityManager.persist(user);
            entityManager.flush();
            return null;
        });
    }

    @Override
    public void saveRememberToken(String tokenHash, int userId, Instant expiresAt) throws SQLException {
        JpaSupport.write(entityManager -> {
            User user = entityManager.find(User.class, userId);
            if (user == null) throw new IllegalStateException("Cannot save remember token for a missing account.");
            RememberToken token = new RememberToken(tokenHash, user,
                    LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
            entityManager.persist(token);
            return null;
        });
    }

    @Override
    public User findByValidRememberToken(String tokenHash) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createQuery("SELECT t.user FROM RememberToken t "
                        + "WHERE t.tokenHash = :hash AND t.expiresAt > :now", User.class)
                .setParameter("hash", tokenHash)
                .setParameter("now", LocalDateTime.now(ZoneOffset.UTC))
                .getResultList().stream().findFirst().orElse(null));
    }

    @Override
    public void deleteRememberToken(String tokenHash) throws SQLException {
        JpaSupport.write(entityManager -> {
            RememberToken token = entityManager.find(RememberToken.class, tokenHash);
            if (token != null) entityManager.remove(token);
            return null;
        });
    }

    @Override
    public boolean createOtp(int userId, String purpose, String codeHash,
                             LocalDateTime now, LocalDateTime expiresAt) throws SQLException {
        return JpaSupport.write(entityManager -> {
            User user = entityManager.find(User.class, userId);
            if (user == null) return false;

            Long recentCount = entityManager.createQuery(
                            "SELECT COUNT(o) FROM EmailOtp o WHERE o.user.id = :userId "
                                    + "AND o.purpose = :purpose AND o.createdAt >= :since", Long.class)
                    .setParameter("userId", userId)
                    .setParameter("purpose", purpose)
                    .setParameter("since", now.minusHours(1))
                    .getSingleResult();
            if (recentCount >= 5) return false;

            List<EmailOtp> latest = entityManager.createQuery(
                            "SELECT o FROM EmailOtp o WHERE o.user.id = :userId "
                                    + "AND o.purpose = :purpose ORDER BY o.createdAt DESC, o.id DESC", EmailOtp.class)
                    .setParameter("userId", userId)
                    .setParameter("purpose", purpose)
                    .setMaxResults(1)
                    .getResultList();
            if (!latest.isEmpty() && latest.get(0).getCreatedAt().isAfter(now.minusSeconds(60))) return false;

            entityManager.createQuery("DELETE FROM EmailOtp o WHERE o.user = :user AND o.createdAt < :cutoff")
                    .setParameter("user", user)
                    .setParameter("cutoff", now.minusDays(2))
                    .executeUpdate();

            EmailOtp otp = new EmailOtp();
            otp.setUser(user);
            otp.setPurpose(purpose);
            otp.setTargetEmail(null);
            otp.setCodeHash(codeHash);
            otp.setCreatedAt(now);
            otp.setExpiresAt(expiresAt);
            entityManager.persist(otp);
            return true;
        });
    }

    @Override
    public OtpResult activateWithOtp(String email, String code, LocalDateTime now) throws SQLException {
        return JpaSupport.write(entityManager -> {
            EmailOtp otp = latestOtp(entityManager, email, "ACTIVATION");
            if (otp == null) return OtpResult.NOT_FOUND;
            if (otp.isConsumed()) return OtpResult.ALREADY_DONE;
            OtpResult validation = validateCode(otp, code, now);
            if (validation != OtpResult.SUCCESS) return validation;
            if (otp.getUser().isActive()) {
                otp.setConsumed(true);
                return OtpResult.ALREADY_DONE;
            }
            otp.getUser().setActive(true);
            otp.setConsumed(true);
            return OtpResult.SUCCESS;
        });
    }

    @Override
    public OtpResult resetPasswordWithOtp(String email, String code, String passwordHash,
                                           LocalDateTime now) throws SQLException {
        return JpaSupport.write(entityManager -> {
            EmailOtp otp = latestOtp(entityManager, email, "PASSWORD_RESET");
            if (otp == null) return OtpResult.NOT_FOUND;
            if (otp.isConsumed()) return OtpResult.ALREADY_DONE;
            OtpResult validation = validateCode(otp, code, now);
            if (validation != OtpResult.SUCCESS) return validation;
            if (!otp.getUser().isActive()) return OtpResult.INVALID;
            otp.getUser().setPasswordHash(passwordHash);
            otp.setConsumed(true);
            return OtpResult.SUCCESS;
        });
    }

    @Override
    public String updateProfile(int userId, String fullName, String phone,
                                String profileImageUrl, String cloudinaryPublicId) throws SQLException {
        return JpaSupport.write(entityManager -> {
            User user = entityManager.find(User.class, userId);
            if (user == null) throw new IllegalArgumentException("The account no longer exists.");
            String previousPublicId = null;
            user.setFullName(fullName);
            user.setPhone(phone);
            if (profileImageUrl != null && cloudinaryPublicId != null) {
                previousPublicId = user.getCloudinaryPublicId();
                user.setProfileImageUrl(profileImageUrl);
                user.setCloudinaryPublicId(cloudinaryPublicId);
            }
            return previousPublicId;
        });
    }

    @Override
    public boolean requestEmailChangeOtp(int userId, String targetEmail, String codeHash,
                                         LocalDateTime now, LocalDateTime expiresAt) throws SQLException {
        return JpaSupport.write(entityManager -> {
            User user = entityManager.find(User.class, userId);
            if (user == null || !user.isActive()) return false;

            Long existingEmail = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", targetEmail)
                    .getSingleResult();
            if (existingEmail > 0) return false;

            Long recentCount = entityManager.createQuery(
                            "SELECT COUNT(o) FROM EmailOtp o WHERE o.user.id = :userId "
                                    + "AND o.purpose = :purpose AND o.createdAt >= :since", Long.class)
                    .setParameter("userId", userId)
                    .setParameter("purpose", "EMAIL_CHANGE")
                    .setParameter("since", now.minusHours(1))
                    .getSingleResult();
            if (recentCount >= 5) return false;

            List<EmailOtp> latest = entityManager.createQuery(
                            "SELECT o FROM EmailOtp o WHERE o.user.id = :userId "
                                    + "AND o.purpose = :purpose ORDER BY o.createdAt DESC, o.id DESC", EmailOtp.class)
                    .setParameter("userId", userId)
                    .setParameter("purpose", "EMAIL_CHANGE")
                    .setMaxResults(1)
                    .getResultList();
            if (!latest.isEmpty() && latest.get(0).getCreatedAt().isAfter(now.minusSeconds(60))) return false;

            entityManager.createQuery("DELETE FROM EmailOtp o WHERE o.user = :user AND o.createdAt < :cutoff")
                    .setParameter("user", user)
                    .setParameter("cutoff", now.minusDays(2))
                    .executeUpdate();

            user.setPendingEmail(targetEmail);
            EmailOtp otp = new EmailOtp();
            otp.setUser(user);
            otp.setPurpose("EMAIL_CHANGE");
            otp.setTargetEmail(targetEmail);
            otp.setCodeHash(codeHash);
            otp.setCreatedAt(now);
            otp.setExpiresAt(expiresAt);
            entityManager.persist(otp);
            return true;
        });
    }

    @Override
    public OtpResult changeEmailWithOtp(int userId, String code, LocalDateTime now) throws SQLException {
        return JpaSupport.write(entityManager -> {
            List<EmailOtp> matches = entityManager.createQuery(
                            "SELECT o FROM EmailOtp o JOIN FETCH o.user u WHERE u.id = :userId "
                                    + "AND o.purpose = :purpose ORDER BY o.createdAt DESC, o.id DESC", EmailOtp.class)
                    .setParameter("userId", userId)
                    .setParameter("purpose", "EMAIL_CHANGE")
                    .setMaxResults(1)
                    .getResultList();
            if (matches.isEmpty()) return OtpResult.NOT_FOUND;

            EmailOtp otp = matches.get(0);
            User user = otp.getUser();
            if (otp.isConsumed()) return OtpResult.ALREADY_DONE;
            OtpResult validation = validateCode(otp, code, now);
            if (validation != OtpResult.SUCCESS) return validation;
            String targetEmail = otp.getTargetEmail();
            if (targetEmail == null || targetEmail.isBlank()
                    || !targetEmail.equals(user.getPendingEmail())) {
                otp.setConsumed(true);
                return OtpResult.NOT_FOUND;
            }

            Long existingEmail = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.email = :email AND u.id <> :userId", Long.class)
                    .setParameter("email", targetEmail)
                    .setParameter("userId", userId)
                    .getSingleResult();
            if (existingEmail > 0) {
                user.setPendingEmail(null);
                otp.setConsumed(true);
                return OtpResult.EMAIL_TAKEN;
            }

            user.setEmail(targetEmail);
            user.setPendingEmail(null);
            otp.setConsumed(true);
            return OtpResult.SUCCESS;
        });
    }

    private EmailOtp latestOtp(jakarta.persistence.EntityManager entityManager, String email, String purpose) {
        List<EmailOtp> matches = entityManager.createQuery(
                        "SELECT o FROM EmailOtp o JOIN FETCH o.user u WHERE u.email = :email "
                                + "AND o.purpose = :purpose ORDER BY o.createdAt DESC, o.id DESC", EmailOtp.class)
                .setParameter("email", email)
                .setParameter("purpose", purpose)
                .setMaxResults(1)
                .getResultList();
        return matches.isEmpty() ? null : matches.get(0);
    }

    private OtpResult validateCode(EmailOtp otp, String code, LocalDateTime now) {
        if (!otp.getExpiresAt().isAfter(now)) {
            otp.setConsumed(true);
            return OtpResult.EXPIRED;
        }
        if (otp.getAttempts() >= MAX_OTP_ATTEMPTS) {
            otp.setConsumed(true);
            return OtpResult.TOO_MANY_ATTEMPTS;
        }
        if (!PasswordHasher.verifyPassword(code, otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= MAX_OTP_ATTEMPTS) {
                otp.setConsumed(true);
                return OtpResult.TOO_MANY_ATTEMPTS;
            }
            return OtpResult.INVALID;
        }
        return OtpResult.SUCCESS;
    }
}
