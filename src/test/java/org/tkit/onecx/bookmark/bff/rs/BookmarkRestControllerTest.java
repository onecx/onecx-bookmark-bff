package org.tkit.onecx.bookmark.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
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
import org.mockserver.model.MediaType;
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
                "endpointName1", Map.of("query1", "param"),
                "fragment", BookmarkScopeDTO.PUBLIC, null);

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
        bookmarkList.add(this.newBookmark("displayName1", "workspaceName1", "productName1", "appId1", "endpointName1",
                Map.of("query1", "param"),
                "fragment", BookmarkScope.PUBLIC, null));
        bookmarkList.add(this.newBookmark("displayName2", "workspaceName2", "productName2", "appId2", "endpointName2",
                Map.of("query1", "param"),
                "fragment2", BookmarkScope.PUBLIC, null));
        bookmarkPageResult.setStream(bookmarkList);

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(bookmarkPageResult)));

        BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO = new BookmarkSearchCriteriaDTO();
        bookmarkSearchCriteriaDTO.setWorkspaceName("workspaceName1");
        bookmarkSearchCriteriaDTO.setProductName("productName1");
        bookmarkSearchCriteriaDTO.setAppId("appId1");
        bookmarkSearchCriteriaDTO.setScope(BookmarkScopeDTO.PUBLIC);

        // search the created bookmark
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(bookmarkSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(BookmarkPageResultDTO.class);

        Assertions.assertNotNull(res);
        Assertions.assertEquals(2, res.getStream().size());
    }

    @Test
    void searchUserBookmarksByCriteria() {

        BookmarkPageResult pageResult = new BookmarkPageResult();
        pageResult.setStream(
                List.of(new Bookmark().scope(BookmarkScope.PRIVATE).displayName("bookmark1").workspaceName("workspace1")));
        pageResult.setSize(1);
        pageResult.setTotalPages(1L);
        pageResult.setTotalElements(1L);

        BookmarkSearchCriteria criteria = new BookmarkSearchCriteria();
        criteria.setWorkspaceName("workspace1");

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/user")
                        .withMethod(HttpMethod.POST).withBody(JsonBody.json(criteria)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO = new BookmarkSearchCriteriaDTO();
        bookmarkSearchCriteriaDTO.setWorkspaceName("workspace1");

        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(bookmarkSearchCriteriaDTO)
                .post("/user")
                .then()
                .statusCode(OK.getStatusCode()).extract().as(BookmarkPageResultDTO.class);

        Assertions.assertEquals(1, res.getStream().size());
    }

    @Test
    void searchBookmarksByCriteria_shouldReturnRequestError_whenCriteriaAreMissing() {

        BookmarkPageResult bookmarkPageResult = new BookmarkPageResult();
        bookmarkPageResult.setNumber(0);
        bookmarkPageResult.setTotalPages(3L);
        bookmarkPageResult.setTotalElements(20L);
        bookmarkPageResult.setSize(50);
        List<Bookmark> bookmarkList = new ArrayList<>();
        bookmarkList.add(this.newBookmark("displayName1", "workspaceName1", "productName1", "appId1", "endpointName1",
                Map.of("query1", "param"),
                "fragment", BookmarkScope.PUBLIC, null));
        bookmarkList.add(this.newBookmark("displayName2", "workspaceName2", "productName2", "appId2", "endpointName2",
                Map.of("query1", "param"),
                "fragment2", BookmarkScope.PUBLIC, null));
        bookmarkPageResult.setStream(bookmarkList);

        mockServerClient
                .when(request().withPath(BOOKMARK_SVC_INTERNAL_API_BASE_PATH + "/search")
                        .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
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
        updateBookmarkDTO.setPosition(1);
        updateBookmarkDTO.setId("82689h23-9624-2234-c50b-8749d073c287");

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
    void updateOrderTest() {
        UpdateBookmark bookmark = new UpdateBookmark();
        bookmark.setDisplayName("name");
        bookmark.setPosition(1);
        bookmark.setModificationCount(1);

        UpdateBookmark bookmark2 = new UpdateBookmark();
        bookmark2.setDisplayName("name2");
        bookmark2.setPosition(2);
        bookmark2.setModificationCount(2);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/bookmarks/11-111").withMethod(HttpMethod.PUT)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(bookmark)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(bookmark)));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/bookmarks/22-222").withMethod(HttpMethod.PUT)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(bookmark2)))
                .withId("mock2")
                .respond(httpRequest -> response().withStatusCode(CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(bookmark2)));

        UpdateBookmarkDTO updateBookmarkDTO = new UpdateBookmarkDTO();
        updateBookmarkDTO.setPosition(1);
        updateBookmarkDTO.setDisplayName("name");
        updateBookmarkDTO.setId("11-111");
        updateBookmarkDTO.setModificationCount(1);

        UpdateBookmarkDTO updateBookmarkDTO2 = new UpdateBookmarkDTO();
        updateBookmarkDTO2.setPosition(2);
        updateBookmarkDTO2.setDisplayName("name2");
        updateBookmarkDTO2.setId("22-222");
        updateBookmarkDTO2.setModificationCount(2);

        BookmarkReorderRequestDTO reorderRequestDTO = new BookmarkReorderRequestDTO();
        reorderRequestDTO.setBookmarks(List.of(updateBookmarkDTO, updateBookmarkDTO2));

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
        BookmarkReorderRequestDTO reorderRequestDTO = new BookmarkReorderRequestDTO();
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
            String endpointName, Map<String, String> query, String fragment, BookmarkScope scope,
            Map<String, String> endpointParameters) {
        CreateBookmark bookmark = new CreateBookmark();
        bookmark.setDisplayName(displayName);
        bookmark.setWorkspaceName(workspaceName);
        bookmark.setProductName(productName);
        bookmark.setAppId(appId);
        bookmark.setEndpointName(endpointName);
        bookmark.setEndpointParameters(endpointParameters);
        bookmark.setQuery(query);
        bookmark.setFragment(fragment);
        bookmark.setScope(scope);
        bookmark.setPosition(1);
        return bookmark;
    }

    private CreateBookmarkDTO createBookmarkDTO(String displayName, String workspaceName, String productName, String appId,
            String endpointName, Map<String, String> query, String fragment, BookmarkScopeDTO scope,
            Map<String, String> endpointParameters) {
        CreateBookmarkDTO bookmarkDTO = new CreateBookmarkDTO();
        bookmarkDTO.setDisplayName(displayName);
        bookmarkDTO.setWorkspaceName(workspaceName);
        bookmarkDTO.setProductName(productName);
        bookmarkDTO.setAppId(appId);
        bookmarkDTO.setEndpointName(endpointName);
        bookmarkDTO.setEndpointParameters(endpointParameters);
        bookmarkDTO.setQuery(query);
        bookmarkDTO.setFragment(fragment);
        bookmarkDTO.setScope(scope);
        bookmarkDTO.setPosition(1);
        return bookmarkDTO;
    }

    private Bookmark newBookmark(String displayName, String workspaceName, String productName, String appId,
            String endpointName, Map<String, String> query, String fragment, BookmarkScope scope,
            Map<String, String> endpointParameters) {
        Bookmark bookmark = new Bookmark();
        bookmark.setDisplayName(displayName);
        bookmark.setWorkspaceName(workspaceName);
        bookmark.setProductName(productName);
        bookmark.setAppId(appId);
        bookmark.setEndpointName(endpointName);
        bookmark.setEndpointParameters(endpointParameters);
        bookmark.setQuery(query);
        bookmark.setFragment(fragment);
        bookmark.setScope(scope);
        bookmark.setPosition(1);
        return bookmark;
    }

}
