package com.example.demojavasvc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

// Standard Spring MVC configuration. @EnableWebMvc registers the default message
// converters, including the Jackson converter when jackson-databind is on the classpath —
// so JSON binding on /echo goes through jackson with its stock, non-polymorphic settings.
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.example.demojavasvc")
public class WebConfig {
}
