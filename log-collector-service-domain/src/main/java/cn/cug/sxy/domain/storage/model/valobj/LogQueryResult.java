package cn.cug.sxy.domain.storage.model.valobj;

import cn.cug.sxy.domain.storage.model.entity.LogDocument;
import cn.cug.sxy.types.model.ValueObject;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @version 1.0
 * @Date 2025/7/15 08:48
 * @Description 日志查询结果值对象
 * @Author jerryhotton
 */

@Getter
@Builder
public class LogQueryResult implements ValueObject {

    /**
     * 日志列表
     */
    private List<LogDocument> logs;
    /**
     * 总命中数
     */
    private long totalHits;
    /**
     * 当前页码
     */
    private int pageNumber;
    /**
     * 每页大小
     */
    private int pageSize;
    /**
     * 获取总页数
     *
     * @return 总页数
     */
    public int getTotalPages() {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalHits / pageSize);
    }
    /**
     * 是否有下一页
     *
     * @return 是否有下一页
     */
    public boolean hasNext() {
        return pageNumber < getTotalPages();
    }
    /**
     * 是否有上一页
     *
     * @return 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNumber > 1;
    }

}
