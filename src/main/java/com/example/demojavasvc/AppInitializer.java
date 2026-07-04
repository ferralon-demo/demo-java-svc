// Programmatic web application bootstrap (Servlet 3.0+), so this WAR carries no
// WEB-INF/web.xml. Registering the Spring DispatcherServlet this way keeps the fixture a
// perfectly ordinary Spring MVC service: nothing here references a scanner or its brand.
package com.example.demojavasvc;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
