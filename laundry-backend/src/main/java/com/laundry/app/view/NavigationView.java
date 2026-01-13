package com.laundry.app.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * Navigation view used to perform role-based redirects after login.
 */
@Named
@RequestScoped
public class NavigationView {

    /**
     * Redirects the current user to the appropriate landing page based on role.
     *
     * @throws IOException if redirect fails
     */
    public void redirectBasedOnRole() throws IOException {
        boolean isManager = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        FacesContext context = FacesContext.getCurrentInstance();

        if (isManager) {
            context.getExternalContext().redirect("manager-dashboard.xhtml");
        } else {
            // --- UPDATED: Redirect to the new Calendar Home ---
            context.getExternalContext().redirect("home.xhtml");
        }
    }
}