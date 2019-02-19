package org.csu.mypetstore.controller;

import com.sun.xml.internal.ws.resources.HttpserverMessages;
import org.csu.mypetstore.domain.Account;
import org.csu.mypetstore.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    private final Logger logger = LoggerFactory.getLogger(AccountController.class);
    //生成随机颜色
    public Color getRandColor(int s, int e) {
        Random random = new Random();
        if (s > 255)
            s = 255;
        if (e > 255)
            e = 255;
        int r, g, b;
        r = s + random.nextInt(e - s); // 随机生成RGB颜色中的r值
        g = s + random.nextInt(e - s); // 随机生成RGB颜色中的g值
        b = s + random.nextInt(e - s); // 随机生成RGB颜色中的b值
        return new Color(r, g, b);
    }

    private Random r = new Random();
    // 随机字符集合中不包括0和o，O，1和l，因为这些不易区分
    private String codes = "23456789abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYXZ";

    private char randomChar() {
        int index = r.nextInt(codes.length());
        return codes.charAt(index);
    }

    @GetMapping("/account/signonForm")
    public String signonForm() {
        logger.info("Enter the login page.");
        return "/account/signonForm";
    }

    @PostMapping("/account/signon")
    public String signon(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("checkCode") String checkCode, Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response)throws Exception {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        Account account = accountService.getAccount(username,password);

        if(account == null) {
            String message = "Invalid username or password.  Signon failed.";
            model.addAttribute("message",message);
            logger.info("Invalid username or password.  Signon failed.");
            return "/common/error";
        }else if(!checkCode.equalsIgnoreCase((String)session.getAttribute("randCheckCode"))) {
            request.setAttribute("errormsg", "验证码不正确");
            response.getWriter().println("<script> type='text/javascript'>window.alert('*验证码错误！')</script>");
            return "/account/signonForm";
        }else {
            model.addAttribute("account",account);
            session.setAttribute("account",account);
            logger.info("User " + account.getUsername() + " login successful.");
            return "/catalog/main";
        }
    }

    @GetMapping("/account/pictureCheckCode")
    public void pictureCheckCode(HttpSession session, HttpServletRequest request, HttpServletResponse response)throws Exception {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        //设置不缓存图片
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "No-cache");
        response.setDateHeader("Expires", 0);

        //指定生成的响应图片
        response.setContentType("image/jpeg");
        int width = 80, height = 35; // 指定生成验证码的宽度和高度
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // 创建BufferedImage对象,其作用相当于一图片
        Graphics g = image.getGraphics(); // 创建Graphics对象,其作用相当于画笔
        Graphics2D g2d = (Graphics2D) g; // 创建Grapchics2D对象

        Random random = new Random();
        Font mfont = new Font("楷体", Font.BOLD, 16); // 定义字体样式
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height); // 绘制背景
        g.setFont(mfont); // 设置字体
        g.setColor(getRandColor(180, 200));

        // 绘制100条颜色和位置全部为随机产生的线条,该线条为2f
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(width - 1);
            int y = random.nextInt(height - 1);
            int x1 = random.nextInt(6) + 1;
            int y1 = random.nextInt(12) + 1;
            BasicStroke bs = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL); // 定制线条样式
            Line2D line = new Line2D.Double(x, y, x + x1, y + y1);
            g2d.setStroke(bs);
            g2d.draw(line); // 绘制直线
        }

        //用来保存验证码字符串文本内容
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; ++i) {// 随机生成4个字符
            String sTemp = String.valueOf(randomChar());
            sb.append(sTemp);

            Color color = new Color(20 + random.nextInt(110), 20 + random.nextInt(110), random.nextInt(110));
            g.setColor(color);
            // 将生成的随机数进行随机缩放并旋转制定角度

            /* 将文字旋转制定角度 */
            Graphics2D g2d_word = (Graphics2D) g;
            AffineTransform trans = new AffineTransform();
            trans.rotate((45) * 3.14 / 180, 15 * i + 8, 7);

            /* 缩放文字 */
            float scaleSize = random.nextFloat() + 0.8f;
            if (scaleSize > 1f)
                scaleSize = 1f;
            trans.scale(scaleSize, scaleSize);
            g2d_word.setTransform(trans);
            g.drawString(sTemp, 15 * i + 18, 14);
        }

        session.setAttribute("randCheckCode", sb.toString());
        System.out.println("sRand="+sb.toString());
        g.dispose(); // 释放g所占用的系统资源
        ImageIO.write(image, "JPEG", response.getOutputStream()); // 输出图片
    }

    @GetMapping("/account/signoff")
    public String signoff(Model model, HttpSession session) {
        Account account = (Account)session.getAttribute("account");
        logger.info("User " + account.getUsername() + " signoff.");
        account = null;
        model.addAttribute("account",account);
        session.setAttribute("account",account);
        return "/catalog/main";
    }

    @GetMapping("/account/newAccountForm")
    public String newAccountForm(Model model) {
        Account account = new Account();
        model.addAttribute("account",account);
        return "/account/newAccountForm";
    }

    @PostMapping("/account/newAccount")
    public String newAccount(@RequestParam("repeatedPassword") String repeatedPassword, Account account, @RequestParam("checkCode") String checkCode, Model model,HttpSession session, HttpServletRequest request, HttpServletResponse response)throws Exception {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if(!account.getPassword().equals(repeatedPassword)) {
            String message = "The password entered is inconsistent Register failed.";
            model.addAttribute("message",message);
            return "/common/error";
        } else if(!checkCode.equalsIgnoreCase((String)session.getAttribute("randCheckCode"))) {
            request.setAttribute("errormsg", "验证码不正确");
            response.getWriter().println("<script> type='text/javascript'>window.alert('*验证码错误！')</script>");
            return "/account/newAccountForm";
        } else {
            accountService.insertAccount(account);
            logger.info("New user: " + account.getUsername());
            account = null;
            model.addAttribute("account",account);
            session.setAttribute("account",account);
            return "/catalog/main";
        }
    }

    @GetMapping("/account/usernameValidation")
    public void usernameValidation(@RequestParam("username") String username, HttpServletResponse response, HttpServletRequest request) throws IOException{
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();

        if(accountService.getAccount(username) != null) {
            out.println("<msg>Exist</msg>");
        }else {
            out.println("<msg>NotExist</msg>");
        }
        out.flush();
        out.close();
    }

    @GetMapping("/account/editAccountForm")
    public String editAccountForm(HttpSession session) {
        Account account = (Account)session.getAttribute("account");
        logger.info("User " + account.getUsername() + " edits personal information.");
        return "/account/editAccountForm";
    }

    @PostMapping("/account/editAccount")
    public String editAccount(@RequestParam("repeatedPassword") String repeatedPassword,Account account, Model model, HttpSession session) {
        Account account_now = (Account)session.getAttribute("account");
        String username = account_now.getUsername();

        if(!account.getPassword().equals(repeatedPassword)) {
            String message = "The password entered is inconsistent Register failed.";
            model.addAttribute("message",message);
            return "/common/error";
        }else {
            account.setUsername(username);
            accountService.updateAccount(account);
            model.addAttribute("account",account);
            session.setAttribute("account",account);
            logger.info("User " + account.getUsername() + " edited personal information successfully.");
            return "/catalog/main";

        }
    }
}
