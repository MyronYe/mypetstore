package org.csu.mypetstore.controller;

import org.csu.mypetstore.domain.Category;
import org.csu.mypetstore.domain.Item;
import org.csu.mypetstore.domain.Product;
import org.csu.mypetstore.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller

public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    private final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    private List<String> list1;
    private List<String> list2;
    private List<String> list3;

    @GetMapping("/catalog/main")
    public String viewMain(HttpSession session) {
        list1 = new ArrayList<>();
        list2 = new ArrayList<>();
        list3 = new ArrayList<>();

        list1.add("english");
        list1.add("chinese");
        list1.add("japanese");

        list2.add("FISH");
        list2.add("DOGS");
        list2.add("REPTILES");
        list2.add("CATS");
        list2.add("BIRDS");

        list3.add("Visa");
        list3.add("MasterCard");
        list3.add("American Express");

        session.setAttribute("list1",list1);
        session.setAttribute("list2",list2);
        session.setAttribute("list3",list3);

        logger.info("Enter main page");
        return "catalog/main";
    }

    //如何从客户端网页获取值，@RequestParam；如何将服务器端控制器中的值传给客户端网页，Model
    @GetMapping("/catalog/category")
        public String viewCatagory(@RequestParam("categoryId") String categoryId, Model model,HttpSession session) {

        if(categoryId != null) {
            Category category = catalogService.getCategory(categoryId);
            List<Product> productList = catalogService.getProductListByCategory(categoryId);
            model.addAttribute("category",category);
            session.setAttribute("category",category);
            model.addAttribute("productList",productList);
            session.setAttribute("productList",productList);
        }
        logger.info("View category: " + categoryId);
        return "catalog/category";
    }

    @GetMapping("/catalog/product")
    public String viewProduct(@RequestParam("productId") String productId, Model model,HttpSession session) {
        if(productId != null) {
            Product product = catalogService.getProduct(productId);
            List<Item> itemList = catalogService.getItemListByProduct(productId);
            model.addAttribute("product",product);
            session.setAttribute("product",product);
            model.addAttribute("itemList",itemList);
            session.setAttribute("itemList",itemList);
        }
        logger.info("View product: " + productId);
        return "catalog/product";
    }

    @GetMapping("/catalog/item")
    public String viewItem(@RequestParam("itemId") String itemId, Model model, HttpSession session) {
        if(itemId != null) {
            Item item = catalogService.getItem(itemId);
            model.addAttribute("item",item);
            session.setAttribute("item",item);
        }
        logger.info("View item: " + itemId);
        return "catalog/item";
    }

    @PostMapping("/catalog/searchProducts")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model,HttpSession session) {
        if(keyword != null && keyword.length() >= 1) {
            List<Product> productList = catalogService.searchProductList(keyword);
            session.setAttribute("productList",productList);
            model.addAttribute("productList",productList);
            logger.info("Search product: " + keyword);
            return "catalog/searchProducts";
        }else {
            String message = "Please enter a keyword to search for, then press the search button.";
            model.addAttribute("message",message);
            return "/common/error";
        }
    }

}
