package de.agrirouter.middleware.controller.unsecured;

import de.agrirouter.middleware.controller.dto.response.enums.OnboardProcessResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The controller for the results of the onboard process.
 */
@Controller
public class OnboardProcessResultController implements UnsecuredApiController {

    /**
     * Route to the internal result page.
     *
     * @return Navigation to a specific page, therefore this is no container.
     */
    @GetMapping(UnsecuredApiController.API_PREFIX +"/onboard-process-result")
    @Operation(hidden = true)
    public String onboardProcessResult(@RequestParam("onboardProcessResult") OnboardProcessResult onboardProcessResult, Model model) {
        model.addAttribute("onboardProcessResult", onboardProcessResult);
        return "onboard-process-result";
    }

}
