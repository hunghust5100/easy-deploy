package com.easydeploy.web.repository;

import com.easydeploy.web.entity.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServerRepository extends JpaRepository<ServerEntity, UUID> {
    List<ServerEntity> findByUserId(UUID userId);
}
