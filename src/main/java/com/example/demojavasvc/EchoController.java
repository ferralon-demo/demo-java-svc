package com.example.demojavasvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// EchoController is the honesty-wall beat for CVE-2019-17531 (jackson-databind polymorphic
// deserialization).
//
// jackson-databind IS present at a version the advisory matches (see pom.xml), and it IS
// genuinely reachable from this externally exposed endpoint: /echo reads an attacker-
// supplied JSON body with ObjectMapper.readValue. But the advisory's dangerous condition —
// Default Typing enabled on an externally exposed endpoint, so a "@class"/type-id field can
// select an arbitrary gadget class — is not met here, by construction:
//
//   - MAPPER is the stock ObjectMapper. activateDefaultTyping()/enableDefaultTyping() is
//     NEVER called, so polymorphic type resolution from the JSON payload stays off; and
//   - the body is bound into a concrete, non-polymorphic type (EchoMessage) with no
//     @JsonTypeInfo, so there is no polymorphic slot for a type name to drive.
//
// The dependency matches, the code is reachable, and the sink is still provably out of
// reach — the definition of a grounded not_exploitable, not a disqualification.
@RestController
public class EchoController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @PostMapping(path = "/echo",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public String echo(@RequestBody String body) throws Exception {
        EchoMessage msg = MAPPER.readValue(body, EchoMessage.class);
        return MAPPER.writeValueAsString(msg);
    }
}
