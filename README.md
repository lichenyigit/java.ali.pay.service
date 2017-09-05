# java.ali.pay.service
支付宝扫码支付demo

项目download完毕后，用idea打开，首先将支付宝面对面支付的sdk中的jar包按照maven的依赖规则放到自定的位置。
例如：
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>20161213173952</version>
</dependency>
alipay-sdk-java包指的是本地仓库下com.alibaba/alipay-sdk-java-20161213173952.jar

把ALIPayConstants文件中的信息补全；
content.xml文件中的数据库链接补全；
执行SQL语句；
tomcat7:run启动项目；

SQL:
CREATE TABLE `pay_server_log` (
  `id` varchar(32) NOT NULL COMMENT '主键id',
  `order_id` varchar(32) NOT NULL COMMENT '订单id（自己商户系统的订单id）',
  `third_order_id` varchar(32) NOT NULL COMMENT '第三方支付异步回调返回的订单id',
  `third_platform` varchar(32) NOT NULL COMMENT '第三方支付平台名称',
  `total_fee` decimal(10,2) NOT NULL COMMENT '总支付金额',
  `description` text COMMENT '系统调用此服务的描述',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信用户的open id（仅微信用户会有）',
  `third_pre_pay_data` text COMMENT '第三方预支付订单信息',
  `third_notify_data` text COMMENT '第三方支付返回的字符串',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间（此条数据创建的时间）',
  PRIMARY KEY (`id`,`order_id`,`third_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付服务日志表';

CREATE TABLE `request_log` (
  `client_ip` varchar(20) NOT NULL COMMENT '请求ip',
  `request_type` varchar(10) NOT NULL COMMENT 'url请求类型请求类型',
  `request_url` text NOT NULL COMMENT '请求url',
  `request_parameters` text COMMENT '请求参数',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据写入时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付服务请求日志表';
