package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormResourceVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormStayVO.VersionReq;
import java.util.List;

public interface DormResourceService {
    Long saveBuilding(BuildingSave req, boolean create);
    void deleteBuilding(VersionReq req);
    BuildingResp getBuilding(Long id);
    PageResult<BuildingResp> buildingPage(DormQuery req);
    Long saveRoom(RoomSave req, boolean create);
    void saveBed(BedSave req);
    void deleteRoom(VersionReq req);
    RoomResp getRoom(Long id);
    PageResult<RoomResp> roomPage(DormQuery req);
    PageResult<BedResp> bedOptions(DormQuery req);
    List<BedResp> beds(Long roomId);
}
