package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The custom home controller.
 */
@Controller
public class ErrorUIController extends UIController {

    /**
     * The error page.
     *
     * @return -
     */
    @GetMapping("/error")
    public String navigation(Model model) {
        model.addAttribute("activeProfiles", getActiveProfiles());
        return Routes.UnsecuredEndpoints.ERROR;
    }

}
