package com.easydeploy.web.repository;

import com.easydeploy.web.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    List<ProjectEntity> findByUserId(UUID userId);
    List<ProjectEntity> findByServerId(UUID serverId);
}
