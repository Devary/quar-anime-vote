package com.votescroll;

import io.quarkus.test.junit.QuarkusTestProfile;

public class PgTestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() {
        return "pg";
    }
}
