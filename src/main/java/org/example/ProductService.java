package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductService {
    private List<Product> products = new ArrayList<>();

    public void createProduct(Product product) {
        products.add(product);
    }

    public Product readProduct(String id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void updateProduct(Product updatedProduct) {
        products = products.stream()
                .map(product -> product.getId().equals(updatedProduct.getId()) ? updatedProduct : product)
                .collect(Collectors.toList());
    }

    public void deleteProduct(String id) {
        products = products.stream()
                .filter(product -> !product.getId().equals(id))
                .collect(Collectors.toList());
    }

    public List<Product> listProductsByCriteria(Criteria criteria) {
        return products.stream()
                .filter(product ->
                        (criteria.getName() == null || product.getName().equals(criteria.getName())) &&
                                (criteria.getMaxPrice() == null || product.getPrice() <= criteria.getMaxPrice()) &&
                                (criteria.getMinPrice() == null || product.getPrice() >= criteria.getMinPrice()) &&
                                (criteria.getMaxQuantity() == null || product.getQuantity() <= criteria.getMaxQuantity()) &&
                                (criteria.getMinQuantity() == null || product.getQuantity() >= criteria.getMinQuantity()))
                .collect(Collectors.toList());
    }
}
