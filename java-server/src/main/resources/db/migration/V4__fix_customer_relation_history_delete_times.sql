ALTER TABLE `customer_staff_relation_history`
  MODIFY COLUMN `customer_delete_staff_at` datetime(3) COMMENT '客户删除员工的时间',
  MODIFY COLUMN `staff_delete_customer_at` datetime(3) COMMENT '员工删除客户的时间';
