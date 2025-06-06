package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.domain.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

/**
 * The applications' controller.
 */
@Controller
@RequiredArgsConstructor
public class ApplicationsUIController extends UIController {

    private final ApplicationService applicationService;

    /**
     * The landing page.
     *
     * @return -
     */
    @GetMapping("applications")
    public String navigation(Principal principal, Model model) {
        List<Application> applications = applicationService.findAll(principal);
        model.addAttribute("applications", applications);
        model.addAttribute("activeProfiles", getActiveProfiles());
        return Routes.UserInterface.ThymeleafRouting.APPLICATIONS;
    }

}
