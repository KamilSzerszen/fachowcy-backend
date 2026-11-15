package pl.fachowcy.jobs.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.fachowcy.jobs.domain.Job;
import pl.fachowcy.jobs.domain.JobStatus;

/** Runs the real Flyway migration against PostgreSQL; Hibernate validates the schema. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class JobRepositoryIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private JobRepository repository;

  @Test
  void savesJobWithDefaultsAndVersion() {
    Job saved = repository.saveAndFlush(new Job("Bathroom renovation"));

    Job loaded = repository.findById(saved.getId()).orElseThrow();
    assertThat(loaded.getTitle()).isEqualTo("Bathroom renovation");
    assertThat(loaded.getStatus()).isEqualTo(JobStatus.NEW);
    assertThat(loaded.getCreatedAt()).isNotNull();
    assertThat(loaded.getUpdatedAt()).isNotNull();
    assertThat(loaded.getVersion()).isZero();
  }
}
