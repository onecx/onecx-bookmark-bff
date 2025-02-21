package org.tkit.onecx.bookmark.bff.rs.mappers;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.bookmark.bff.rs.internal.model.*;
import gen.org.tkit.onecx.bookmark.client.model.*;
import gen.org.tkit.onecx.bookmark.exim.v1.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface BookmarkMapper {

    CreateBookmark map(CreateBookmarkDTO createBookmarkDTO);

    BookmarkSearchCriteria map(BookmarkSearchCriteriaDTO bookmarkSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    BookmarkPageResultDTO map(BookmarkPageResult bookmarkPageResult);

    @Mapping(target = "removeEndpointParametersItem", ignore = true)
    @Mapping(target = "removeQueryItem", ignore = true)
    BookmarkDTO map(Bookmark bookmark);

    UpdateBookmark map(UpdateBookmarkDTO updateBookmarkDTO);

    ExportBookmarksRequest mapExport(ExportBookmarksRequestDTO exportBookmarksRequestDTO);

    EximBookmarkScope map(EximBookmarkScopeDTO dto);

    @Mapping(target = "removeBookmarksItem", ignore = true)
    BookmarkSnapshotDTO mapSnapshot(BookmarkSnapshot bookmarkSnapshot);

    BookmarkSnapshot map(BookmarkSnapshotDTO snapshotDTO);

    Map<String, List<EximBookmarkDTO>> mapBookmarkMap(Map<String, List<EximBookmark>> bookmarks);

    Map<String, List<EximBookmark>> mapBookmarkMapDTO(Map<String, List<EximBookmarkDTO>> bookmarks);

    List<EximBookmarkDTO> map(List<EximBookmark> bookmarks);

    List<EximBookmark> mapEximDTOList(List<EximBookmarkDTO> bookmarks);

    @Mapping(target = "removeQueryItem", ignore = true)
    @Mapping(target = "removeEndpointParametersItem", ignore = true)
    EximBookmarkDTO map(EximBookmark bookmark);

    @Mapping(target = "workspaceName", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "position", ignore = true)
    EximBookmark map(EximBookmarkDTO bookmark);

    default ImportBookmarkRequest mapImport(ImportBookmarksRequestDTO importBookmarkRequestDTO) {
        ImportBookmarkRequest request = new ImportBookmarkRequest();
        request.setWorkspace(importBookmarkRequestDTO.getWorkspaceName());
        request.setImportMode(mapMode(importBookmarkRequestDTO.getImportMode()));
        request.setSnapshot(map(importBookmarkRequestDTO.getSnapshot()));
        request.getSnapshot().getBookmarks().values().forEach(eximBookmarks -> eximBookmarks.forEach(eximBookmark -> {
            eximBookmark.setWorkspaceName(importBookmarkRequestDTO.getWorkspaceName());
            eximBookmark.setPosition(0);
            eximBookmark.setScope(EximBookmarkScope.PRIVATE);
        }));

        return request;
    }

    EximMode mapMode(EximModeDTO importMode);
}
