package vn.iotstar.config;

import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Video;

/** Manual JPA smoke test based on the teacher's Category/Video example. */
public final class TestJpa {
    private TestJpa() {
    }

    public static void main(String[] args) {
        String videoId = UUID.randomUUID().toString();
        int categoryId = 0;

        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                Category category = new Category();
                category.setName("JPA smoke test " + videoId.substring(0, 8));
                category.setStatus(1);
                entityManager.persist(category);
                entityManager.flush();
                categoryId = category.getId();

                Video video = new Video();
                video.setVideoId(videoId);
                video.setTitle("JPA relation smoke test");
                video.setActive(1);
                video.setCategory(category);
                entityManager.persist(video);
                transaction.commit();

                entityManager.clear();
                Video savedVideo = entityManager.find(Video.class, videoId);
                if (savedVideo == null || savedVideo.getCategory().getId() != categoryId) {
                    throw new IllegalStateException("JPA could not read back the Video-Category relationship.");
                }

                transaction.begin();
                entityManager.remove(entityManager.find(Video.class, videoId));
                entityManager.remove(entityManager.find(Category.class, categoryId));
                transaction.commit();
                System.out.println("JPA test passed: Category and Video were persisted, read, and removed.");
            } catch (RuntimeException ex) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw ex;
            }
        } finally {
            JpaConfig.shutdown();
        }
    }
}
