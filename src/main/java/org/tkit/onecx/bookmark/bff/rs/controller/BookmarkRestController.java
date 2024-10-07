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
import org.tkit.onecx.bookmark.bff.rs.mappers.ProblemDetailMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.BookmarksInternalApiService;
import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;
import gen.org.tkit.onecx.bookmark.client.api.BookmarksInternalApi;
import gen.org.tkit.onecx.bookmark.client.model.BookmarkPageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class BookmarkRestController implements BookmarksInternalApiService {

    @Inject
    @RestClient
    BookmarksInternalApi client;

    @Inject
    BookmarkMapper bookmarkMapper;

    @Inject
    ProblemDetailMapper problemDetailMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response convertBookmark(UpdateBookmarkDTO updateBookmarkDTO) {
        try (Response response = client.updateBookmark(updateBookmarkDTO.getId(),
                bookmarkMapper.convert(updateBookmarkDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response createNewBookmark(CreateBookmarkDTO createBookmarkDTO) {
        try (Response response = client.createNewBookmark(bookmarkMapper.map(createBookmarkDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response deleteBookmarkById(String id) {
        try (Response response = client.deleteBookmarkById(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response searchBookmarksByCriteria(BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO) {
        try (Response response = client.searchBookmarksByCriteria(bookmarkMapper.map(bookmarkSearchCriteriaDTO))) {
            BookmarkPageResult bookmarkPageResult = response.readEntity(BookmarkPageResult.class);
            BookmarkPageResultDTO bookmarkPageResultDTO = bookmarkMapper.map(bookmarkPageResult);
            return Response.status(response.getStatus()).entity(bookmarkPageResultDTO).build();
        }
    }

    @Override
    public Response searchUserBookmarksByCriteria(BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO) {
        try (Response response = client.searchUserBookmarksByCriteria(bookmarkMapper.map(bookmarkSearchCriteriaDTO))) {
            BookmarkPageResult userBookmarkPageResult = response.readEntity(BookmarkPageResult.class);
            BookmarkPageResultDTO bookmarkPageResultDTO = bookmarkMapper.map(userBookmarkPageResult);
            return Response.status(Response.Status.OK).entity(bookmarkPageResultDTO).build();
        }
    }

    @Override
    public Response updateBookmark(String id, UpdateBookmarkDTO updateBookmarkDTO) {
        try (Response response = client.updateBookmark(id, bookmarkMapper.map(updateBookmarkDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response updateBookmarksOrder(BookmarkReorderRequestDTO bookmarkReorderRequestDTO) {
        if (!bookmarkReorderRequestDTO.getBookmarks().isEmpty()) {
            for (UpdateBookmarkDTO bookmarkDTO : bookmarkReorderRequestDTO.getBookmarks()) {
                client.updateBookmark(bookmarkDTO.getId(),
                        bookmarkMapper.map(bookmarkDTO)).close();
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
