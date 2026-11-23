package vn.iotstar.service;

public enum OtpResult {
    SUCCESS,
    INVALID,
    EXPIRED,
    TOO_MANY_ATTEMPTS,
    ALREADY_DONE,
    EMAIL_TAKEN,
    NOT_FOUND
}
