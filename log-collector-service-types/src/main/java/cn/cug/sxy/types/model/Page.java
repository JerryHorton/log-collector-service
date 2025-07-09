package cn.cug.sxy.types.model;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/7 15:11
 * @Description 分页结果包装类（用于表示分页查询结果）
 * @Author jerryhotton
 */

@Getter
public class Page<T> {

    /**
     * 当前页内容
     */
    private final List<T> content;
    /**
     * 当前页码（从0或1开始，具体看实现）
     */
    private final int pageNumber;
    /**
     * 每页条数
     */
    private final int pageSize;
    /**
     * 总元素数
     */
    private final long totalElements;
    /**
     * 总页数
     */
    private final int totalPages;

    public Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = Collections.unmodifiableList(content);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
    }

    /**
     * 检查是否有内容
     *
     * @return 是否有内容
     */
    public boolean hasContent() {
        return !content.isEmpty();
    }

    /**
     * 检查是否是第一页
     *
     * @return 是否是第一页
     */
    public boolean isFirst() {
        return pageNumber == 0;
    }

    /**
     * 检查是否是最后一页
     *
     * @return 是否是最后一页
     */
    public boolean isLast() {
        return pageNumber == totalPages - 1;
    }

    /**
     * 检查是否有下一页
     *
     * @return 是否有下一页
     */
    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }

    /**
     * 检查是否有上一页
     *
     * @return 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page<?> page = (Page<?>) o;
        return pageNumber == page.pageNumber &&
                pageSize == page.pageSize &&
                totalElements == page.totalElements &&
                totalPages == page.totalPages &&
                Objects.equals(content, page.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, pageNumber, pageSize, totalElements, totalPages);
    }

}
