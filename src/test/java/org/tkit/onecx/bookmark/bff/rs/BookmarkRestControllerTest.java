package org.tkit.onecx.bookmark.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.*;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.tkit.onecx.bookmark.bff.rs.controller.BookmarkRestController;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;
import gen.org.tkit.onecx.bookmark.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(BookmarkRestController.class)
public class BookmarkRestControllerTest extends org.tkit.onecx.bookmark.bff.rs.AbstractTest {

    private static final String BOOKMARK_SVC_INTERNAL_API_BASE_PATH = "/internal/bookmarks";

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
    void createBookmark() {

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH)
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        var createBookmarkDTO = this.createBookmarkDTO("displayName1", "workspaceName1", "productName1", "appId1",
                "endpointName1", "query",
                "hash", CreateBookmarkDTO.ScopeEnum.PUBLIC, null);

        // bff call
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(createBookmarkDTO)
                .post()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Assertions.assertNotNull(response);

    }

    @Test
    void searchBookmarksByCriteria() {

        BookmarkPageResult bookmarkPageResult = new BookmarkPageResult();
        bookmarkPageResult.setNumber(0);
        bookmarkPageResult.setTotalPages(3L);
        bookmarkPageResult.setSize(50);
        List<Bookmark> bookmarkList = new ArrayList<>();
        bookmarkList.add(this.newBookmark("displayName1", "workspaceName1", "productName1", "appId1", "endpointName1", "query",
                "hash", Bookmark.ScopeEnum.PUBLIC, null));
        bookmarkList.add(this.newBookmark("displayName2", "workspaceName2", "productName2", "appId2", "endpointName2", "query2",
                "hash2", Bookmark.ScopeEnum.PUBLIC, null));
        bookmarkPageResult.setStream(bookmarkList);

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(bookmarkPageResult)));

        BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO = new BookmarkSearchCriteriaDTO();
        bookmarkSearchCriteriaDTO.setWorkspaceName("workspaceName1");
        bookmarkSearchCriteriaDTO.setProductName("productName1");
        bookmarkSearchCriteriaDTO.setAppId("appId1");
        bookmarkSearchCriteriaDTO.setScope("PUBLIC");

        // search the created bookmark
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(bookmarkSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(BookmarkPageResultDTO.class);

        Assertions.assertNotNull(res);
        Assertions.assertEquals(2, res.getStream().size());
    }

    @Test
    void searchBookmarksByCriteria_shouldReturnRequestError_whenCriteriaAreMissing() {

        BookmarkPageResult bookmarkPageResult = new BookmarkPageResult();
        bookmarkPageResult.setNumber(0);
        bookmarkPageResult.setTotalPages(3L);
        bookmarkPageResult.setTotalElements(20L);
        bookmarkPageResult.setSize(50);
        List<Bookmark> bookmarkList = new ArrayList<>();
        bookmarkList.add(this.newBookmark("displayName1", "workspaceName1", "productName1", "appId1", "endpointName1", "query",
                "hash", Bookmark.ScopeEnum.PUBLIC, null));
        bookmarkList.add(this.newBookmark("displayName2", "workspaceName2", "productName2", "appId2", "endpointName2", "query2",
                "hash2", Bookmark.ScopeEnum.PUBLIC, null));
        bookmarkPageResult.setStream(bookmarkList);

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(bookmarkPageResult)));

        BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO = new BookmarkSearchCriteriaDTO();
        bookmarkSearchCriteriaDTO.setProductName("productName1");
        bookmarkSearchCriteriaDTO.setAppId("appId1");

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(bookmarkSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    void updateBookmark() {
        String id = "82689h23-9624-2234-c50b-8749d073c287";

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                        .withMethod(HttpMethod.PUT))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        UpdateBookmarkDTO updateBookmarkDTO = new UpdateBookmarkDTO();
        updateBookmarkDTO.setDisplayName("newDisplayName");
        updateBookmarkDTO.setModificationCount(1);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(updateBookmarkDTO)
                .put(id)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    @Test
    void deleteBookmark() {
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
    void deleteBookmark_shouldReturnError_whenDeletionFailed() {
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
    void errorSecurityTest() {
        String id = "82689h23-9624-2234-c50b-8749d073c287";

        BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO = new BookmarkSearchCriteriaDTO();
        bookmarkSearchCriteriaDTO.setWorkspaceName("workspaceName1");
        bookmarkSearchCriteriaDTO.setProductName("productName1");
        bookmarkSearchCriteriaDTO.setAppId("appId1");

        // No auth
        given()
                .when()
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(bookmarkSearchCriteriaDTO)
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

    private CreateBookmark createBookmark(String displayName, String workspaceName, String productName, String appId,
            String endpointName,
            String query, String hash, CreateBookmark.ScopeEnum scope, Map<String, String> endpointParameters) {
        CreateBookmark bookmark = new CreateBookmark();
        bookmark.setDisplayName(displayName);
        bookmark.setWorkspaceName(workspaceName);
        bookmark.setProductName(productName);
        bookmark.setAppId(appId);
        bookmark.setEndpointName(endpointName);
        bookmark.setEndpointParameters(endpointParameters);
        bookmark.setQuery(query);
        bookmark.setHash(hash);
        bookmark.setScope(scope);
        return bookmark;
    }

    private CreateBookmarkDTO createBookmarkDTO(String displayName, String workspaceName, String productName, String appId,
            String endpointName,
            String query, String hash, CreateBookmarkDTO.ScopeEnum scope, Map<String, String> endpointParameters) {
        CreateBookmarkDTO bookmarkDTO = new CreateBookmarkDTO();
        bookmarkDTO.setDisplayName(displayName);
        bookmarkDTO.setWorkspaceName(workspaceName);
        bookmarkDTO.setProductName(productName);
        bookmarkDTO.setAppId(appId);
        bookmarkDTO.setEndpointName(endpointName);
        bookmarkDTO.setEndpointParameters(endpointParameters);
        bookmarkDTO.setQuery(query);
        bookmarkDTO.setHash(hash);
        bookmarkDTO.setScope(scope);
        return bookmarkDTO;
    }

    private Bookmark newBookmark(String displayName, String workspaceName, String productName, String appId,
            String endpointName,
            String query, String hash, Bookmark.ScopeEnum scope, Map<String, String> endpointParameters) {
        Bookmark bookmark = new Bookmark();
        bookmark.setDisplayName(displayName);
        bookmark.setWorkspaceName(workspaceName);
        bookmark.setProductName(productName);
        bookmark.setAppId(appId);
        bookmark.setEndpointName(endpointName);
        bookmark.setEndpointParameters(endpointParameters);
        bookmark.setQuery(query);
        bookmark.setHash(hash);
        bookmark.setScope(scope);
        return bookmark;
    }

}
