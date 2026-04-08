package com.nexora.core.graphql;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class NexoraQueryController {

    @QueryMapping
    public String health() {
        return "Nexora GraphQL API is running";
    }
}
