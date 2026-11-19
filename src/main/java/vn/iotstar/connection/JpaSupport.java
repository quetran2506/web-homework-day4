package vn.iotstar.connection;

import java.sql.SQLException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.config.JpaConfig;

public final class JpaSupport {
    @FunctionalInterface
    public interface Work<T> {
        T execute(EntityManager entityManager);
    }

    private JpaSupport() {
    }

    public static <T> T read(Work<T> work) throws SQLException {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            return work.execute(entityManager);
        } catch (RuntimeException ex) {
            throw new SQLException("JPA read operation failed.", ex);
        }
    }

    public static <T> T write(Work<T> work) throws SQLException {
        try (EntityManager entityManager = JpaConfig.getEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                T result = work.execute(entityManager);
                transaction.commit();
                return result;
            } catch (RuntimeException ex) {
                if (transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (RuntimeException rollbackFailure) {
                        ex.addSuppressed(rollbackFailure);
                    }
                }
                throw ex;
            }
        } catch (RuntimeException ex) {
            throw new SQLException("JPA write operation failed.", ex);
        }
    }
}
