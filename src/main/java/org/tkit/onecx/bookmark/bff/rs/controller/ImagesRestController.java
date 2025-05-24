package org.tkit.onecx.bookmark.bff.rs.controller;

import java.io.File;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.bookmark.bff.rs.mappers.BookmarkMapper;
import org.tkit.onecx.bookmark.bff.rs.mappers.ExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.ImagesInternalApiService;
import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.bookmark.image.client.api.ImagesInternalApi;
import gen.org.tkit.onecx.bookmark.image.client.model.ImageInfo;
import gen.org.tkit.onecx.product.store.client.api.ImagesApi;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ImagesRestController implements ImagesInternalApiService {

    @Inject
    @RestClient
    ImagesApi productStoreImageClient;

    @Inject
    @RestClient
    ImagesInternalApi bookmarkImageClient;

    @Inject
    HttpHeaders headers;

    @Inject
    BookmarkMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response deleteImage(String refId) {
        try (Response response = bookmarkImageClient.deleteImage(refId)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getImage(String refId) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = bookmarkImageClient.getImage(refId)) {
            var contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            var contentLength = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
            var body = response.readEntity(byte[].class);
            if (contentType != null && body.length != 0) {
                responseBuilder = Response.status(response.getStatus())
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                        .entity(body);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }

            return responseBuilder.build();
        }
    }

    @Override
    public Response getProductLogo(String productName) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = productStoreImageClient.getProductLogo(productName)) {
            var contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            var contentLength = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
            var body = response.readEntity(byte[].class);
            if (contentType != null && body.length != 0) {
                responseBuilder = Response.status(response.getStatus())
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                        .entity(body);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }

            return responseBuilder.build();
        }
    }

    @Override
    public Response uploadImage(String refId, File body) {
        try (Response response = bookmarkImageClient.uploadImage(headers.getLength(), refId, body)) {
            ImageInfoDTO imageInfoDTO = mapper.mapImageInfo(response.readEntity(ImageInfo.class));
            return Response.status(response.getStatus()).entity(imageInfoDTO).build();
        }
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
