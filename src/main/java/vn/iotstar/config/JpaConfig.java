package vn.iotstar.config;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaConfig {
    public static final String PERSISTENCE_UNIT = "jpa-hibernate-sqlserver";
    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=webst2;encrypt=true;trustServerCertificate=true";
    private static volatile EntityManagerFactory entityManagerFactory;

    private JpaConfig() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory current = entityManagerFactory;
        if (current == null || !current.isOpen()) {
            synchronized (JpaConfig.class) {
                current = entityManagerFactory;
                if (current == null || !current.isOpen()) {
                    entityManagerFactory = current = buildEntityManagerFactory();
                }
            }
        }
        return current;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        entityManagerFactory = null;
    }

    private static EntityManagerFactory buildEntityManagerFactory() {
        String username = setting("DB_USER", "");
        if (username.isBlank()) {
            throw new IllegalStateException("DB_USER is missing. Set SQL Server credentials before starting Tomcat.");
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        properties.put("jakarta.persistence.jdbc.url", setting("DB_URL", DEFAULT_URL));
        properties.put("jakarta.persistence.jdbc.user", username);
        properties.put("jakarta.persistence.jdbc.password", setting("DB_PASSWORD", ""));
        properties.put("hibernate.hbm2ddl.auto", setting("JPA_DDL_AUTO", "update"));
        properties.put("hibernate.show_sql", setting("JPA_SHOW_SQL", "true"));
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, properties);
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
