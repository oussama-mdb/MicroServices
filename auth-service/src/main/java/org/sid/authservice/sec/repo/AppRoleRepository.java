package org.sid.authservice.sec.repo;

import org.sid.authservice.sec.entities.AppRole;
import org.sid.authservice.sec.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    AppRole findByRoleName(String rolename);
}
