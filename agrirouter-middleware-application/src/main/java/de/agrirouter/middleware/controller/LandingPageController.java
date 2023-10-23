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
        model.addAttribute("loadingMessage", getRandomLoadingMessage());
        return Routes.UserInterface.ThymeleafRouting.LANDINGPAGE;
    }

    private String getRandomLoadingMessage() {
        var messages = new String[]{
                "Just a moment, we're revving up the engines for you.",
                "Gathering stardust, almost there!",
                "Hold tight, we're sculpting your experience.",
                "Sipping digital coffee, almost brewed!",
                "Tickling the servers, stay tuned!",
                "In a parallel universe, your page is already loaded.",
                "Summoning the digital spirits, stay patient.",
                "Racing through cyberspace, just for you!",
                "Building your digital paradise, brick by pixelated brick.",
                "Just unfurling the digital red carpet for you.",
                "Brace for impact! We're launching your experience.",
                "Patience is a virtue. Your digital journey is about to begin.",
                "Embrace the calm before the digital storm.",
                "Brewing awesomeness... Just a moment.",
                "Gathering stardust for your magical experience.",
                "Loading the pixels of your dreams.",
                "Revving up the engines of imagination.",
                "Hold tight! Unleashing the magic in progress.",
                "Whispering sweet nothings to our servers.",
                "Embracing the infinite possibilities of cyberspace.",
                "In the meantime, take a deep breath and smile.",
                "Fueling up the digital rocket for your mission.",
                "Loading... because even digital worlds need time to warm up.",
                "The wheels of technology are in motion.",
                "Unveiling the future, one pixel at a time.",
                "Get ready to dive into a world of wonder.",
                "Buckle up! Our servers are reaching warp speed.",
                "Preparing a symphony of code and creativity.",
                "One small step for you, one giant leap for your experience.",
                "The countdown to digital brilliance has begun."
        };
        return messages[(int) (Math.random() * messages.length)];
    }

}
