package vn.iotstar.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

@Entity
@Table(name = "Videos")
@NamedQuery(name = "Video.findAll", query = "SELECT v FROM Video v")
public class Video implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "VideoId", length = 64)
    private String videoId;

    @Column(name = "Active", nullable = false)
    private int active = 1;

    @Column(name = "Description", columnDefinition = "nvarchar(max) null")
    private String description;

    @Column(name = "Poster", length = 500, columnDefinition = "nvarchar(500) null")
    private String poster;

    @Column(name = "Title", length = 500, columnDefinition = "nvarchar(500) null")
    private String title;

    @Column(name = "Views", nullable = false)
    private int views;

    @Column(name = "VideoFile", length = 255)
    private String videoFile;

    @Column(name = "CreatedAt", nullable = false, columnDefinition = "datetime2(0)")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId")
    private Category category;

    public Video() {
    }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    public int getActive() { return active; }
    public void setActive(int active) { this.active = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public String getVideoFile() { return videoFile; }
    public void setVideoFile(String videoFile) { this.videoFile = videoFile; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    @PrePersist
    private void setCreationTime() {
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
