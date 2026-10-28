package com.zosh.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping
    public String home(){
        return "Hii i am the greatest man in the world";
    }

  @GetMapping("/api")
    public String saftey(){
        return "Hii I am a gangster";
  }
}
