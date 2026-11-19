package vn.iotstar.service.impl;

import java.sql.SQLException;
import java.util.List;
import vn.iotstar.dao.VideoDao;
import vn.iotstar.dao.impl.VideoDaoImpl;
import vn.iotstar.entity.Video;
import vn.iotstar.service.VideoService;

public class VideoServiceImpl implements VideoService {
    private final VideoDao videoDao;

    public VideoServiceImpl() { this(new VideoDaoImpl()); }
    public VideoServiceImpl(VideoDao videoDao) { this.videoDao = videoDao; }

    @Override public List<Video> latest(int limit) throws SQLException { return videoDao.findLatest(limit); }
    @Override public List<Video> publicPage(Integer userId, int page, int pageSize) throws SQLException {
        return videoDao.findPublicPage(userId, Math.max(0, page - 1) * pageSize, pageSize);
    }
    @Override public int countPublic(Integer userId) throws SQLException { return videoDao.countPublic(userId); }
    @Override public List<Video> ownedBy(int userId) throws SQLException { return videoDao.findOwnedByUser(userId); }
    @Override public Video published(String id) throws SQLException { return videoDao.findPublishedById(id); }
    @Override public Video owned(String id, int userId) throws SQLException { return videoDao.findOwnedById(id, userId); }
    @Override public void create(Video video) throws SQLException { videoDao.insert(video); }
    @Override public boolean update(Video video, int userId) throws SQLException { return videoDao.updateOwned(video, userId); }
    @Override public boolean delete(String id, int userId) throws SQLException { return videoDao.deleteOwned(id, userId); }
}
