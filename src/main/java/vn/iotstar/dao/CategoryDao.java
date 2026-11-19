package vn.iotstar.dao;

import java.sql.SQLException;
import java.util.List;
import vn.iotstar.entity.Category;

public interface CategoryDao {
    void insert(Category category) throws SQLException;
    void update(Category category) throws SQLException;
    void delete(int id) throws SQLException;
    Category findById(int id) throws SQLException;
    Category findByName(String name) throws SQLException;
    List<Category> findAll() throws SQLException;
    List<Category> search(String keyword) throws SQLException;
    List<Category> findAll(int pageIndex, int pageSize) throws SQLException;
    List<Category> search(String keyword, int pageIndex, int pageSize) throws SQLException;
    int count() throws SQLException;
    int count(String keyword) throws SQLException;
}
