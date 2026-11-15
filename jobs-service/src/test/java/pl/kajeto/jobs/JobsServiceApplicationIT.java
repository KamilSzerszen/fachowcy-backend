package pl.kajeto.jobs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Starts the whole application (Flyway + JPA + Security) against PostgreSQL. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class JobsServiceApplicationIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @MockitoBean private JwtDecoder jwtDecoder;

  @Autowired private MockMvc mockMvc;

  @Test
  void healthEndpointsArePublic() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    mockMvc
        .perform(get("/actuator/health/readiness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
    mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
  }

  @Test
  void openApiDocsArePublic() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/v1/jobs/ping']").exists());
  }

  @Test
  void pingRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/jobs/ping")).andExpect(status().isUnauthorized());
  }
}
