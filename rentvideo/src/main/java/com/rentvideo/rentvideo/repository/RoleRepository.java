package com.rentvideo.rentvideo.repository;

import com.rentvideo.rentvideo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(Role.RoleName name); // <--- Make sure this is correct
}