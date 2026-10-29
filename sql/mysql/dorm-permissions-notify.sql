INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, always_show)
SELECT '宿舍管理权限', '', 1, 82, 0, 'dorm-permissions', '', '', 0, b'0', b'0', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path='dorm-permissions' AND type=1 AND deleted=b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, always_show)
SELECT p.name, p.permission, 3, p.sort, root.id, '', '', '', 0, b'0', b'0', b'0'
FROM (
  SELECT '宿舍管理员' name, 'dorm:admin' permission, 1 sort
  UNION ALL SELECT '创建宿舍楼', 'dorm:building:create', 2
  UNION ALL SELECT '修改宿舍楼', 'dorm:building:update', 3
  UNION ALL SELECT '删除宿舍楼', 'dorm:building:delete', 4
  UNION ALL SELECT '宿舍楼分页', 'dorm:building:query', 5
  UNION ALL SELECT '创建房间', 'dorm:room:create', 6
  UNION ALL SELECT '启停床位', 'dorm:room:update', 7
  UNION ALL SELECT '删除房间', 'dorm:room:delete', 8
  UNION ALL SELECT '房间床位', 'dorm:room:query', 9
  UNION ALL SELECT '空床选项', 'dorm:bed:query', 10
  UNION ALL SELECT '批量自动分配', 'dorm:stay:assign', 11
  UNION ALL SELECT '取消待入住', 'dorm:stay:cancel', 12
  UNION ALL SELECT '办理入住', 'dorm:stay:check-in', 13
  UNION ALL SELECT '办理退宿', 'dorm:stay:check-out', 14
  UNION ALL SELECT '确认线下退款', 'dorm:stay:refund', 15
  UNION ALL SELECT '住宿分页', 'dorm:stay:query', 16
  UNION ALL SELECT '本人住宿', 'dorm:stay:my', 17
  UNION ALL SELECT '申请调宿', 'dorm:change-request:create', 18
  UNION ALL SELECT '取消本人申请', 'dorm:change-request:cancel', 19
  UNION ALL SELECT '拒绝调宿', 'dorm:change-request:approve', 20
  UNION ALL SELECT '工作人员调床', 'dorm:change-request:direct', 21
  UNION ALL SELECT '调宿申请分页', 'dorm:change-request:query', 22
  UNION ALL SELECT '查寝历史分页', 'dorm:night-check:query', 23
  UNION ALL SELECT '保存查寝结果', 'dorm:night-check:update', 24
  UNION ALL SELECT '更正卫生评分', 'dorm:hygiene:update', 25
  UNION ALL SELECT '卫生周月排名', 'dorm:hygiene:query', 26
  UNION ALL SELECT '关联纪律处分', 'dorm:violation:update', 27
  UNION ALL SELECT '违规分页', 'dorm:violation:query', 28
  UNION ALL SELECT '返校打卡', 'dorm:travel:check-in', 29
  UNION ALL SELECT '审核离校记录', 'dorm:travel:review', 30
  UNION ALL SELECT '按出行记录的班级汇总', 'dorm:travel:query', 31
  UNION ALL SELECT '授权学生分页', 'dorm:reference:query', 32
  UNION ALL SELECT '读取授权离校照片', 'dorm:photo:query', 33
) p
JOIN (SELECT MIN(id) id FROM system_menu WHERE path='dorm-permissions' AND type=1 AND deleted=b'0') root
WHERE root.id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM system_menu m WHERE m.permission=p.permission AND m.deleted=b'0');

INSERT INTO system_notify_template (name, code, type, nickname, content, params, status, remark)
SELECT '宿舍检查预警', 'dorm_alert', 1, '宿舍管理', '{detail}', '["detail"]', 0, ''
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='dorm_alert' AND deleted=b'0');

INSERT INTO system_notify_template (name, code, type, nickname, content, params, status, remark)
SELECT '调宿处理结果', 'dorm_change_result', 1, '宿舍管理', '{detail}', '["detail"]', 0, ''
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='dorm_change_result' AND deleted=b'0');
