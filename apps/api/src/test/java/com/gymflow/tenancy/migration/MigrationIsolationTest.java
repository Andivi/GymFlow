package com.gymflow.tenancy.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MigrationIsolationTest {

    @Autowired
    DataSource controlPlaneDataSource;

    @Autowired
    TenantMigrations tenantMigrations;

    @Value("${spring.datasource.url}")
    String controlUrl;

    @Value("${spring.datasource.username}")
    String username;

    @Value("${spring.datasource.password}")
    String password;

    @Value("${gymflow.test.tenant-db-name}")
    String tenantDbName;

    DataSource tenantDataSource;

    @BeforeEach
    void createTenantDatabase() {
        JdbcTemplate control = new JdbcTemplate(controlPlaneDataSource);
        control.execute("DROP DATABASE IF EXISTS " + tenantDbName + " WITH (FORCE)");
        control.execute("CREATE DATABASE " + tenantDbName);
        tenantDataSource = new DriverManagerDataSource(withDatabase(controlUrl, tenantDbName), username, password);
    }

    @AfterEach
    void dropTenantDatabase() {
        new JdbcTemplate(controlPlaneDataSource).execute("DROP DATABASE IF EXISTS " + tenantDbName + " WITH (FORCE)");
    }

    @Test
    void controlPlaneMigrationsRunOnStartup() {
        assertThat(tableExists(controlPlaneDataSource, "gyms")).isTrue();
        assertThat(tableExists(controlPlaneDataSource, "tenant_database_registry")).isTrue();
        assertThat(tableExists(controlPlaneDataSource, ControlPlaneMigrations.HISTORY_TABLE)).isTrue();
        assertThat(tableExists(controlPlaneDataSource, "members")).isFalse();
    }

    @Test
    void tenantMigrationsOnlyTouchTheTenantDatabase() {
        tenantMigrations.migrate(tenantDataSource);

        assertThat(tableExists(tenantDataSource, "members")).isTrue();
        assertThat(tableExists(tenantDataSource, "membership_plans")).isTrue();
        assertThat(tableExists(tenantDataSource, TenantMigrations.HISTORY_TABLE)).isTrue();
        assertThat(tableExists(tenantDataSource, "gyms")).isFalse();
        assertThat(tableExists(tenantDataSource, ControlPlaneMigrations.HISTORY_TABLE)).isFalse();
        assertThat(tableExists(controlPlaneDataSource, "members")).isFalse();
    }

    @Test
    void refusesTheControlPlaneDataSource() {
        assertThatThrownBy(() -> tenantMigrations.migrate(controlPlaneDataSource))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(tableExists(controlPlaneDataSource, "members")).isFalse();
    }

    @Test
    void refusesAnotherDataSourcePointingAtTheControlPlaneDatabase() {
        DataSource sameDatabase = new DriverManagerDataSource(controlUrl, username, password);

        assertThatThrownBy(() -> tenantMigrations.migrate(sameDatabase))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(tableExists(controlPlaneDataSource, "members")).isFalse();
    }

    private static boolean tableExists(DataSource dataSource, String table) {
        return Boolean.TRUE.equals(new JdbcTemplate(dataSource)
            .queryForObject("SELECT to_regclass(?) IS NOT NULL", Boolean.class, table));
    }

    private static String withDatabase(String jdbcUrl, String database) {
        return jdbcUrl.replaceFirst("/[^/?]+(\\?|$)", "/" + database + "$1");
    }
}
