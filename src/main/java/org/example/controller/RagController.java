package org.example.controller;

import org.example.service.rag.RagAgent;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "http://localhost:1420")
public class RagController {
    private final RagAgent ragAgent;

    public RagController(RagAgent ragAgent) {
        this.ragAgent = ragAgent;
    }

    @PostMapping
    public String getResponse(@RequestBody String request) {
        return ragAgent.getPrompt(request);
    }
}
