package pl.kajeto.jobs.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.kajeto.jobs.config.SecurityConfig;

@WebMvcTest(JobsController.class)
@Import(SecurityConfig.class)
class JobsControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void withoutTokenReturns401() throws Exception {
    mockMvc.perform(get("/api/v1/jobs/ping")).andExpect(status().isUnauthorized());
  }

  @Test
  void withTokenWithoutOwnerRoleReturns403() throws Exception {
    mockMvc
        .perform(get("/api/v1/jobs/ping").with(jwt().authorities(() -> "ROLE_WORKER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void withOwnerRoleReturns200() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/jobs/ping")
                .with(jwt().jwt(j -> j.subject("user-1")).authorities(() -> "ROLE_OWNER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.subject").value("user-1"));
  }
}
