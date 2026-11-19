package vn.iotstar.service.impl;

import java.sql.SQLException;
import java.util.List;
import vn.iotstar.dao.CategoryDao;
import vn.iotstar.dao.impl.CategoryDaoImpl;
import vn.iotstar.entity.Category;
import vn.iotstar.service.CategoryService;

public class CategoryServiceImpl implements CategoryService {
    private final CategoryDao categoryDao;

    public CategoryServiceImpl() {
        this(new CategoryDaoImpl());
    }

    public CategoryServiceImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public void insert(Category category) throws SQLException {
        categoryDao.insert(category);
    }

    @Override
    public void edit(Category category) throws SQLException {
        categoryDao.update(category);
    }

    @Override
    public void delete(int id) throws SQLException {
        categoryDao.delete(id);
    }

    @Override
    public Category get(int id) throws SQLException {
        return categoryDao.findById(id);
    }

    @Override
    public Category get(String name) throws SQLException {
        return categoryDao.findByName(name);
    }

    @Override
    public List<Category> getAll() throws SQLException {
        return categoryDao.findAll();
    }

    @Override
    public List<Category> search(String keyword) throws SQLException {
        return categoryDao.search(keyword);
    }

    @Override
    public List<Category> getAll(int pageIndex, int pageSize) throws SQLException {
        return categoryDao.findAll(pageIndex, pageSize);
    }

    @Override
    public List<Category> search(String keyword, int pageIndex, int pageSize) throws SQLException {
        return categoryDao.search(keyword, pageIndex, pageSize);
    }

    @Override
    public int count() throws SQLException {
        return categoryDao.count();
    }

    @Override
    public int count(String keyword) throws SQLException {
        return categoryDao.count(keyword);
    }
}
