package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.TripResp;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormTravelVO.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;
import java.util.List;

public interface DormTravelService {
    ContextResp context();
    TripResp depart(PositionReq req, MultipartFile photo);
    TripResp returnToSchool(ReturnReq req);
    void review(TravelReview req);
    TripResp get(Long id);
    PageResult<TripResp> page(DormQuery req);
    List<ClassSummary> summary(DormQuery req);
    PhotoPayload photo(Long id);
    @Data
    class PhotoPayload {
        private byte[] content;
        private String contentType;
    }
}
