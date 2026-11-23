package vn.iotstar.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "OneTimeCodes")
public class EmailOtp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OtpId")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "Purpose", nullable = false, length = 24)
    private String purpose;

    @Column(name = "TargetEmail", length = 255)
    private String targetEmail;

    @Column(name = "CodeHash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "CreatedAt", nullable = false, columnDefinition = "datetime2(0)")
    private LocalDateTime createdAt;

    @Column(name = "ExpiresAt", nullable = false, columnDefinition = "datetime2(0)")
    private LocalDateTime expiresAt;

    @Column(name = "Attempts", nullable = false)
    private int attempts;

    @Column(name = "Consumed", nullable = false)
    private boolean consumed;

    @PrePersist
    private void setCreatedAt() {
        if (createdAt == null) createdAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    public long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public boolean isConsumed() { return consumed; }
    public void setConsumed(boolean consumed) { this.consumed = consumed; }
}
