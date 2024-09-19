package com.FA24SE088.OnlineForum.repositories;

import com.FA24SE088.OnlineForum.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
//    boolean findByName(String name);
    Role findByName(String name);
}
