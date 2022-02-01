package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return Routes.HOME;
    }

}
