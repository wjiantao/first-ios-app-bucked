package com.shiguang.service.impl;

import com.shiguang.dto.MapWorksQueryDTO;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.MapWorkMapper;
import com.shiguang.vo.MapWorkVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapServiceImplTest {

    @Mock
    private MapWorkMapper mapWorkMapper;

    @InjectMocks
    private MapServiceImpl mapService;

    private MapWorksQueryDTO normalQuery() {
        MapWorksQueryDTO query = new MapWorksQueryDTO();
        query.setWest(116.0);
        query.setSouth(28.0);
        query.setEast(122.0);
        query.setNorth(32.0);
        return query;
    }

    private MapWorkVO work(String id, String sortTime) {
        return MapWorkVO.builder()
                .id(id)
                .title("作品" + id)
                .authorName("地图作者")
                .latitude(30.0)
                .longitude(120.0)
                .likeCount(1L)
                .sortTime(LocalDateTime.parse(sortTime))
                .build();
    }

    @Test
    void listVisible_shouldQuerySingleRangeWhenWestBeforeEast() {
        List<MapWorkVO> rows = List.of(work("w-1", "2026-09-03T10:00:00"));
        when(mapWorkMapper.selectVisible(116.0, 122.0, 28.0, 32.0, 500))
                .thenReturn(rows);

        List<MapWorkVO> result = mapService.listVisible(normalQuery());

        assertEquals(rows, result);
        assertEquals("地图作者", result.get(0).getAuthorName());
        verify(mapWorkMapper).selectVisible(116.0, 122.0, 28.0, 32.0, 500);
    }

    @Test
    void listVisible_shouldSplitAntimeridianAndMergeByTime() {
        MapWorksQueryDTO query = normalQuery();
        query.setWest(170.0);
        query.setEast(-170.0);
        query.setLimit(2);

        when(mapWorkMapper.selectVisible(170.0, 180.0, 28.0, 32.0, 2))
                .thenReturn(List.of(
                        work("w-new", "2026-09-04T10:00:00"),
                        work("w-mid", "2026-09-02T10:00:00")));
        when(mapWorkMapper.selectVisible(-180.0, -170.0, 28.0, 32.0, 2))
                .thenReturn(List.of(
                        work("w-new", "2026-09-04T10:00:00"),
                        work("w-old", "2026-09-01T10:00:00")));

        List<MapWorkVO> result = mapService.listVisible(query);

        assertEquals(2, result.size());
        assertEquals("w-new", result.get(0).getId());
        assertEquals("w-mid", result.get(1).getId());
    }

    @Test
    void listVisible_shouldClampLimitToMaximum() {
        MapWorksQueryDTO query = normalQuery();
        query.setLimit(2000);
        when(mapWorkMapper.selectVisible(116.0, 122.0, 28.0, 32.0, 1000))
                .thenReturn(List.of());

        mapService.listVisible(query);

        verify(mapWorkMapper).selectVisible(116.0, 122.0, 28.0, 32.0, 1000);
    }

    @Test
    void listVisible_shouldRejectMissingBounds() {
        MapWorksQueryDTO query = new MapWorksQueryDTO();
        assertThrows(BusinessException.class, () -> mapService.listVisible(query));
    }

    @Test
    void listVisible_shouldRejectInvalidLatitude() {
        MapWorksQueryDTO query = normalQuery();
        query.setNorth(100.0);
        assertThrows(BusinessException.class, () -> mapService.listVisible(query));
    }

    @Test
    void listVisible_shouldRejectNonFiniteBounds() {
        MapWorksQueryDTO query = normalQuery();
        query.setWest(Double.NaN);

        assertThrows(BusinessException.class, () -> mapService.listVisible(query));
    }

    @Test
    void listVisible_shouldRejectNullQuery() {
        assertThrows(BusinessException.class, () -> mapService.listVisible(null));
    }
}
