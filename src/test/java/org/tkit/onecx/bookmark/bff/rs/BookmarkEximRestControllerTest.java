package org.tkit.onecx.bookmark.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.bookmark.bff.rs.controller.BookmarkEximRestController;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;
import gen.org.tkit.onecx.bookmark.exim.v1.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(BookmarkEximRestController.class)
class BookmarkEximRestControllerTest extends AbstractTest {
    private static final String BOOKMARK_SVC_EXIM_API_BASE_PATH = "/exim/v1/bookmark";

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
    void exportTest() {

        ExportBookmarksRequest request = new ExportBookmarksRequest();
        request.setScopes(List.of(EximBookmarkScope.PRIVATE));
        request.setWorkspaceName("workspace1");

        BookmarkSnapshot snapshot = new BookmarkSnapshot();
        snapshot.setBookmarks(Map.of(EximBookmarkScope.PRIVATE.toString(),
                List.of(new EximBookmark().scope(EximBookmarkScope.PRIVATE).url("someUrl"))));

        // create mock rest endpoint
        mockServerClient.when(request().withPath(BOOKMARK_SVC_EXIM_API_BASE_PATH + "/export").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(snapshot)));

        ExportBookmarksRequestDTO requestDTO = new ExportBookmarksRequestDTO();
        requestDTO.setScopes(List.of(EximBookmarkScopeDTO.PRIVATE));
        requestDTO.setWorkspaceName("workspace1");

        var data = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .body(requestDTO)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(BookmarkSnapshotDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getBookmarks().get(EximBookmarkScope.PRIVATE.toString())).hasSize(1);
    }

    @Test
    void importImagesTest() {
        BookmarkSnapshot snapshot = new BookmarkSnapshot();
        snapshot.setBookmarks(Map.of(EximBookmarkScope.PRIVATE.toString(),
                List.of(new EximBookmark().scope(EximBookmarkScope.PRIVATE).url("someUrl"))));
        ImportBookmarkRequest request = new ImportBookmarkRequest();
        request.setSnapshot(snapshot);
        request.setWorkspace("workspace2");
        request.setImportMode(EximMode.OVERWRITE);

        // create mock rest endpoint
        mockServerClient.when(request().withPath(BOOKMARK_SVC_EXIM_API_BASE_PATH + "/import").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode()));

        BookmarkSnapshotDTO snapshotDTO = new BookmarkSnapshotDTO();
        snapshotDTO.setBookmarks(Map.of(EximBookmarkScopeDTO.PRIVATE.toString(),
                List.of(new EximBookmarkDTO().scope(EximBookmarkScopeDTO.PRIVATE).url("someUrl"))));
        ImportBookmarksRequestDTO requestDTO = new ImportBookmarksRequestDTO();
        requestDTO.setSnapshot(snapshotDTO);
        requestDTO.setWorkspaceName("workspace2");
        requestDTO.setImportMode(EximModeDTO.OVERWRITE);

        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .body(requestDTO)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    void importNoSnapshotTest() {
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .post("/import")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void exportClientExceptionTest() {
        // create mock rest endpoint
        mockServerClient.when(request().withPath(BOOKMARK_SVC_EXIM_API_BASE_PATH + "/export").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(new ExportBookmarksRequest().workspaceName("w1"))))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .body(new ExportBookmarksRequestDTO().workspaceName("w1"))
                .post("/export")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }
}
