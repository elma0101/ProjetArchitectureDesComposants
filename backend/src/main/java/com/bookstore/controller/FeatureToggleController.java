package com.bookstore.controller;

import com.bookstore.versioning.ApiVersion;
import com.bookstore.versioning.FeatureToggleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing feature toggles
 */
@RestController
@RequestMapping("/api/admin/features")
@ApiVersion("2.0")
public class FeatureToggleController {

    @Autowired
    private FeatureToggleService featureToggleService;

    @GetMapping
    public ResponseEntity<Map<String, Boolean>> getAllFeatures() {
        return ResponseEntity.ok(featureToggleService.getAllFeatureStates());
    }

    @PostMapping("/{featureName}/enable")
    public ResponseEntity<Map<String, Object>> enableFeature(@PathVariable String featureName) {
        featureToggleService.enableFeature(featureName);
        return ResponseEntity.ok(Map.of(
            "feature", featureName,
            "enabled", true,
            "message", "Feature enabled successfully"
        ));
    }

    @PostMapping("/{featureName}/disable")
    public ResponseEntity<Map<String, Object>> disableFeature(@PathVariable String featureName) {
        featureToggleService.disableFeature(featureName);
        return ResponseEntity.ok(Map.of(
            "feature", featureName,
            "enabled", false,
            "message", "Feature disabled successfully"
        ));
    }

    @GetMapping("/{featureName}")
    public ResponseEntity<Map<String, Object>> getFeatureStatus(@PathVariable String featureName) {
        boolean enabled = featureToggleService.isFeatureEnabled(featureName);
        return ResponseEntity.ok(Map.of(
            "feature", featureName,
            "enabled", enabled
        ));
    }
}