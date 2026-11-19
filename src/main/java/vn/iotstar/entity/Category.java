package vn.iotstar.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categories")
@NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c ORDER BY c.id DESC")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryId")
    private int id;

    @NotBlank(message = "Tên danh mục không được để trống.")
    @Size(max = 255, message = "Tên danh mục tối đa 255 ký tự.")
    @Column(name = "CategoryName", length = 255, columnDefinition = "nvarchar(255) not null")
    private String name;

    @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự.")
    @Column(name = "Images", length = 500, columnDefinition = "nvarchar(500) null")
    private String icon;

    @Column(name = "status", nullable = false)
    private int status = 1;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Video> videos = new ArrayList<>();

    public Category() {
    }

    public Category(int id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public List<Video> getVideos() { return videos; }
    public void setVideos(List<Video> videos) { this.videos = videos; }

    public Video addVideo(Video video) {
        videos.add(video);
        video.setCategory(this);
        return video;
    }

    public Video removeVideo(Video video) {
        videos.remove(video);
        video.setCategory(null);
        return video;
    }
}
