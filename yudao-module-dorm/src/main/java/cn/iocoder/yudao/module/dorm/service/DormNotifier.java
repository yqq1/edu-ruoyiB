package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Component
public class DormNotifier {
    @Resource private DormReferences references;
    @Resource private NotifyMessageSendApi messages;
    @Resource private AdminUserApi users;
    public void counselors(Set<Long> classes, String detail) {
        if (classes.isEmpty()) return;
        Map<Long, Set<Long>> bindings = references.gateway().counselorAccounts(classes);
        require(bindings != null, "辅导员通知账号未显式绑定");
        Set<Long> accounts = new LinkedHashSet<>();
        for (Long classId : classes) {
            Set<Long> bound = bindings.get(classId);
            require(bound != null && !bound.isEmpty(), "每个受影响班级必须绑定辅导员通知账号");
            accounts.addAll(bound);
        }
        send(accounts, "dorm_alert", detail);
    }
    public void student(Long studentId, String detail) {
        Long account = references.student(studentId).getAccountId();
        require(account != null && account > 0, "学生通知账号未显式绑定");
        send(Collections.singleton(account), "dorm_change_result", detail);
    }
    private void send(Set<Long> accounts, String template, String detail) {
        accounts.forEach(DormRules::id);
        users.validateUserList(accounts);
        for (Long account : accounts) {
            NotifySendSingleToUserReqDTO req = new NotifySendSingleToUserReqDTO();
            req.setUserId(account);
            req.setTemplateCode(template);
            req.setTemplateParams(Collections.<String, Object>singletonMap("detail", detail));
            require(messages.sendSingleMessageToAdmin(req) != null, "宿舍通知未入库，请检查账号和通知模板");
        }
    }
}
