package com.example.elicesecondproject.mall;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleController {
    @GetMapping("/")
    public String home() {
        return "index"; // templates 폴더의 index.html을 찾아감
    }
}
