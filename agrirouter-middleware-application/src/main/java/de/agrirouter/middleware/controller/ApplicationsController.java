package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.domain.Application;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

/**
 * The applications controller.
 */
@Controller
@RequestMapping("applications")
public class ApplicationsController {

    private final ApplicationService applicationService;

    public ApplicationsController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * The home / landing page.
     *
     * @return -
     */
    @GetMapping("/")
    public String navigation(Principal principal, Model model) {
        List<Application> applications = applicationService.findAll(principal);
        model.addAttribute("applications", applications);
        return Routes.APPLICATIONS;
    }

}
