package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The custom home controller.
 */
@Controller
public class HomeUIController {

    /**
     * The home / landing page.
     *
     * @return -
     */
    @GetMapping("/")
    public String navigation() {
        return Routes.UI.HOME;
    }

}
