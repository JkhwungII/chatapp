package org.jake.messager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebController {
    @GetMapping("/")
    public String getWeb() {
        return "test";
    }
}