package org.tkit.onecx.bookmark.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.util.Objects;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.tkit.onecx.bookmark.bff.rs.controller.ImagesRestController;

import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(ImagesRestController.class)
public class ImagesRestControllerTest extends AbstractTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final String MEDIA_TYPE_IMAGE_PNG = "image/png";
    private static final String MEDIA_TYPE_IMAGE_JPG = "image/jpg";

    private static final File FILE = new File(
            Objects.requireNonNull(ImagesRestControllerTest.class.getResource("/images/Testimage.png")).getFile());

    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String MOCK_ID = "MOCK";

    @BeforeEach
    void resetExpectation() {

        try {
            mockServerClient.clear(MOCK_ID);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    public void getProductLogo_Test() {
        System.out.println("LOGO_CALLED");

        var refId = "productName";
        byte[] bytesRes = new byte[] { (byte) 0xe0, 0x4f, (byte) 0xd0,
                0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b,
                0x30, 0x30, (byte) 0x9d };

        mockServerClient.when(request()
                .withPath("/v1/image/" + refId + "/logo")
                .withMethod(HttpMethod.GET))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withHeaders(
                                new Header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_PNG))
                        .withBody(bytesRes));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("productName", refId)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_PNG)
                .extract().body().asByteArray();

        assertThat(res).isNotNull().isNotEmpty();
    }

    @Test
    public void getProductLogo_Empty_Body_Test() {

        var refId = "productName";
        byte[] bytesRes = new byte[] {};

        mockServerClient.when(request()
                .withPath("/v1/image/" + refId + "/logo")
                .withMethod(HttpMethod.GET))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withHeaders(
                                new Header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_PNG))
                        .withBody(bytesRes));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("productName", refId)
                .get()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

    }

    @Test
    public void getProductLogo_Missing_ContentType_Test() {

        var refId = "productName";
        byte[] bytesRes = new byte[] { (byte) 0xe0, 0x4f, (byte) 0xd0,
                0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b,
                0x30, 0x30, (byte) 0x9d };

        mockServerClient.when(request()
                .withPath("/v1/image/" + refId + "/logo")
                .withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withBody(bytesRes));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("productName", refId)
                .get()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

    }

}
