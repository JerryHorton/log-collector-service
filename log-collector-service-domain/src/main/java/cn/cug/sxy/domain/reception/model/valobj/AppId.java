package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.exception.AppException;
import cn.cug.sxy.types.model.Identifier;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @Date 2025/7/7 11:45
 * @Description 应用ID值对象
 * @Author jerryhotton
 */

public class AppId implements Identifier {

    private final String value;

    // 应用ID格式正则表达式：字母开头，可包含字母、数字、下划线、连字符，长度3-32
    private static final Pattern APP_ID_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]{2,31}$");


    public AppId(String value) {
        validateAppId(value);
        this.value = value;
    }

    /**
     * 验证应用ID格式
     * @param appId 应用ID
     */
    private void validateAppId(String appId) {
        if (appId == null || !APP_ID_PATTERN.matcher(appId).matches()) {
            throw new AppException("无效的应用ID格式，必须以字母开头，长度3-32位，可包含字母、数字、下划线或连字符");
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppId appId = (AppId) o;
        return Objects.equals(value, appId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
