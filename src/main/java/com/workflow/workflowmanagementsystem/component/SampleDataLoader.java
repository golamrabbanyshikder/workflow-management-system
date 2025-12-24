package com.workflow.workflowmanagementsystem.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Component to load sample data into the database when the application starts.
 * This is useful for development and testing environments.
 */
@Component
public class SampleDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataLoader.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.sample-data.enabled:false}")
    private boolean sampleDataEnabled;

    @Value("${app.sample-data.force-reload:false}")
    private boolean forceReload;

    @Override
    public void run(String... args) throws Exception {
        if (!sampleDataEnabled) {
            logger.info("Sample data loading is disabled. Set app.sample-data.enabled=true to enable.");
            return;
        }

        try {
            if (forceReload || !hasExistingData()) {
                logger.info("Loading sample data...");
                loadSampleData();
                logger.info("Sample data loaded successfully!");
            } else {
                logger.info("Database already contains data. Skipping sample data loading. " +
                        "Set app.sample-data.force-reload=true to force reload.");
            }
        } catch (Exception e) {
            logger.error("Failed to load sample data: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Check if the database already contains data
     */
    private boolean hasExistingData() {
        try {
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            return userCount != null && userCount > 0;
        } catch (Exception e) {
            // Table might not exist yet
            return false;
        }
    }

    /**
     * Load sample data from the SQL script
     */
    private void loadSampleData() throws IOException, SQLException {
        // Get the SQL script from classpath
        ClassPathResource resource = new ClassPathResource("templates/sql/sample-data.sql");
        
        if (!resource.exists()) {
            throw new RuntimeException("Sample data SQL file not found: templates/sql/sample-data.sql");
        }

        // Read the SQL script
        String sqlScript = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        
        // Split the script into individual statements
        String[] sqlStatements = sqlScript.split(";(?=([^']*'[^']*')*[^']*$)");
        
        // Execute each statement
        for (String statement : sqlStatements) {
            statement = statement.trim();
            if (!statement.isEmpty() && !statement.startsWith("--")) {
                try {
                    jdbcTemplate.execute(statement);
                } catch (Exception e) {
                    // Log the error but continue with other statements
                    logger.warn("Failed to execute SQL statement: " + statement.substring(0, Math.min(100, statement.length())) + "...", e);
                }
            }
        }
    }
}