import com.github.javafaker.Faker;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class AKAKCE {

    Faker randomUreteci = new Faker();
    RequestSpecification requestSpec;
    int bookingID;
    String tokenPassword;
    String rndFirstName = randomUreteci.name().firstName();
    String rndLastName = randomUreteci.name().lastName();

    @BeforeClass
    public void setup() {
        baseURI = "https://restful-booker.herokuapp.com";
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(baseURI)
                .build();
    }

    @Test()
    public void allBookingList() {
        given().
                spec(requestSpec)

                .when()
                .get("/booking")

                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test(dependsOnMethods = "allBookingList")
    public void postAuthorization() {
        Map<String, String> token = new HashMap<>();
        token.put("username", "admin");
        token.put("password", "password123");

        tokenPassword =
                given()
                        .spec(requestSpec)
                        .body(token)

                        .when()
                        .post("/auth")

                        .then()
                        .extract().path("token");
    }

    @Test(dependsOnMethods = "postAuthorization")
    public void creatingBooking() {

        Map<String, Object> bookingdates = new HashMap<>();
        bookingdates.put("checkin", "2023-01-01");
        bookingdates.put("checkout", "2024-01-01");

        Map<String, Object> body = new HashMap<>();
        body.put("firstname", "Büşra");
        body.put("lastname", "ÜNLÜ");
        body.put("totalprice", 111);
        body.put("depositpaid", true);
        body.put("bookingdates", bookingdates);
        body.put("additionalneeds", "Breakfast");

        bookingID =
                given()
                        .spec(requestSpec)
                        .body(body)
                        .log().body()

                        .when()
                        .post("/booking")

                        .then()
                        .log().body()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract().path("bookingid");
    }

    @Test(dependsOnMethods = "creatingBooking")
    public void updatesBooking() {
        Map<String, Object> bookingdates = new HashMap<>();
        bookingdates.put("checkin", "2023-01-01");
        bookingdates.put("checkout", "2024-01-01");

        Map<String, Object> newBody = new HashMap<>();
        newBody.put("firstname", rndFirstName);
        newBody.put("lastname", rndLastName);
        newBody.put("totalprice", 111);
        newBody.put("depositpaid", true);
        newBody.put("bookingdates", bookingdates);
        newBody.put("additionalneeds", "Breakfast");

        given()
                .spec(requestSpec)
                .cookie("token", tokenPassword)
                .body(newBody)
                .log().body()

                .when()
                .put("/booking/" + bookingID)

                .then()
                .contentType(ContentType.JSON)
                .log().body()
                .body("firstname", equalTo(rndFirstName))
                .body("lastname", equalTo(rndLastName));
    }

    @Test(dependsOnMethods = "updatesBooking")
    public void partialUpdateBooking() {
        Map<String, Object> partialUpdateBody = new HashMap<>();
        partialUpdateBody.put("firstname", "Büşra");
        partialUpdateBody.put("lastname", "Kuzucu");

        given()
                .spec(requestSpec)
                .cookie("token", tokenPassword)
                .body(partialUpdateBody)

                .when()
                .patch("/booking/" + bookingID)

                .then()
                .log().body()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .body("firstname", equalTo("Büşra"))
                .body("lastname", equalTo("Kuzucu"));
    }

    @Test(dependsOnMethods = "partialUpdateBooking")
    public void deleteBooking() {
        given().
                spec(requestSpec)
                .cookie("token", tokenPassword)
                .pathParam("pp1", "booking")

                .when()
                .delete("/{pp1}/" + bookingID)

                .then()
                .statusCode(201);
    }
}
