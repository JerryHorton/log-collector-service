package cn.cug.sxy.domain.reception.model.entity;

import cn.cug.sxy.domain.reception.model.aggregate.AppAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @Date 2025/7/8 09:24
 * @Description 权限校验中动态上下文
 * @Author jerryhotton
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationDynamicContext {

    private AppAccess appAccess;

}
