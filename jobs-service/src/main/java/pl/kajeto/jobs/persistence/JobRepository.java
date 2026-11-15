package pl.fachowcy.jobs.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.fachowcy.jobs.domain.Job;

public interface JobRepository extends JpaRepository<Job, UUID> {}
