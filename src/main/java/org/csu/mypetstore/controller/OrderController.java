package org.csu.mypetstore.controller;

import org.csu.mypetstore.domain.Account;
import org.csu.mypetstore.domain.Cart;
import org.csu.mypetstore.domain.Order;
import org.csu.mypetstore.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    private Order order = new Order();
    private boolean shippingAddress;

    @Autowired
    private OrderService orderService;

    private final Logger logger = LoggerFactory.getLogger(OrderController.class);


    @GetMapping("/order/newOrderForm")
    public String newOrderForm(Model model, HttpSession session) {
        Account account = (Account)session.getAttribute("account");
        Cart cart = (Cart)session.getAttribute("cart");

        if(account == null) {
            return "/account/signonForm";
        }else {
            if(cart != null) {
                order.initOrder(account,cart);
                model.addAttribute("order",order);
                session.setAttribute("order",order);
                logger.info("User " + account.getUsername() + " enters the order page.");
                return "/order/newOrderForm";
            }else {
                String message = "An order could not be created because a cart could not be found.";
                model.addAttribute("message",message);
                return "/common/error";
            }
        }
    }

    @PostMapping("/order/newOrder")
    public String newOrder(HttpServletRequest request, Order order, Model model, HttpSession session) {

//        @RequestParam("shippingAddressRequired") String shippingAddressRequired,

        Order newOrder = new Order();
        newOrder = (Order)session.getAttribute("order");

        newOrder.setCardType(order.getCardType());
        newOrder.setCreditCard(order.getCreditCard());
        newOrder.setExpiryDate(order.getExpiryDate());

        newOrder.setOrderDate(new Date());
        newOrder.setBillToFirstName(order.getBillToFirstName());
        newOrder.setBillToLastName(order.getBillToLastName());
        newOrder.setBillAddress1(order.getBillAddress1());
        newOrder.setBillAddress2(order.getBillAddress2());
        newOrder.setBillCity(order.getBillCity());
        newOrder.setBillState(order.getBillState());
        newOrder.setBillZip(order.getBillZip());
        newOrder.setBillCountry(order.getBillCountry());

        String shippingAddressRequired = request.getParameter("shippingAddressRequired");
        shippingAddress = shippingAddressRequired != null;
        if(shippingAddress) {
            model.addAttribute("order",newOrder);
            session.setAttribute("order",newOrder);
            logger.info("Enter the shippingForm.");
            return "/order/shippingForm";
        }else {
            newOrder.setShipToFirstName(order.getBillToFirstName());
            newOrder.setShipToLastName(order.getBillToLastName());
            newOrder.setShipAddress1(order.getBillAddress1());
            newOrder.setShipAddress2(order.getBillAddress2());
            newOrder.setShipCity(order.getBillCity());
            newOrder.setShipState(order.getBillState());
            newOrder.setShipZip(order.getBillZip());
            newOrder.setShipCountry(order.getBillCountry());
            model.addAttribute("order",newOrder);
            session.setAttribute("order",newOrder);
            logger.info("Confirm the order.");
            return "/order/confirmOrder";
        }
    }

    @GetMapping("/order/confirmOrder")
    public String confirmOrder(Model model, HttpSession session) {
        Order newOrder = (Order)session.getAttribute("order");
        Account account = (Account)session.getAttribute("account");

        if(newOrder.getUsername().equals(account.getUsername())) {
            orderService.insertOrder(newOrder);
            logger.info("Order confirmed.");
            return "/order/viewOrder";
        }else {
            String message = "You may only view your own orders.";
            model.addAttribute("message",message);
            return "/common/error";
        }
    }

    @GetMapping("/order/listOrders")
    public String listOrders(Model model, HttpSession session) {
        Account account = (Account)session.getAttribute("account");
        List<Order> orderList = orderService.getOrdersByUsername(account.getUsername());
        model.addAttribute("orderList",orderList);
        session.setAttribute("orderList",orderList);
        logger.info("List orders.");
        return "/order/listOrders";
    }
    @PostMapping("/order/shipping")
    public String shipping(Order order, Model model, HttpSession session) {
        Order newOrder = new Order();
        newOrder = (Order)session.getAttribute("order");

        newOrder.setShipToFirstName(order.getShipToFirstName());
        newOrder.setShipToLastName(order.getShipToLastName());
        newOrder.setShipAddress1(order.getShipAddress1());
        newOrder.setShipAddress2(order.getShipAddress2());
        newOrder.setShipCity(order.getShipCity());
        newOrder.setShipState(order.getShipState());
        newOrder.setShipZip(order.getShipZip());
        newOrder.setShipCountry(order.getShipCountry());

        model.addAttribute("order",newOrder);
        session.setAttribute("order",newOrder);

        logger.info("Confirm order.");
        return "/order/confirmOrder";
    }

    @GetMapping("/order/viewOrder")
    public String viewOrder(@RequestParam("orderId") int orderId,  Model model, HttpSession session) {
        Account account = (Account)session.getAttribute("account");

        Order order = orderService.getOrder(orderId);

        if(account.getUsername().equals(order.getUsername())) {
            model.addAttribute("order",order);
            session.setAttribute("order",order);
            logger.info("View order.");
            return "/order/viewOrder";
        }else {
            String message = "You may only view your own orders.";
            model.addAttribute("message",message);
            return "/common/error";
        }
    }

}
