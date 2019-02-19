package org.csu.mypetstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @GetMapping("/")
    public String viewIndex() {
        logger.info("Enter the web project.");
        return "index";
    }

    @GetMapping("/help")
    public String viewHelp() {
        return "help";
    }
}
