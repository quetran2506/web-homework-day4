package vn.iotstar.service;

public enum EmailChangeRequestStatus {
    SENT,
    EMAIL_FAILED,
    RATE_LIMITED,
    EMAIL_TAKEN,
    SAME_EMAIL,
    INVALID_EMAIL,
    NOT_FOUND
}
