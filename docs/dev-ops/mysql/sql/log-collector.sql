-- 1. 日志接收端点表 (log_receiver_endpoint)
CREATE TABLE `log_receiver_endpoint`
(
    `id`                    bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `endpoint_id`           varchar(64)  NOT NULL COMMENT '端点唯一标识',
    `name`                  varchar(128) NOT NULL COMMENT '端点名称',
    `type`                  varchar(32)  NOT NULL COMMENT '端点类型(HTTP/SDK/AGENT)',
    `protocol`              varchar(32)  NOT NULL COMMENT '协议(HTTP/HTTPS/TCP/UDP)',
    `path`                  varchar(255)          DEFAULT NULL COMMENT '接收路径(HTTP路径或Socket地址)',
    `port`                  int(11)               DEFAULT NULL COMMENT '端口号',
    `format`                varchar(32)  NOT NULL COMMENT '接收格式(JSON/TEXT/BINARY)',
    `max_payload_size`      int(11)      NOT NULL DEFAULT '1048576' COMMENT '最大负载大小(字节)',
    `compression_enabled`   tinyint(1)   NOT NULL DEFAULT '0' COMMENT '是否启用压缩',
    `compression_algorithm` varchar(32)           DEFAULT NULL COMMENT '压缩算法(GZIP/DEFLATE)',
    `status`                varchar(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
    `created_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_endpoint_id` (`endpoint_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='日志接收端点表';

-- 2. 接入应用表 (log_app_access)
CREATE TABLE `log_app_access`
(
    `id`                bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `app_id`            varchar(64)  NOT NULL COMMENT '应用ID',
    `app_name`          varchar(128) NOT NULL COMMENT '应用名称',
    `access_key`        varchar(64)  NOT NULL COMMENT '访问密钥',
    `secret_key`        varchar(128) NOT NULL COMMENT '密钥(加密存储)',
    `allowed_endpoints` varchar(512)          DEFAULT NULL COMMENT '允许的端点ID列表(逗号分隔)',
    `ip_whitelist`      varchar(512)          DEFAULT NULL COMMENT 'IP白名单(逗号分隔)',
    `rate_limit`        int(11)      NOT NULL DEFAULT '1000' COMMENT '速率限制(次/分钟)',
    `status`            varchar(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE/BLOCKED)',
    `expiry_time`       datetime              DEFAULT NULL COMMENT '过期时间',
    `created_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_id` (`app_id`),
    UNIQUE KEY `uk_access_key` (`access_key`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='接入应用表';

-- 3. 接收日志缓冲表 (log_reception_buffer)
CREATE TABLE `log_reception_buffer`
(
    `id`             bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `batch_id`       varchar(64) NOT NULL COMMENT '批次ID',
    `app_id`         varchar(64) NOT NULL COMMENT '应用ID',
    `endpoint_id`    varchar(64) NOT NULL COMMENT '接收端点ID',
    `log_count`      int(11)     NOT NULL COMMENT '日志条数',
    `payload_size`   int(11)     NOT NULL COMMENT '负载大小(字节)',
    `status`         varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/PROCESSED/FAILED)',
    `error_message`  varchar(512)         DEFAULT NULL COMMENT '错误信息',
    `received_time`  datetime    NOT NULL COMMENT '接收时间',
    `processed_time` datetime             DEFAULT NULL COMMENT '处理时间',
    `created_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_id` (`batch_id`),
    KEY `idx_app_id` (`app_id`),
    KEY `idx_endpoint_id` (`endpoint_id`),
    KEY `idx_status` (`status`),
    KEY `idx_received_time` (`received_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='接收日志缓冲表';

-- 4. 接收统计表 (log_reception_stats)
CREATE TABLE `log_reception_stats`
(
    `id`               bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `app_id`           varchar(64) NOT NULL COMMENT '应用ID',
    `endpoint_id`      varchar(64) NOT NULL COMMENT '接收端点ID',
    `stat_date`        date        NOT NULL COMMENT '统计日期',
    `stat_hour`        int(11)     NOT NULL COMMENT '统计小时(0-23)',
    `received_count`   bigint(20)  NOT NULL DEFAULT '0' COMMENT '接收日志数',
    `received_bytes`   bigint(20)  NOT NULL DEFAULT '0' COMMENT '接收字节数',
    `success_count`    bigint(20)  NOT NULL DEFAULT '0' COMMENT '成功处理数',
    `failed_count`     bigint(20)  NOT NULL DEFAULT '0' COMMENT '失败处理数',
    `avg_process_time` int(11)     NOT NULL DEFAULT '0' COMMENT '平均处理时间(毫秒)',
    `max_process_time` int(11)     NOT NULL DEFAULT '0' COMMENT '最大处理时间(毫秒)',
    `created_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_endpoint_date_hour` (`app_id`, `endpoint_id`, `stat_date`, `stat_hour`),
    KEY `idx_stat_date` (`stat_date`),
    KEY `idx_app_id` (`app_id`),
    KEY `idx_endpoint_id` (`endpoint_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='接收统计表';

-- 5. 接收认证日志表 (log_reception_auth_log)
CREATE TABLE `log_reception_auth_log`
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `app_id`        varchar(64)          DEFAULT NULL COMMENT '应用ID',
    `endpoint_id`   varchar(64) NOT NULL COMMENT '接收端点ID',
    `client_ip`     varchar(64) NOT NULL COMMENT '客户端IP',
    `auth_method`   varchar(32) NOT NULL COMMENT '认证方法(ACCESSKEY/TOKEN/CERT)',
    `auth_result`   varchar(16) NOT NULL COMMENT '认证结果(SUCCESS/FAILED)',
    `error_code`    varchar(32)          DEFAULT NULL COMMENT '错误代码',
    `error_message` varchar(255)         DEFAULT NULL COMMENT '错误信息',
    `request_time`  datetime    NOT NULL COMMENT '请求时间',
    `created_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_app_id` (`app_id`),
    KEY `idx_endpoint_id` (`endpoint_id`),
    KEY `idx_auth_result` (`auth_result`),
    KEY `idx_request_time` (`request_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='接收认证日志表';