package com.collectohub.catalog.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.application.EditorialDataQualityService;
import com.collectohub.catalog.dto.EditorialDataQualityReportResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/admin/data-quality")
@EditorialAdminRequired
public class EditorialDataQualityController {
    private final EditorialDataQualityService service;
    public EditorialDataQualityController(EditorialDataQualityService service) { this.service = service; }
    @GetMapping("/report")
    public EditorialDataQualityReportResponse report(@AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String scope, @RequestParam(defaultValue = "50") int limit) {
        return service.report(user, scope, limit);
    }
}
