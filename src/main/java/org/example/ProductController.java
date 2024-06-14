package org.example;

import java.util.List;

public class ProductController {
    private ProductService productService = new ProductService();

    public void createProduct(String id, String name, double price, int quantity) {
        Product product = new Product(id, name, price, quantity);
        productService.createProduct(product);
    }

    public Product readProduct(String id) {
        return productService.readProduct(id);
    }

    public void updateProduct(String id, String name, double price, int quantity) {
        Product product = new Product(id, name, price, quantity);
        productService.updateProduct(product);
    }

    public void deleteProduct(String id) {
        productService.deleteProduct(id);
    }

    public List<Product> listProductsByCriteria(Criteria criteria) {
        return productService.listProductsByCriteria(criteria);
    }
}
