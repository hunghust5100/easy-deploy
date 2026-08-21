package com.easydeploy.web.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployments")
public class DeploymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private ServerEntity server;

    @Column(nullable = false, length = 30)
    private String triggerSource = "WEB_UI"; // WEB_UI, CLI, GITHUB_ACTION

    @Column(length = 50)
    private String commitHash;

    @Column(nullable = false, length = 30)
    private String status = "QUEUED"; // QUEUED, RUNNING, SUCCESS, FAILED, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String logContent = "";

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime finishedAt;

    private Integer durationSeconds;

    public DeploymentEntity() {}

    public DeploymentEntity(ProjectEntity project, UserEntity user, ServerEntity server, String triggerSource) {
        this.project = project;
        this.user = user;
        this.server = server;
        this.triggerSource = triggerSource;
        this.startedAt = LocalDateTime.now();
        this.status = "RUNNING";
    }

    public void appendLog(String logLine) {
        if (this.logContent == null) {
            this.logContent = logLine;
        } else {
            this.logContent += logLine;
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ProjectEntity getProject() { return project; }
    public void setProject(ProjectEntity project) { this.project = project; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public ServerEntity getServer() { return server; }
    public void setServer(ServerEntity server) { this.server = server; }

    public String getTriggerSource() { return triggerSource; }
    public void setTriggerSource(String triggerSource) { this.triggerSource = triggerSource; }

    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLogContent() { return logContent; }
    public void setLogContent(String logContent) { this.logContent = logContent; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
}
