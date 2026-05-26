package de.agrirouter.middleware.controller.aop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller advice to add branding information to the model.
 */
@ControllerAdvice
public class BrandingUIControllerAdvice {

    @Value("${app.branding.favicon}")
    private String favicon;

    @Value("${app.branding.customCss:#{null}}")
    private String customCss;

    /**
     * Add the favicon to the model.
     *
     * @return The favicon.
     */
    @ModelAttribute("favicon")
    public String favicon() {
        return favicon;
    }

    /**
     * Add the custom CSS to the model.
     *
     * @return The custom CSS.
     */
    @ModelAttribute("customCss")
    public String customCss() {
        return customCss;
    }

}
