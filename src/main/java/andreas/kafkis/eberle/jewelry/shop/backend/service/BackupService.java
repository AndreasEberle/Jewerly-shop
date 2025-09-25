package andreas.kafkis.eberle.jewelry.shop.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Service
public class BackupService {

    private static final Logger logger = Logger.getLogger(BackupService.class.getName());

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${backup.directory:backups}")
    private String backupDirectory;

    @Value("${backup.retention.count:10}")
    private int retentionCount;

    /**
     * Backup database on configured schedule
     * Interval is controlled by application.properties: backup.schedule.interval.minutes
     */
    @Scheduled(fixedRateString = "#{${backup.schedule.interval.minutes:30} * 60 * 1000}")
    @Async
    @ConditionalOnProperty(name = "backup.schedule.enabled", havingValue = "true", matchIfMissing = true)
    public void performScheduledBackup() {
        logger.info("Starting scheduled database backup...");
        performBackup();
    }

    /**
     * Backup database manually
     */
    public String performBackup() {
        try {
            // Create backup directory if it doesn't exist
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            // Generate backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = String.format("jewelry_shop_backup_%s.sql", timestamp);
            Path backupFilePath = backupPath.resolve(backupFileName);

            // Extract database info from URL
            String dbHost = extractHostFromUrl(databaseUrl);
            String dbPort = extractPortFromUrl(databaseUrl);
            String dbName = extractDatabaseNameFromUrl(databaseUrl);

            // Build pg_dump command
            ProcessBuilder processBuilder = new ProcessBuilder(
                "pg_dump",
                "--host=" + dbHost,
                "--port=" + dbPort,
                "--username=" + databaseUsername,
                "--format=plain",
                "--verbose",
                "--file=" + backupFilePath.toString(),
                dbName
            );

            // Set password as environment variable
            processBuilder.environment().put("PGPASSWORD", databasePassword);

            // Execute backup
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Database backup completed successfully: " + backupFilePath);
                cleanupOldBackups();
                return backupFilePath.toString();
            } else {
                logger.severe("Database backup failed with exit code: " + exitCode);
                return null;
            }

        } catch (Exception e) {
            logger.severe("Database backup failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Keep only the configured number of backups (cleanup old ones)
     */
    private void cleanupOldBackups() {
        try {
            Path backupPath = Paths.get(backupDirectory);
            Files.list(backupPath)
                .filter(path -> path.getFileName().toString().startsWith("jewelry_shop_backup_"))
                .sorted((p1, p2) -> Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()))
                .skip(retentionCount) // Keep the configured number of backups
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.info("Deleted old backup: " + path.getFileName());
                    } catch (IOException e) {
                        logger.warning("Failed to delete old backup: " + path.getFileName());
                    }
                });
        } catch (Exception e) {
            logger.warning("Failed to cleanup old backups: " + e.getMessage());
        }
    }

    /**
     * Extract host from JDBC URL
     */
    private String extractHostFromUrl(String url) {
        // jdbc:postgresql://localhost:5433/jewelryshopdb
        String[] parts = url.split("://")[1].split("/")[0].split(":");
        return parts[0];
    }

    /**
     * Extract port from JDBC URL
     */
    private String extractPortFromUrl(String url) {
        // jdbc:postgresql://localhost:5433/jewelryshopdb
        String[] parts = url.split("://")[1].split("/")[0].split(":");
        return parts.length > 1 ? parts[1] : "5432";
    }

    /**
     * Extract database name from JDBC URL
     */
    private String extractDatabaseNameFromUrl(String url) {
        // jdbc:postgresql://localhost:5433/jewelryshopdb
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    /**
     * Get backup statistics
     */
    public BackupStats getBackupStats() {
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                return new BackupStats(0, 0, null);
            }

            long backupCount = Files.list(backupPath)
                .filter(path -> path.getFileName().toString().startsWith("jewelry_shop_backup_"))
                .count();

            long totalSize = Files.list(backupPath)
                .filter(path -> path.getFileName().toString().startsWith("jewelry_shop_backup_"))
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();

            // Get the latest backup timestamp
            String latestBackup = Files.list(backupPath)
                .filter(path -> path.getFileName().toString().startsWith("jewelry_shop_backup_"))
                .map(path -> path.getFileName().toString())
                .sorted((a, b) -> b.compareTo(a))
                .findFirst()
                .orElse(null);

            return new BackupStats(backupCount, totalSize, latestBackup);
        } catch (Exception e) {
            logger.warning("Failed to get backup stats: " + e.getMessage());
            return new BackupStats(0, 0, null);
        }
    }

    /**
     * Backup statistics class
     */
    public static class BackupStats {
        public final long backupCount;
        public final long totalSizeBytes;
        public final String latestBackup;

        public BackupStats(long backupCount, long totalSizeBytes, String latestBackup) {
            this.backupCount = backupCount;
            this.totalSizeBytes = totalSizeBytes;
            this.latestBackup = latestBackup;
        }
    }
}
