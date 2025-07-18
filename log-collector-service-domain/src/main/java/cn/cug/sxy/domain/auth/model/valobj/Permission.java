package cn.cug.sxy.domain.auth.model.valobj;

import cn.cug.sxy.types.model.ValueObject;
import lombok.Getter;

import java.util.Objects;

/**
 * @version 1.0
 * @Date 2025/7/10 10:20
 * @Description 权限值对象（表示系统中的一个权限）
 * @Author jerryhotton
 */

@Getter
public class Permission implements ValueObject {

    /**
     * 权限类型
     */
    private final PermissionType type;

    /**
     * 权限目标资源
     */
    private final String resource;

    /**
     * 权限操作
     */
    private final String action;

    /**
     * 创建一个权限
     */
    public Permission(PermissionType type, String resource, String action) {
        this.type = type;
        this.resource = resource;
        this.action = action;
    }

    /**
     * 创建一个端点访问权限
     */
    public static Permission forEndpoint(String endpointId) {
        return new Permission(PermissionType.ENDPOINT_ACCESS, endpointId, "access");
    }

    /**
     * 创建一个API调用权限
     */
    public static Permission forApiCall(String apiPath, String method) {
        return new Permission(PermissionType.API_CALL, apiPath, method);
    }

    /**
     * 创建一个资源操作权限
     */
    public static Permission forResource(String resourceType, String resourceId, String operation) {
        return new Permission(PermissionType.RESOURCE_OPERATION, resourceType + ":" + resourceId, operation);
    }

    /**
     * 创建一个功能访问权限
     */
    public static Permission forFeature(String featureName) {
        return new Permission(PermissionType.FEATURE_ACCESS, featureName, "access");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return type == that.type &&
                Objects.equals(resource, that.resource) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, resource, action);
    }

    @Override
    public String toString() {
        return type + ":" + resource + ":" + action;
    }

    /**
     * 权限类型枚举
     */
    public enum PermissionType {
        /**
         * 端点访问权限
         */
        ENDPOINT_ACCESS,

        /**
         * API调用权限
         */
        API_CALL,

        /**
         * 资源操作权限
         */
        RESOURCE_OPERATION,

        /**
         * 功能访问权限
         */
        FEATURE_ACCESS
    }

}
