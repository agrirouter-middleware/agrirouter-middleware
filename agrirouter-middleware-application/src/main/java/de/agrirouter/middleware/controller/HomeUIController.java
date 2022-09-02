package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

/**
 * The custom home controller.
 */
@Controller
public class HomeUIController {

    private final Environment environment;

    public HomeUIController(Environment environment) {
        this.environment = environment;
    }

    /**
     * The home / landing page.
     *
     * @return -
     */
    @GetMapping("/")
    public String navigation(Model model) {
        model.addAttribute("activeProfiles", "Active profiles = " + Arrays.stream(environment.getActiveProfiles()).toList());
        return Routes.UI.HOME;
    }

}
