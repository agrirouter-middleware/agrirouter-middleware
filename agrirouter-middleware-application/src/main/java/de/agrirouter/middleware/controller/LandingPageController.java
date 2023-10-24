package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The custom home controller.
 */
@Controller
public class LandingPageController extends UIController {

    /**
     * The home / landing page.
     *
     * @return -
     */
    @GetMapping("/landing_page")
    public String navigation(@RequestParam(value = "state") String state,
                             @RequestParam(value = "token", required = false) String token,
                             @RequestParam(value = "signature", required = false) String signature,
                             @RequestParam(value = "error", required = false) String error,
                             Model model) {
        model.addAttribute("state", state);
        model.addAttribute("token", token);
        model.addAttribute("signature", signature);
        model.addAttribute("error", error);
        model.addAttribute("redirectUrl", Routes.UnsecuredEndpoints.CALLBACK_PROCESSOR);
        return Routes.UserInterface.ThymeleafRouting.LANDINGPAGE;
    }

}
