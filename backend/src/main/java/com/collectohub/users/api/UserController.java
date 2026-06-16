package com.collectohub.users.api;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.users.dto.UserMeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public UserMeResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return UserMeResponse.from(user);
    }
}
