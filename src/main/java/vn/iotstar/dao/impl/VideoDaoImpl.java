package vn.iotstar.dao.impl;

import java.sql.SQLException;
import java.util.List;
import vn.iotstar.connection.JpaSupport;
import vn.iotstar.dao.VideoDao;
import vn.iotstar.entity.User;
import vn.iotstar.entity.Video;

public class VideoDaoImpl implements VideoDao {
    @Override
    public List<Video> findLatest(int limit) throws SQLException {
        return JpaSupport.read(em -> em.createQuery(
                        "SELECT v FROM Video v JOIN FETCH v.user WHERE v.active = 1 "
                                + "ORDER BY v.createdAt DESC, v.videoId DESC", Video.class)
                .setMaxResults(Math.max(1, limit)).getResultList());
    }

    @Override
    public List<Video> findPublicPage(Integer userId, int offset, int pageSize) throws SQLException {
        return JpaSupport.read(em -> {
            String jpql = "SELECT v FROM Video v JOIN FETCH v.user WHERE v.active = 1";
            if (userId != null) jpql += " AND v.user.id = :userId";
            jpql += " ORDER BY v.createdAt DESC, v.videoId DESC";
            var query = em.createQuery(jpql, Video.class);
            if (userId != null) query.setParameter("userId", userId);
            return query.setFirstResult(Math.max(0, offset)).setMaxResults(Math.max(1, pageSize)).getResultList();
        });
    }

    @Override
    public int countPublic(Integer userId) throws SQLException {
        return JpaSupport.read(em -> {
            String jpql = "SELECT COUNT(v) FROM Video v WHERE v.active = 1";
            if (userId != null) jpql += " AND v.user.id = :userId";
            var query = em.createQuery(jpql, Long.class);
            if (userId != null) query.setParameter("userId", userId);
            return query.getSingleResult().intValue();
        });
    }

    @Override
    public List<Video> findOwnedByUser(int userId) throws SQLException {
        return JpaSupport.read(em -> em.createQuery(
                        "SELECT v FROM Video v JOIN FETCH v.user WHERE v.user.id = :userId "
                                + "ORDER BY v.createdAt DESC, v.videoId DESC", Video.class)
                .setParameter("userId", userId).getResultList());
    }

    @Override
    public Video findPublishedById(String id) throws SQLException {
        return JpaSupport.read(em -> em.createQuery(
                        "SELECT v FROM Video v JOIN FETCH v.user WHERE v.videoId = :id AND v.active = 1", Video.class)
                .setParameter("id", id).getResultList().stream().findFirst().orElse(null));
    }

    @Override
    public Video findOwnedById(String id, int userId) throws SQLException {
        return JpaSupport.read(em -> em.createQuery(
                        "SELECT v FROM Video v JOIN FETCH v.user WHERE v.videoId = :id AND v.user.id = :userId", Video.class)
                .setParameter("id", id).setParameter("userId", userId)
                .getResultList().stream().findFirst().orElse(null));
    }

    @Override
    public void insert(Video video) throws SQLException {
        JpaSupport.write(em -> {
            em.persist(video);
            em.flush();
            return null;
        });
    }

    @Override
    public boolean updateOwned(Video video, int userId) throws SQLException {
        return JpaSupport.write(em -> {
            Video existing = em.find(Video.class, video.getVideoId());
            if (existing == null || existing.getUser() == null || existing.getUser().getId() != userId) return false;
            existing.setTitle(video.getTitle());
            existing.setDescription(video.getDescription());
            existing.setPoster(video.getPoster());
            existing.setVideoFile(video.getVideoFile());
            existing.setActive(video.getActive());
            return true;
        });
    }

    @Override
    public boolean deleteOwned(String id, int userId) throws SQLException {
        return JpaSupport.write(em -> {
            Video existing = em.find(Video.class, id);
            if (existing == null || existing.getUser() == null || existing.getUser().getId() != userId) return false;
            em.remove(existing);
            return true;
        });
    }
}
