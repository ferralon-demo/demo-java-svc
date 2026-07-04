package com.example.demojavasvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// GreetingController exposes the data-binding surface that makes CVE-2022-22965
// (Spring4Shell) reachable in this service.
//
// The reachability condition this fixture embodies, exactly:
//   - spring-beans / spring-webmvc are pinned < 5.3.18 (see pom.xml);
//   - the WAR is deployed on a standalone Tomcat 9 running under JDK 9+ (see Dockerfile);
//   - the /greeting handler binds untrusted request parameters directly onto a
//     non-primitive command object (Greeting) via @ModelAttribute; and
//   - there is deliberately NO @InitBinder restricting the bindable field set (no
//     setDisallowedFields("class.*"), no allow-list), so the WebDataBinder will walk
//     nested bean properties from the request parameters.
//
// That combination is precisely the affected configuration in the advisory: an attacker can
// drive the binder down class.module.classLoader.* to reconfigure the container. This
// controller does not itself perform the exploit — it establishes the genuinely reachable
// path. Whether that path is exploited is proven under execution (see FIRING.md), never
// asserted here.
@Controller
public class GreetingController {

    @GetMapping("/")
    @ResponseBody
    public String index() {
        return "demo-java-svc — POST /greeting with form fields name, message\n";
    }

    @PostMapping("/greeting")
    @ResponseBody
    public String greet(@ModelAttribute Greeting greeting) {
        return "hello " + greeting.getName() + ": " + greeting.getMessage() + "\n";
    }
}
