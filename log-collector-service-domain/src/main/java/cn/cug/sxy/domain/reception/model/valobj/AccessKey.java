package cn.cug.sxy.domain.reception.model.valobj;

import cn.cug.sxy.types.exception.AppException;
import cn.cug.sxy.types.model.Identifier;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @Date 2025/7/7 15:39
 * @Description 访问密钥值对象（用于标识应用的访问凭证）
 * @Author jerryhotton
 */

public class AccessKey implements Identifier {

    private final String value;

    // 访问密钥格式正则表达式：字母、数字、下划线、连字符，长度16-64
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{16,64}$");

    /**
     * 构造函数
     *
     * @param value 访问密钥值
     */
    public AccessKey(String value) {
        validateKey(value);
        this.value = value;
    }

    /**
     * 生成新的随机访问密钥
     *
     * @return 新的访问密钥
     */
    public static AccessKey generate() {
        // 生成一个长度为32的随机UUID字符串，去除连字符
        String randomKey = UUID.randomUUID().toString().replace("-", "");
        return new AccessKey(randomKey);
    }

    /**
     * 验证密钥格式
     *
     * @param key 访问密钥
     */
    private void validateKey(String key) {
        if (key == null || !KEY_PATTERN.matcher(key).matches()) {
            throw new AppException("无效的访问密钥格式，必须为16-64位的字母、数字、下划线或连字符");
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取部分掩码的密钥，用于显示
     *
     * @return 掩码后的密钥
     */
    public String getMaskedValue() {
        if (value.length() <= 8) {
            return value;
        }

        // 显示前4位和后4位，中间用*代替
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessKey accessKey = (AccessKey) o;
        return Objects.equals(value, accessKey.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return getMaskedValue();
    }

}
