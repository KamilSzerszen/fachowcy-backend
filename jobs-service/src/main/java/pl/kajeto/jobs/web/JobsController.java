package pl.kajeto.jobs.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobsController {

  @GetMapping("/ping")
  @PreAuthorize("hasRole('OWNER')")
  @SecurityRequirement(name = "bearer-jwt")
  public PingResponse ping(@AuthenticationPrincipal Jwt jwt) {
    return new PingResponse("ok", jwt.getSubject());
  }
}
