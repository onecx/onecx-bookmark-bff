package org.tkit.onecx.bookmark.bff.rs.controller;

import java.util.List;

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

import gen.org.tkit.onecx.bookmark.bff.rs.internal.BookmarkExportImportApiService;
import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;
import gen.org.tkit.onecx.bookmark.exim.v1.client.api.BookmarkExportImportApi;
import gen.org.tkit.onecx.bookmark.exim.v1.client.model.BookmarkSnapshot;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class BookmarkEximRestController implements BookmarkExportImportApiService {

    @Inject
    @RestClient
    BookmarkExportImportApi exportImportApi;

    @Inject
    BookmarkMapper bookmarkMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response exportBookmarks(ExportBookmarksRequestDTO exportBookmarksRequestDTO) {
        try (Response response = exportImportApi.exportBookmarks(bookmarkMapper.mapExport(exportBookmarksRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(bookmarkMapper.mapSnapshot(response.readEntity(BookmarkSnapshot.class))).build();
        }
    }

    @Override
    public Response importBookmarks(String workspaceName, BookmarkSnapshotDTO bookmarkSnapshotDTO, EximModeDTO importMode,
            List<EximBookmarkScopeDTO> scopes) {
        try (Response response = exportImportApi.importBookmarks(workspaceName,
                bookmarkMapper.mapImport(bookmarkSnapshotDTO, scopes), bookmarkMapper.mapMode(importMode))) {
            return Response.status(response.getStatus()).build();
        }
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
