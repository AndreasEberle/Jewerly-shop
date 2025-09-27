package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BackupService {

    private static final Logger logger = Logger.getLogger(BackupService.class.getName());

    @Autowired
    private DataSource dataSource;

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

    @Value("${backup.pg-dump.path:}")
    private String pgDumpPath;

    @Value("${backup.method:auto}")
    private String backupMethod;

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

            String result = null;
            
            // Try different backup methods based on configuration
            switch (backupMethod.toLowerCase()) {
                case "pg_dump":
                    result = performPgDumpBackup(backupFilePath);
                    break;
                case "jdbc":
                    result = performJdbcBackup(backupFilePath);
                    break;
                case "auto":
                default:
                    // Try pg_dump first, fall back to JDBC if it fails
                    result = performPgDumpBackup(backupFilePath);
                    if (result == null) {
                        logger.info("pg_dump failed, falling back to JDBC backup...");
                        result = performJdbcBackup(backupFilePath);
                    }
                    break;
            }

            if (result != null) {
                cleanupOldBackups();
            }
            
            return result;

        } catch (Exception e) {
            logger.severe("Database backup failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Perform backup using pg_dump
     */
    private String performPgDumpBackup(Path backupFilePath) {
        try {
            // Find pg_dump executable
            String pgDumpCommand = findPgDumpExecutable();
            if (pgDumpCommand == null) {
                logger.warning("pg_dump executable not found");
                return null;
            }

            // Extract database info from URL
            String dbHost = extractHostFromUrl(databaseUrl);
            String dbPort = extractPortFromUrl(databaseUrl);
            String dbName = extractDatabaseNameFromUrl(databaseUrl);

            // Build pg_dump command
            ProcessBuilder processBuilder = new ProcessBuilder(
                pgDumpCommand,
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
            logger.info("Starting pg_dump backup with command: " + String.join(" ", processBuilder.command()));
            Process process = processBuilder.start();
            
            // Capture error output for debugging
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                StringBuilder errorOutput = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                if (errorOutput.length() > 0) {
                    logger.info("pg_dump output: " + errorOutput.toString());
                }
            }
            
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("pg_dump backup completed successfully: " + backupFilePath);
                return backupFilePath.toString();
            } else {
                logger.warning("pg_dump backup failed with exit code: " + exitCode);
                return null;
            }

        } catch (Exception e) {
            logger.warning("pg_dump backup failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Perform backup using JDBC (fallback method)
     */
    private String performJdbcBackup(Path backupFilePath) {
        try {
            logger.info("Starting JDBC backup to: " + backupFilePath);
            
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 FileWriter writer = new FileWriter(backupFilePath.toFile())) {

                // Write header
                writer.write("-- Jewelry Shop Database Backup\n");
                writer.write("-- Generated: " + LocalDateTime.now() + "\n");
                writer.write("-- Method: JDBC\n\n");

                // Get all table names
                List<String> tableNames = getTableNames(connection);
                
                for (String tableName : tableNames) {
                    writer.write("-- Table: " + tableName + "\n");
                    
                    // Get table structure
                    try (ResultSet rs = statement.executeQuery("SELECT column_name, data_type, is_nullable, column_default " +
                            "FROM information_schema.columns WHERE table_name = '" + tableName + "' ORDER BY ordinal_position")) {
                        
                        writer.write("-- Columns for " + tableName + ":\n");
                        while (rs.next()) {
                            writer.write("-- " + rs.getString("column_name") + " " + 
                                        rs.getString("data_type") + 
                                        (rs.getString("is_nullable").equals("NO") ? " NOT NULL" : "") + 
                                        (rs.getString("column_default") != null ? " DEFAULT " + rs.getString("column_default") : "") + "\n");
                        }
                    }
                    
                    // Export data
                    try (ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName)) {
                        int columnCount = rs.getMetaData().getColumnCount();
                        
                        while (rs.next()) {
                            StringBuilder insertStatement = new StringBuilder();
                            insertStatement.append("INSERT INTO ").append(tableName).append(" VALUES (");
                            
                            for (int i = 1; i <= columnCount; i++) {
                                if (i > 1) insertStatement.append(", ");
                                
                                Object value = rs.getObject(i);
                                if (value == null) {
                                    insertStatement.append("NULL");
                                } else if (value instanceof String) {
                                    insertStatement.append("'").append(value.toString().replace("'", "''")).append("'");
                                } else {
                                    insertStatement.append(value.toString());
                                }
                            }
                            
                            insertStatement.append(");\n");
                            writer.write(insertStatement.toString());
                        }
                    }
                    
                    writer.write("\n");
                }
                
                writer.flush();
                logger.info("JDBC backup completed successfully: " + backupFilePath);
                return backupFilePath.toString();
            }

        } catch (Exception e) {
            logger.severe("JDBC backup failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all table names from the database
     */
    private List<String> getTableNames(Connection connection) throws Exception {
        List<String> tableNames = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT table_name FROM information_schema.tables " +
                     "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name")) {
            
            while (rs.next()) {
                tableNames.add(rs.getString("table_name"));
            }
        }
        return tableNames;
    }

    /**
     * Find pg_dump executable on Windows
     */
    private String findPgDumpExecutable() {
        // If custom path is configured, use it
        if (pgDumpPath != null && !pgDumpPath.trim().isEmpty()) {
            File customPgDump = new File(pgDumpPath);
            if (customPgDump.exists() && customPgDump.canExecute()) {
                logger.info("Using custom pg_dump path: " + pgDumpPath);
                return pgDumpPath;
            }
        }

        // Try to find pg_dump in PATH first
        try {
            Process process = new ProcessBuilder("where", "pg_dump").start();
            process.waitFor();
            if (process.exitValue() == 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String path = reader.readLine();
                    if (path != null && !path.trim().isEmpty()) {
                        logger.info("Found pg_dump in PATH: " + path);
                        return "pg_dump";
                    }
                }
            }
        } catch (Exception e) {
            logger.fine("pg_dump not found in PATH: " + e.getMessage());
        }

        // Common PostgreSQL installation paths on Windows
        String[] commonPaths = {
            "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe",
            "C:\\Program Files\\PostgreSQL\\15\\bin\\pg_dump.exe",
            "C:\\Program Files\\PostgreSQL\\14\\bin\\pg_dump.exe",
            "C:\\Program Files\\PostgreSQL\\13\\bin\\pg_dump.exe",
            "C:\\Program Files\\PostgreSQL\\12\\bin\\pg_dump.exe",
            "C:\\Program Files (x86)\\PostgreSQL\\16\\bin\\pg_dump.exe",
            "C:\\Program Files (x86)\\PostgreSQL\\15\\bin\\pg_dump.exe",
            "C:\\Program Files (x86)\\PostgreSQL\\14\\bin\\pg_dump.exe",
            "C:\\Program Files (x86)\\PostgreSQL\\13\\bin\\pg_dump.exe",
            "C:\\Program Files (x86)\\PostgreSQL\\12\\bin\\pg_dump.exe"
        };

        for (String path : commonPaths) {
            File pgDumpFile = new File(path);
            if (pgDumpFile.exists() && pgDumpFile.canExecute()) {
                logger.info("Found pg_dump at: " + path);
                return path;
            }
        }

        logger.warning("pg_dump executable not found in common locations");
        return null;
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
