package org.csu.mypetstore.service;

import org.csu.mypetstore.domain.Category;
import org.csu.mypetstore.domain.Item;
import org.csu.mypetstore.domain.Product;

import java.util.List;

public interface CatalogService {
    List<Category> getCategoryList();

    Category getCategory(String categoryId);

    Product getProduct(String productId);

    List<Product> getProductListByCategory(String categoryId);

    // TODO enable using more than one keyword
    List<Product> searchProductList(String keyword);

    List<Item> getItemListByProduct(String productId);

    Item getItem(String itemId);

    boolean isItemInStock(String itemId);
}
