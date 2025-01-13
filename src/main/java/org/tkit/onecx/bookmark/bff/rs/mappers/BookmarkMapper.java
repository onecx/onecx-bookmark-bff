package org.tkit.onecx.bookmark.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;
import gen.org.tkit.onecx.bookmark.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface BookmarkMapper {

    CreateBookmark map(CreateBookmarkDTO createBookmarkDTO);

    BookmarkSearchCriteria map(BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    BookmarkPageResultDTO map(BookmarkPageResult bookmarkPageResult);

    @Mapping(target = "removeEndpointParametersItem", ignore = true)
    BookmarkDTO map(Bookmark bookmark);

    UpdateBookmark map(UpdateBookmarkDTO updateBookmarkDTO);

    @Mapping(target = "position", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "scope", constant = "PUBLIC")
    UpdateBookmark convert(UpdateBookmarkDTO convertBookmarkDTO);
}
