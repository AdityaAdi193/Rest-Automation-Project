
package com.qa.api.tests;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BookingAPITests {

    private String token;
    private int bookingId;

    private static final String BASE_URL =
            "https://restful-booker.herokuapp.com";

    @BeforeClass
    public void setUp() {

        RestAssured.baseURI = BASE_URL;

        String authBody = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        System.out.println("========== API SETUP ==========");

        try {
            Response response = given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(authBody)
                    .when()
                    .post("/auth");

            System.out.println("Authentication status: "
                    + response.getStatusCode());
            System.out.println("Authentication response: "
                    + response.asString());

            response.then().statusCode(200);

            token = response.jsonPath().getString("token");

            if (token == null || token.isBlank()) {
                throw new IllegalStateException(
                        "Authentication token is missing.");
            }

            System.out.println("Authentication successful.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "API setup/authentication failed.", e);
        }
    }

    // Reusable request specification
    private RequestSpecification request() {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    // Reusable request payload
    private String bookingPayload(String firstName) {
        return """
                {
                    "firstname": "%s",
                    "lastname": "Automation",
                    "totalprice": 1500,
                    "depositpaid": true,
                    "bookingdates": {
                        "checkin": "2027-01-10",
                        "checkout": "2027-01-15"
                    },
                    "additionalneeds": "Breakfast"
                }
                """.formatted(firstName);
    }

    // Test 1: Get all booking IDs
    @Test(priority = 1)
    public void getAllBookingIds() {

        Response response = request()
                .when()
                .get("/booking");

        System.out.println("GET booking IDs status: "
                + response.getStatusCode());

        response.then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0));

        System.out.println("PASS: Get all booking IDs");
    }

    // Test 2: Create a booking
    @Test(priority = 2)
    public void createBooking() {

        String payload = bookingPayload("Aditya");

        System.out.println("========== CREATE BOOKING ==========");
        System.out.println("Request body: " + payload);

        Response response = request()
                .body(payload)
                .when()
                .post("/booking");

        System.out.println("Create booking HTTP status: "
                + response.getStatusCode());
        System.out.println("Create booking response body: "
                + response.asString());

        // Validate status first
        if (response.getStatusCode() != 200) {
            throw new AssertionError(
                    "Create booking failed. HTTP status: "
                            + response.getStatusCode()
                            + ", response: " + response.asString());
        }

        response.then()
                .contentType(ContentType.JSON)
                .body("bookingid", notNullValue())
                .body("booking.firstname", equalTo("Aditya"))
                .body("booking.lastname", equalTo("Automation"))
                .body("booking.totalprice", equalTo(1500))
                .body("booking.depositpaid", equalTo(true))
                .body("booking.bookingdates.checkin",
                        equalTo("2027-01-10"));

        bookingId = response.jsonPath().getInt("bookingid");

        if (bookingId <= 0) {
            throw new AssertionError(
                    "The API returned an invalid booking ID.");
        }

        System.out.println("PASS: Booking created. ID = "
                + bookingId);
    }

    // Test 3: Retrieve booking and validate schema
    @Test(priority = 3, dependsOnMethods = "createBooking")
    public void getBookingByIdAndValidateSchema() {

        request()
                .pathParam("id", bookingId)
                .when()
                .get("/booking/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath(
                        "booking-schema.json"))
                .body("firstname", equalTo("Aditya"))
                .body("lastname", equalTo("Automation"));

        System.out.println("PASS: Get booking and schema validation");
    }

    // Test 4: Full update
    @Test(priority = 4, dependsOnMethods = "createBooking")
    public void updateBooking() {

        Response response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookie("token", token)
                .pathParam("id", bookingId)
                .body(bookingPayload("AdityaUpdated"))
                .when()
                .put("/booking/{id}");

        System.out.println("PUT status: "
                + response.getStatusCode());
        System.out.println("PUT response: "
                + response.asString());

        response.then()
                .statusCode(200)
                .body("firstname", equalTo("AdityaUpdated"))
                .body("lastname", equalTo("Automation"))
                .body("totalprice", equalTo(1500));

        System.out.println("PASS: Full booking update");
    }

    // Test 5: Partial update
    @Test(priority = 5, dependsOnMethods = "updateBooking")
    public void partialUpdateBooking() {

        String patchBody = """
                {
                    "firstname": "AdityaPatched"
                }
                """;

        Response response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookie("token", token)
                .pathParam("id", bookingId)
                .body(patchBody)
                .when()
                .patch("/booking/{id}");

        System.out.println("PATCH status: "
                + response.getStatusCode());
        System.out.println("PATCH response: "
                + response.asString());

        response.then()
                .statusCode(200)
                .body("firstname", equalTo("AdityaPatched"));

        System.out.println("PASS: Partial booking update");
    }

    // Test 6: Negative scenario
    @Test(priority = 6)
    public void invalidBookingIdReturns404() {

        Response response = request()
                .pathParam("id", 999999999)
                .when()
                .get("/booking/{id}");

        System.out.println("Invalid ID status: "
                + response.getStatusCode());

        response.then().statusCode(404);

        System.out.println("PASS: Invalid booking ID scenario");
    }

    // Test 7: Delete booking and verify deletion
    @Test(priority = 7, dependsOnMethods = "partialUpdateBooking")
    public void deleteBooking() {

        Response response = given()
                .accept(ContentType.JSON)
                .cookie("token", token)
                .pathParam("id", bookingId)
                .when()
                .delete("/booking/{id}");

        System.out.println("DELETE status: "
                + response.getStatusCode());
        System.out.println("DELETE response: "
                + response.asString());

        response.then().statusCode(201);

        Response verificationResponse = request()
                .pathParam("id", bookingId)
                .when()
                .get("/booking/{id}");

        verificationResponse.then().statusCode(404);

        System.out.println("PASS: Delete and verify booking");
    }
}
