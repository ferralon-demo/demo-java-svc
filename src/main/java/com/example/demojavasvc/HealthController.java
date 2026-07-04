package com.example.demojavasvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

// Liveness probe. No request data is bound here; it exists so the container has a trivially
// reachable healthy route for orchestration and for a proof sandbox to confirm the service
// is up before driving the /greeting surface.
@Controller
public class HealthController {

    @GetMapping("/healthz")
    @ResponseBody
    public String healthz() {
        return "ok\n";
    }
}
