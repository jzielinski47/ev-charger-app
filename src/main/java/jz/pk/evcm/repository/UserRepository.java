package jz.pk.evcm.repository;

import jz.pk.evcm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
