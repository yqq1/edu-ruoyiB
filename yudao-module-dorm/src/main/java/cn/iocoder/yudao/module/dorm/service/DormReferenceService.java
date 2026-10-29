package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import lombok.Data;

public interface DormReferenceService {
    DormVO.ReferenceResp options();
    PageResult<StudentResp> students(DormQuery req);
    @Data
    class StudentResp {
        private Long id;
        private String studentNo;
        private String name;
        private Long classId;
        private String className;
        private String gender;
        private boolean enrolled;
    }
}
