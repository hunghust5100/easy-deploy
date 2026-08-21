package com.easydeploy.web.repository;

import com.easydeploy.web.entity.DeploymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeploymentRepository extends JpaRepository<DeploymentEntity, UUID> {
    List<DeploymentEntity> findByProjectIdOrderByStartedAtDesc(UUID projectId);
    List<DeploymentEntity> findByUserIdOrderByStartedAtDesc(UUID userId);
}
