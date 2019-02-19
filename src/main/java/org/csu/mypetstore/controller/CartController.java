package org.csu.mypetstore.controller;

import org.csu.mypetstore.domain.Cart;
import org.csu.mypetstore.domain.CartItem;
import org.csu.mypetstore.domain.Item;
import org.csu.mypetstore.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.Iterator;

@Controller
public class CartController {

    private Cart cart;

    @Autowired
    private CatalogService catalogService;
    private final Logger logger = LoggerFactory.getLogger(CartController.class);

    @GetMapping("/cart/cart")
    public String viewCart(Model model, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
            model.addAttribute("cart", cart);
        }
        logger.info("View cart.");
        return "/cart/cart";
    }

    @GetMapping("/cart/addItemToCart")
    public String addItemToCart(@RequestParam("workingItemId") String workingItemId, Model model, HttpSession session) {
        cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }
        if (cart.containsItemId(workingItemId)) {
            cart.incrementQuantityByItemId(workingItemId);
        } else {
            boolean isInStock = catalogService.isItemInStock(workingItemId);
            Item item = catalogService.getItem(workingItemId);
            cart.addItem(item, isInStock);
        }
        model.addAttribute("cart", cart);
        session.setAttribute("cart", cart);
        logger.info("add " + workingItemId + " to cart.");
        return "/cart/cart";
    }

    @GetMapping("/cart/removeItemFromCart")
    public String removeItemFromCart(@RequestParam("workingItemId") String workingItemId, Model model, HttpSession session) {
        cart = (Cart) session.getAttribute("cart");
        Item item = cart.removeItemById(workingItemId);
        if (item == null) {
            String message = "Attempted to remove null CartItem from Cart.";
            model.addAttribute("message", message);
            return "/common/error";
        } else {
            model.addAttribute("cart",cart);
            session.setAttribute("cart",cart);
            logger.info("Remove " + workingItemId + " form cart.");
            return "/cart/cart";
        }
    }

    @PostMapping("/cart/updateCartQuantities")
    public String updateCartQuantities(Model model, HttpSession session, HttpServletRequest request) {
        cart = (Cart) session.getAttribute("cart");
        Iterator<CartItem> cartItems = cart.getAllCartItems();
        while (cartItems.hasNext()) {
            CartItem cartItem = cartItems.next();
            String itemId = cartItem.getItem().getItemId();
            try {
                int quantity = Integer.parseInt(request.getParameter(itemId));
                cart.setQuantityByItemId(itemId, quantity);
                if (quantity < 1) {
                    cartItems.remove();
                }
                model.addAttribute("cart", cart);
                session.setAttribute("cart", cart);
            } catch (Exception e) {
                String message = "The Quantities of Item must be Integer!";
                model.addAttribute("message", message);
                return "/common/error";
            }
        }
        logger.info("Update cart.");
        return "/cart/cart";
    }

    @GetMapping("/cart/cartItemValidation")
    public void cartItemValidation(HttpSession session, HttpServletResponse response, HttpServletRequest request)throws Exception {
        cart = (Cart) session.getAttribute("cart");
        if (cart == null) return;
        Iterator<CartItem> cartItems = cart.getAllCartItems();
        String eleId = request.getParameter("eleId");

        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();

        while (cartItems.hasNext()) {
            CartItem cartItem = cartItems.next();
            String itemId = cartItem.getItem().getItemId();

            if (eleId.equals(itemId)) {
                int quantity = Integer.parseInt((String) request.getParameter("quantity"));
                cart.setQuantityByItemId(itemId, quantity);
                if (quantity < 1) {
                    cartItems.remove();
                    out.println("<msg>NotExist" + "#" + cart.getSubTotal() + "</msg>");
                } else {
                    out.println("<msg>" + cartItem.getTotal() + "#" + cart.getSubTotal() + "</msg>");
                }
                break;
            }
        }
            cart.getSubTotal();
            session.setAttribute("cart", cart);
            out.flush();
            out.close();
            logger.info("Update cart.");
            return;
    }
}
