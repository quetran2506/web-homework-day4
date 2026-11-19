package vn.iotstar.service;

public enum OtpRequestStatus {
    SENT,
    EMAIL_FAILED,
    RATE_LIMITED,
    ALREADY_ACTIVE,
    NOT_FOUND
}
