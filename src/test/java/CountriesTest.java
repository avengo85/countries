import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.*;
import org.json.JSONObject;

import static io.restassured.RestAssured.*;

public class CountriesTest {
    public static final String URL_ALL = "http://services.groupkt.com/country/get/all";
    public static final String URL_COUNTRY = "http://services.groupkt.com/country/get/iso2code/{COUNTRY_ISO2CODE}";
    public static final String URL_ADD_COUNTRY = "http://services.groupkt.com/country/register/";
    public static final String RESULT = "RestResponse.result.";

// Trying to keep universality - we can just add more fields to responses or add more countries to the array without changing the code.

    public static final String[][] RESPONSE_ITEMS = {{"name", "alpha2_code", "alpha3_code", "RestResponse.messages"},
            {"United States of America", "Germany", "United Kingdom of Great Britain and Northern Ireland", "Test Country"},
            {"US", "DE", "GB", "TC"},
            {"USA", "DEU", "GBR", "TCY"},
            {"Country found matching code ", "No matching country found for requested code "}};
    public Response responseAll, response;


    @Test
    public void getAllTest() {

        responseAll = given().get(URL_ALL);
        responseAll.then().statusCode(200);
//            for every field in 'RestResponse.result." ...
        for (int k = 0; k < RESPONSE_ITEMS[2].length - 1; k++) {
            responseAll.then().body(RESULT + RESPONSE_ITEMS[0][1], Matchers.hasItem(RESPONSE_ITEMS[2][k]));
        }

    }

    @Test
    public void getOneCountryTest() {
//            for every Country except Test Country...
        for (int i = 0; i < RESPONSE_ITEMS[1].length - 1; i++) {
            response = given().get(URL_COUNTRY, RESPONSE_ITEMS[2][i]);
            response.then().statusCode(200);
            Assert.assertTrue("Message is wrong for " + RESPONSE_ITEMS[2][i], response.jsonPath().get(RESPONSE_ITEMS[0][3]).toString().
                    contains(RESPONSE_ITEMS[4][0] + "[" + RESPONSE_ITEMS[2][i] + "]"));
//            for every field in 'RestResponse.result." ...
            for (int j = 0; j < RESPONSE_ITEMS[0].length - 1; j++) {
                response.then().body(RESULT + RESPONSE_ITEMS[0][j], Matchers.equalTo(RESPONSE_ITEMS[j + 1][i]));
            }
        }
    }

    @Test
    public void getInexistentCountryTest() {
        response = given().get(URL_COUNTRY, RESPONSE_ITEMS[3][0]);
        response.then().statusCode(200);
        Assert.assertTrue("Message is wrong for inexistent country", response.jsonPath().get(RESPONSE_ITEMS[0][3]).toString().
                contains(RESPONSE_ITEMS[4][1] + "[" + RESPONSE_ITEMS[3][0] + "]"));
    }

    @Test
    public void postTest() {
// We do not have DELETE method, then we cannot reuse authomatically the test for POST method without using of DELETE or adding a plenty of rubbish instances
        JSONObject requestParams = new JSONObject();
        int lastField = RESPONSE_ITEMS[0].length - 1;
//  add all existing fields to JSON object:
        for (int i = 0; i < lastField; i++) {
            requestParams.put(RESPONSE_ITEMS[0][i], RESPONSE_ITEMS[i + 1][lastField]);
        }
        RequestSpecification request = given();
        request.header("Content-Type", "application/json");
        request.body(requestParams.toString());
        response = request.post(URL_ADD_COUNTRY);
        response.then().statusCode(201);

    }

}
