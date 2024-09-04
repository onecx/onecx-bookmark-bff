package org.tkit.onecx.bookmark.bff.rs.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.ImagesInternalApiService;
import gen.org.tkit.onecx.product.store.client.api.ImagesApi;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ImagesRestController implements ImagesInternalApiService {

    @Inject
    @RestClient
    ImagesApi productStoreImageClient;

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
}
