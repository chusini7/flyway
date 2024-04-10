package com.efreight.flyway.resolver;

import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;

public class LocationResolver {

    private static final String VENDOR_PLACEHOLDER = "{vendor}";

    private final DataSource dataSource;

    public LocationResolver(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String[] resolveLocations(Collection<String> locations) {
        return resolveLocations(StringUtils.toStringArray(locations));
    }

    public String[] resolveLocations(String[] locations) {
        if (usesVendorLocation(locations)) {
            DatabaseDriver databaseDriver = getDatabaseDriver();
            return replaceVendorLocations(locations, databaseDriver);
        }
        return locations;
    }

    private String[] replaceVendorLocations(String[] locations,
                                            DatabaseDriver databaseDriver) {
        if (databaseDriver == DatabaseDriver.UNKNOWN) {
            return locations;
        }
        String vendor = databaseDriver.getId();
        return Arrays.stream(locations)
                .map((location) -> location.replace(VENDOR_PLACEHOLDER, vendor))
                .toArray(String[]::new);
    }

    private DatabaseDriver getDatabaseDriver() {
        try {
            String url = JdbcUtils.extractDatabaseMetaData(this.dataSource, "getURL");
            return DatabaseDriver.fromJdbcUrl(url);
        } catch (MetaDataAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private boolean usesVendorLocation(String... locations) {
        for (String location : locations) {
            if (location.contains(VENDOR_PLACEHOLDER)) {
                return true;
            }
        }
        return false;
    }
}
