package org.example;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ProductServiceTest {
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        productService = new ProductService();
    }

    @Test
    public void testCreateProduct() {
        Product product = new Product("1", "Product A", 100.0, 10);
        productService.createProduct(product);
        assertEquals(product, productService.readProduct("1"));
    }

    @Test
    public void testReadProduct() {
        Product product = new Product("2", "Product B", 200.0, 20);
        productService.createProduct(product);
        Product retrievedProduct = productService.readProduct("2");
        assertNotNull(retrievedProduct);
        assertEquals("Product B", retrievedProduct.getName());
    }

    @Test
    public void testUpdateProduct() {
        Product product = new Product("3", "Product C", 300.0, 30);
        productService.createProduct(product);
        Product updatedProduct = new Product("3", "Product C Updated", 350.0, 35);
        productService.updateProduct(updatedProduct);
        Product retrievedProduct = productService.readProduct("3");
        assertEquals("Product C Updated", retrievedProduct.getName());
        assertEquals(350.0, retrievedProduct.getPrice());
        assertEquals(35, retrievedProduct.getQuantity());
    }

    @Test
    public void testDeleteProduct() {
        Product product = new Product("4", "Product D", 400.0, 40);
        productService.createProduct(product);
        productService.deleteProduct("4");
        assertNull(productService.readProduct("4"));
    }

    @Test
    public void testListProductsByCriteria() {
        Product product1 = new Product("5", "Product E", 500.0, 50);
        Product product2 = new Product("6", "Product F", 600.0, 60);
        Product product3 = new Product("7", "Product G", 700.0, 70);
        productService.createProduct(product1);
        productService.createProduct(product2);
        productService.createProduct(product3);

        Criteria criteria = new Criteria(null, 600.0, null, null, 70);
        List<Product> products = productService.listProductsByCriteria(criteria);
        assertEquals(2, products.size());
        assertTrue(products.contains(product2));
        assertTrue(products.contains(product3));
    }
}
