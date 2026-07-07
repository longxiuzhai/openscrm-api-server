package cn.openscrm.api.common.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private final List<T> items;
    private final long totalRows;
    private final long page;
    private final long pageSize;
}
