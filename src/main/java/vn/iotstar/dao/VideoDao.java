package vn.iotstar.dao;

import java.sql.SQLException;
import java.util.List;
import vn.iotstar.entity.Video;

public interface VideoDao {
    List<Video> findLatest(int limit) throws SQLException;
    List<Video> findPublicPage(Integer userId, int offset, int pageSize) throws SQLException;
    int countPublic(Integer userId) throws SQLException;
    List<Video> findOwnedByUser(int userId) throws SQLException;
    Video findPublishedById(String id) throws SQLException;
    Video findOwnedById(String id, int userId) throws SQLException;
    void insert(Video video) throws SQLException;
    boolean updateOwned(Video video, int userId) throws SQLException;
    boolean deleteOwned(String id, int userId) throws SQLException;
}
