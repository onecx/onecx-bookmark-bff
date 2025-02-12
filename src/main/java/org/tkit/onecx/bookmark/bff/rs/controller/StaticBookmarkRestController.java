package org.tkit.onecx.bookmark.bff.rs.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.bookmark.bff.rs.mappers.BookmarkMapper;
import org.tkit.onecx.bookmark.bff.rs.mappers.ExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.StaticBookmarksApiService;
import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;
import gen.org.tkit.onecx.bookmark.client.api.StaticBookmarksInternalApi;
import gen.org.tkit.onecx.bookmark.client.model.StaticBookmarkPageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class StaticBookmarkRestController implements StaticBookmarksApiService {

    @Inject
    @RestClient
    StaticBookmarksInternalApi client;

    @Inject
    BookmarkMapper bookmarkMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createNewStaticBookmark(CreateStaticBookmarkDTO createStaticBookmarkDTO) {
        try (Response response = client.createNewStaticBookmark(bookmarkMapper.mapStatic(createStaticBookmarkDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response deleteStaticBookmarkById(String id) {
        try (Response response = client.deleteStaticBookmarkById(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response searchStaticBookmarksByCriteria(StaticBookmarkSearchCriteriaDTO staticBookmarkSearchCriteriaDTO) {
        try (Response response = client
                .searchStaticBookmarksByCriteria(bookmarkMapper.mapStaticCriteria(staticBookmarkSearchCriteriaDTO))) {
            StaticBookmarkPageResult bookmarkPageResult = response.readEntity(StaticBookmarkPageResult.class);
            StaticBookmarkPageResultDTO bookmarkPageResultDTO = bookmarkMapper.mapStaticPageResult(bookmarkPageResult);
            return Response.status(response.getStatus()).entity(bookmarkPageResultDTO).build();
        }
    }

    @Override
    public Response updateStaticBookmark(String id, UpdateStaticBookmarkDTO updateStaticBookmarkDTO) {
        try (Response response = client.updateStaticBookmark(id, bookmarkMapper.updateStatic(updateStaticBookmarkDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response updateStaticBookmarksOrder(StaticBookmarkReorderRequestDTO staticBookmarkReorderRequestDTO) {
        if (!staticBookmarkReorderRequestDTO.getBookmarks().isEmpty()) {
            for (UpdateStaticBookmarkDTO bookmarkDTO : staticBookmarkReorderRequestDTO.getBookmarks()) {
                client.updateStaticBookmark(bookmarkDTO.getId(),
                        bookmarkMapper.updateStatic(bookmarkDTO)).close();
            }
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }

}
