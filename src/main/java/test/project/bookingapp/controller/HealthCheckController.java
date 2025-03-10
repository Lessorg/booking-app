package test.project.bookingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health Check", description = "API for checking service health status")
public class HealthCheckController {

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Returns service health status")
    public Map<String, String> healthCheck() {
        return Map.of("status", "UP");
    }
}
