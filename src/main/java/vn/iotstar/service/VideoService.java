package vn.iotstar.service;

import java.sql.SQLException;
import java.util.List;
import vn.iotstar.entity.Video;

public interface VideoService {
    List<Video> latest(int limit) throws SQLException;
    List<Video> publicPage(Integer userId, int page, int pageSize) throws SQLException;
    int countPublic(Integer userId) throws SQLException;
    List<Video> ownedBy(int userId) throws SQLException;
    Video published(String id) throws SQLException;
    Video owned(String id, int userId) throws SQLException;
    void create(Video video) throws SQLException;
    boolean update(Video video, int userId) throws SQLException;
    boolean delete(String id, int userId) throws SQLException;
}
