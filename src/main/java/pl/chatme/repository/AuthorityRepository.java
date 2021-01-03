package pl.chatme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chatme.domain.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
