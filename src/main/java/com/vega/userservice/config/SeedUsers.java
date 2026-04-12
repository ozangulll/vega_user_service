package com.vega.userservice.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Canonical list of dev/demo users created on user-service startup ({@link DataInitializer}).
 * <p>
 * <b>When you add, remove, or rename a seed user, update every place below</b> so docs, scripts,
 * and UI hints stay accurate (password policy here: plain password equals username for dev seeds).
 * <ul>
 *   <li>{@code ../start-all-services.sh} — log lines and {@code vega login …} hint</li>
 *   <li>{@code vega_user_service/test-docker.sh} — {@code SEED_USERNAMES} array</li>
 *   <li>{@code vega_user_service/init-scripts/01-init-user.sql} — comment only (no INSERT; seeds are Java-side)</li>
 *   <li>{@code vega_user_service/README-Docker.md} — example credentials</li>
 *   <li>{@code vega_user_service/docker-compose.yml} — {@code MYSQL_USER} / {@code SPRING_DATASOURCE_*}
 *       are the <em>database</em> login (not the same concept as seed app users, but often one seed
 *       username matches {@code MYSQL_USER} for convenience)</li>
 *   <li>{@code vega-repos/vega-repos-backend} — {@code VEGA_USER_SERVICE_URL}, {@code JWT_SECRET} must match
 *       this service so JWTs validate in Vega Repos</li>
 *   <li>{@code vega-repos/vega-repos-frontend} — placeholders that mention a sample username (e.g. collaborator)</li>
 *   <li>{@code VEGA/} CLI — Javadoc examples using {@code owner/repo} paths</li>
 * </ul>
 */
public final class SeedUsers {

    private SeedUsers() {}

    /**
     * Primary account recommended in scripts and local UI login; password equals username (dev only).
     */
    public static final String PRIMARY_DEV_USERNAME = "versionengineai";
    public static final String PRIMARY_DEV_PASSWORD = "versionengineai";

    public record Spec(String username, String plainPassword, String email, String firstName, String lastName) {}

    public static List<Spec> all() {
        return List.of(
                new Spec(PRIMARY_DEV_USERNAME, PRIMARY_DEV_PASSWORD,
                        PRIMARY_DEV_USERNAME + "@vega.local", "Version", "Engine AI"),
                new Spec("defaultuser", "defaultuser", "defaultuser@vega.local", "Default", "User"),
                new Spec("developer1", "developer1", "developer1@vega.local", "Developer", "One"),
                new Spec("reviewer1", "reviewer1", "reviewer1@vega.local", "Reviewer", "One")
        );
    }

    /** All seed usernames (for tests / diagnostics). */
    public static Set<String> usernames() {
        return new HashSet<>(all().stream().map(Spec::username).toList());
    }
}
