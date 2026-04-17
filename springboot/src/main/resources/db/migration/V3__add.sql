-- 或者方式B: 为SUPER_ADMIN也添加首页菜单
INSERT INTO menu (name, path, parent_id, icon, order_num, role, component, status)
VALUES ('首页', '/manager/index', 0, 'House', 1, 'SUPER_ADMIN', 'Index.vue', 'ENABLE');
