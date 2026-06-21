package com.votescroll;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class PollResourceTest {
    @Test
    void listPollsReturns150() {
        given().when().get("/polls")
            .then().statusCode(200)
            .body("$.size()", greaterThanOrEqualTo(150));
    }

    @Test
    void listMultiPollsReturns10() {
        given().when().get("/multi-polls")
            .then().statusCode(200)
            .body("$.size()", equalTo(10));
    }
}
