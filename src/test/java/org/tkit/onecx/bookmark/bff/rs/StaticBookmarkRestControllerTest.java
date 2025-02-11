package org.tkit.onecx.bookmark.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.bookmark.bff.rs.controller.StaticBookmarkRestController;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;
import gen.org.tkit.onecx.bookmark.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(StaticBookmarkRestController.class)
public class StaticBookmarkRestControllerTest extends AbstractTest {

    private static final String BOOKMARK_SVC_INTERNAL_API_BASE_PATH = "/internal/static/bookmarks";

    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String MOCK_ID = "MOCK";

    @AfterEach
    void resetMockserver() {
        try {
            mockServerClient.clear(MOCK_ID);
        } catch (Exception ex) {
            // mockId not existing
        }
    }

    @Test
    void createStaticBookmark() {

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH)
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        var createStaticBookmarkDTO = this.createStaticBookmarkDTO("displayName1", "workspaceName1", "someUrl");

        // bff call
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(createStaticBookmarkDTO)
                .post()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Assertions.assertNotNull(response);

    }

    @Test
    void searchStaticBookmarksByCriteria() {

        StaticBookmarkPageResult staticBookmarkPageResult = new StaticBookmarkPageResult();
        staticBookmarkPageResult.setNumber(0);
        staticBookmarkPageResult.setTotalPages(3L);
        staticBookmarkPageResult.setSize(50);
        List<StaticBookmark> staticBookmarkList = new ArrayList<>();
        staticBookmarkList.add(this.newStaticBookmark("displayName1", "workspaceName1", "someUrl"));
        staticBookmarkList.add(this.newStaticBookmark("displayName2", "workspaceName2", "someUrl"));
        staticBookmarkPageResult.setStream(staticBookmarkList);

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(staticBookmarkPageResult)));

        StaticBookmarkSearchCriteriaDTO staticBookmarkSearchCriteriaDTO = new StaticBookmarkSearchCriteriaDTO();
        staticBookmarkSearchCriteriaDTO.setWorkspaceName("workspaceName1");

        // search the created staticBookmark
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(staticBookmarkSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(StaticBookmarkPageResultDTO.class);

        Assertions.assertNotNull(res);
        Assertions.assertEquals(2, res.getStream().size());
    }

    @Test
    void searchStaticBookmarksByCriteria_shouldReturnRequestError_whenCriteriaAreMissing() {

        StaticBookmarkPageResult staticBookmarkPageResult = new StaticBookmarkPageResult();
        staticBookmarkPageResult.setNumber(0);
        staticBookmarkPageResult.setTotalPages(3L);
        staticBookmarkPageResult.setTotalElements(20L);
        staticBookmarkPageResult.setSize(50);
        List<StaticBookmark> staticBookmarkList = new ArrayList<>();
        staticBookmarkList.add(this.newStaticBookmark("displayName1", "workspaceName1", "someUrl"));
        staticBookmarkList.add(this.newStaticBookmark("displayName2", "workspaceName2", "someUrl"));
        staticBookmarkPageResult.setStream(staticBookmarkList);

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(staticBookmarkPageResult)));

        StaticBookmarkSearchCriteriaDTO staticBookmarkSearchCriteriaDTO = new StaticBookmarkSearchCriteriaDTO();

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(staticBookmarkSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    void updateStaticBookmark() {
        String id = "82689h23-9624-2234-c50b-8749d073c287";

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                        .withMethod(HttpMethod.PUT))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        UpdateStaticBookmarkDTO updateStaticBookmarkDTO = new UpdateStaticBookmarkDTO();
        updateStaticBookmarkDTO.setDisplayName("newDisplayName");
        updateStaticBookmarkDTO.setModificationCount(1);
        updateStaticBookmarkDTO.setPosition(1);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(updateStaticBookmarkDTO)
                .put(id)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    @Test
    void deleteStaticBookmark() {
        String id = "82689h23-9624-2234-c50b-8749d073c287";

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                        .withMethod(HttpMethod.DELETE))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .delete(id)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    @Test
    void deleteStaticBookmark_shouldReturnError_whenDeletionFailed() {
        String id = "82689h23-9624-2234-c50b-8749d073c287";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("400");

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                        .withMethod(HttpMethod.DELETE))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(
                        httpRequest -> response()
                                .withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                                .withBody(JsonBody.json(problemDetailResponse)));

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .delete(id)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertEquals("400", res.getErrorCode());

    }

    @Test
    void updateOrderTest() {
        UpdateStaticBookmark staticBookmark = new UpdateStaticBookmark();
        staticBookmark.setDisplayName("name");
        staticBookmark.setPosition(1);
        staticBookmark.setModificationCount(1);

        UpdateStaticBookmark staticBookmark2 = new UpdateStaticBookmark();
        staticBookmark2.setDisplayName("name2");
        staticBookmark2.setPosition(2);
        staticBookmark2.setModificationCount(2);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/static/bookmarks/11-111").withMethod(HttpMethod.PUT)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(staticBookmark)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(staticBookmark)));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/static/bookmarks/22-222").withMethod(HttpMethod.PUT)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(staticBookmark2)))
                .withId("mock2")
                .respond(httpRequest -> response().withStatusCode(CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(staticBookmark2)));

        UpdateStaticBookmarkDTO updateStaticBookmarkDTO = new UpdateStaticBookmarkDTO();
        updateStaticBookmarkDTO.setPosition(1);
        updateStaticBookmarkDTO.setDisplayName("name");
        updateStaticBookmarkDTO.setId("11-111");
        updateStaticBookmarkDTO.setModificationCount(1);

        UpdateStaticBookmarkDTO updateStaticBookmarkDTO2 = new UpdateStaticBookmarkDTO();
        updateStaticBookmarkDTO2.setPosition(2);
        updateStaticBookmarkDTO2.setDisplayName("name2");
        updateStaticBookmarkDTO2.setId("22-222");
        updateStaticBookmarkDTO2.setModificationCount(2);

        StaticBookmarkReorderRequestDTO reorderRequestDTO = new StaticBookmarkReorderRequestDTO();
        reorderRequestDTO.setBookmarks(List.of(updateStaticBookmarkDTO, updateStaticBookmarkDTO2));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .body(reorderRequestDTO)
                .contentType(APPLICATION_JSON)
                .post("/reorder")
                .then()
                .statusCode(OK.getStatusCode());
        mockServerClient.clear("mock2");
        mockServerClient.clear(MOCK_ID);
    }

    @Test
    void updateOrderEmptyBodyTest() {
        StaticBookmarkReorderRequestDTO reorderRequestDTO = new StaticBookmarkReorderRequestDTO();
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .body(reorderRequestDTO)
                .contentType(APPLICATION_JSON)
                .post("/reorder")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

    }

    @Test
    void errorSecurityTest() {
        String id = "82689h23-9624-2234-c50b-8749d073c287";

        StaticBookmarkSearchCriteriaDTO staticBookmarkSearchCriteriaDTO = new StaticBookmarkSearchCriteriaDTO();
        staticBookmarkSearchCriteriaDTO.setWorkspaceName("workspaceName1");

        // No auth
        given()
                .when()
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(staticBookmarkSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());

        // FORBIDDEN when needed delete permission and USER have only read
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_HEADER_PARAM, USER)
                .contentType(APPLICATION_JSON)
                .delete(id)
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    private CreateStaticBookmark createStaticBookmark(String displayName, String workspaceName, String url) {
        CreateStaticBookmark staticBookmark = new CreateStaticBookmark();
        staticBookmark.setDisplayName(displayName);
        staticBookmark.setWorkspaceName(workspaceName);
        staticBookmark.setUrl(url);
        staticBookmark.setPosition(1);
        return staticBookmark;
    }

    private CreateStaticBookmarkDTO createStaticBookmarkDTO(String displayName, String workspaceName, String url) {
        CreateStaticBookmarkDTO staticBookmarkDTO = new CreateStaticBookmarkDTO();
        staticBookmarkDTO.setDisplayName(displayName);
        staticBookmarkDTO.setWorkspaceName(workspaceName);
        staticBookmarkDTO.setUrl(url);
        staticBookmarkDTO.setPosition(1);
        return staticBookmarkDTO;
    }

    private StaticBookmark newStaticBookmark(String displayName, String workspaceName, String url) {
        StaticBookmark staticBookmark = new StaticBookmark();
        staticBookmark.setDisplayName(displayName);
        staticBookmark.setWorkspaceName(workspaceName);
        staticBookmark.setUrl(url);
        staticBookmark.setPosition(1);
        return staticBookmark;
    }

}
