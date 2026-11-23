package vn.iotstar.dao;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import vn.iotstar.entity.User;
import vn.iotstar.service.OtpResult;

public interface UserDao {
    User findByUsername(String username) throws SQLException;
    User findByEmail(String email) throws SQLException;
    boolean existsByUsername(String username) throws SQLException;
    boolean existsByEmail(String email) throws SQLException;
    void insert(User user) throws SQLException;
    void saveRememberToken(String tokenHash, int userId, Instant expiresAt) throws SQLException;
    User findByValidRememberToken(String tokenHash) throws SQLException;
    void deleteRememberToken(String tokenHash) throws SQLException;
    boolean createOtp(int userId, String purpose, String codeHash,
                      LocalDateTime now, LocalDateTime expiresAt) throws SQLException;
    OtpResult activateWithOtp(String email, String code, LocalDateTime now) throws SQLException;
    OtpResult resetPasswordWithOtp(String email, String code, String passwordHash,
                                   LocalDateTime now) throws SQLException;
    String updateProfile(int userId, String fullName, String phone,
                         String profileImageUrl, String cloudinaryPublicId) throws SQLException;
    boolean requestEmailChangeOtp(int userId, String targetEmail, String codeHash,
                                  LocalDateTime now, LocalDateTime expiresAt) throws SQLException;
    OtpResult changeEmailWithOtp(int userId, String code, LocalDateTime now) throws SQLException;
}
