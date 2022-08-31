package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.domain.Endpoint;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

/**
 * The endpoints controller.
 */
@Controller
public class EndpointsUIController {

    private final EndpointService endpointService;

    public EndpointsUIController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    /**
     * The home / landing page.
     *
     * @return -
     */
    @SuppressWarnings("unused")
    @GetMapping("/endpoints")
    public String navigation(Principal principal, @RequestParam(value = "internalApplicationId") String internalApplicationId, Model model) {
        List<Endpoint> endpoints = endpointService.findAll(internalApplicationId);
        model.addAttribute("endpoints", endpoints);
        return Routes.UI.ENDPOINTS;
    }

}
