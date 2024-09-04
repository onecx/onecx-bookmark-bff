package org.tkit.onecx.bookmark.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;

@ApplicationScoped
public class BffLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, CreateBookmarkDTO.class, x -> {
                    CreateBookmarkDTO d = (CreateBookmarkDTO) x;
                    return CreateBookmarkDTO.class.getSimpleName() + "[" + d.getDisplayName() + "," + d.getProductName() + "]";
                }),
                item(10, UpdateBookmarkDTO.class, x -> {
                    UpdateBookmarkDTO d = (UpdateBookmarkDTO) x;
                    return UpdateBookmarkDTO.class.getSimpleName() + "[" + d.getDisplayName() + "," + d.getModificationCount()
                            + "]";
                }),
                item(10, BookmarkSearchCriteriaDTO.class, x -> {
                    BookmarkSearchCriteriaDTO d = (BookmarkSearchCriteriaDTO) x;
                    return BookmarkSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + ","
                            + d.getPageSize()
                            + "]";
                }));
    }
}
