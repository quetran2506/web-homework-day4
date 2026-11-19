package vn.iotstar.service;

import java.sql.SQLException;
import vn.iotstar.entity.User;

public interface UserService {
    User login(String username, String password) throws SQLException;
    RegistrationStatus register(String fullName, String email, String username,
                                String password, String phone) throws SQLException;
    OtpRequestStatus requestActivation(String email) throws SQLException;
    OtpRequestStatus requestPasswordReset(String email) throws SQLException;
    OtpResult activate(String email, String code) throws SQLException;
    OtpResult resetPassword(String email, String code, String password) throws SQLException;
    String issueRememberToken(User user) throws SQLException;
    User findByRememberToken(String token) throws SQLException;
    void revokeRememberToken(String token) throws SQLException;
}
