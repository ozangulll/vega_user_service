package com.vega.userservice.config;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedUsersTest {

    @Test
    void seedUsernamesAreUnique() {
        List<String> names = SeedUsers.all().stream().map(SeedUsers.Spec::username).toList();
        assertEquals(names.size(), new HashSet<>(names).size(), "Duplicate seed username");
    }

    @Test
    void primaryDevAccountIsIncluded() {
        assertTrue(
                SeedUsers.all().stream().anyMatch(s ->
                        SeedUsers.PRIMARY_DEV_USERNAME.equals(s.username())
                                && SeedUsers.PRIMARY_DEV_PASSWORD.equals(s.plainPassword())),
                "PRIMARY_DEV_* must match an entry in SeedUsers.all()"
        );
    }
}
