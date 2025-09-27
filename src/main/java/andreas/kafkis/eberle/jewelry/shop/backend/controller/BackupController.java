package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.BackupService;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @Value("${backup.directory:backups}")
    private String backupDirectory;

    @Value("${backup.schedule.enabled:true}")
    private boolean scheduleEnabled;

    @Value("${backup.schedule.interval.minutes:30}")
    private int intervalMinutes;

    @Value("${backup.retention.count:10}")
    private int retentionCount;

    /**
     * Trigger manual backup
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBackup() {
        try {
            String backupFilePath = backupService.performBackup();
            
            if (backupFilePath != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Backup created successfully",
                    "backupFile", backupFilePath,
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Backup failed - check server logs",
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Backup error: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    /**
     * Get backup configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getBackupConfig() {
        return ResponseEntity.ok(Map.of(
            "backupDirectory", backupDirectory,
            "scheduleEnabled", scheduleEnabled,
            "intervalMinutes", intervalMinutes,
            "retentionCount", retentionCount,
            "nextBackupIn", scheduleEnabled ? intervalMinutes + " minutes (approx)" : "Disabled",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    /**
     * Get backup statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBackupStats() {
        try {
            BackupService.BackupStats stats = backupService.getBackupStats();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "backupCount", stats.backupCount,
                "totalSizeBytes", stats.totalSizeBytes,
                "totalSizeMB", Math.round(stats.totalSizeBytes / (1024.0 * 1024.0) * 100.0) / 100.0,
                "latestBackup", stats.latestBackup != null ? stats.latestBackup : "No backups found",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to get backup stats: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }
}
