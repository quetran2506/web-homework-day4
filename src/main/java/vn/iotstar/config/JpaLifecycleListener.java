package vn.iotstar.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class JpaLifecycleListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            JpaConfig.getEntityManagerFactory();
            event.getServletContext().log("JPA EntityManagerFactory initialized.");
        } catch (RuntimeException ex) {
            event.getServletContext().log("Could not initialize JPA. Check DB_URL, DB_USER, and DB_PASSWORD.", ex);
            throw ex;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        JpaConfig.shutdown();
    }
}
