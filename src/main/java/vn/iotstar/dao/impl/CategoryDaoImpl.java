package vn.iotstar.dao.impl;

import java.sql.SQLException;
import java.util.List;
import vn.iotstar.connection.JpaSupport;
import vn.iotstar.dao.CategoryDao;
import vn.iotstar.entity.Category;

public class CategoryDaoImpl implements CategoryDao {
    @Override
    public void insert(Category category) throws SQLException {
        JpaSupport.write(entityManager -> {
            entityManager.persist(category);
            entityManager.flush();
            return null;
        });
    }

    @Override
    public void update(Category category) throws SQLException {
        JpaSupport.write(entityManager -> {
            entityManager.merge(category);
            return null;
        });
    }

    @Override
    public void delete(int id) throws SQLException {
        JpaSupport.write(entityManager -> {
            Category category = entityManager.find(Category.class, id);
            if (category != null) {
                entityManager.remove(category);
            }
            return null;
        });
    }

    @Override
    public Category findById(int id) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager.find(Category.class, id));
    }

    @Override
    public Category findByName(String name) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createQuery("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)", Category.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList().stream().findFirst().orElse(null));
    }

    @Override
    public List<Category> findAll() throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createNamedQuery("Category.findAll", Category.class)
                .getResultList());
    }

    @Override
    public List<Category> search(String keyword) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createQuery("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(:keyword) "
                        + "ORDER BY c.id DESC", Category.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList());
    }

    @Override
    public List<Category> findAll(int pageIndex, int pageSize) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createNamedQuery("Category.findAll", Category.class)
                .setFirstResult(Math.max(0, pageIndex) * Math.max(1, pageSize))
                .setMaxResults(Math.max(1, pageSize))
                .getResultList());
    }

    @Override
    public List<Category> search(String keyword, int pageIndex, int pageSize) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createQuery("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(:keyword) "
                        + "ORDER BY c.id DESC", Category.class)
                .setParameter("keyword", "%" + keyword + "%")
                .setFirstResult(Math.max(0, pageIndex) * Math.max(1, pageSize))
                .setMaxResults(Math.max(1, pageSize))
                .getResultList());
    }

    @Override
    public int count() throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createQuery("SELECT COUNT(c) FROM Category c", Long.class)
                .getSingleResult().intValue());
    }

    @Override
    public int count(String keyword) throws SQLException {
        return JpaSupport.read(entityManager -> entityManager
                .createQuery("SELECT COUNT(c) FROM Category c WHERE LOWER(c.name) LIKE LOWER(:keyword)",
                        Long.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getSingleResult().intValue());
    }
}
