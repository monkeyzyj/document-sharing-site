package com.jiaruiblog.controller;

import com.jiaruiblog.auth.Permission;
import com.jiaruiblog.auth.PermissionEnum;
import com.jiaruiblog.common.MessageConstant;
import com.jiaruiblog.entity.BasePageDTO;
import com.jiaruiblog.entity.User;
import com.jiaruiblog.entity.dto.BatchIdDTO;
import com.jiaruiblog.entity.dto.RefuseBatchDTO;
import com.jiaruiblog.entity.dto.RefuseDTO;
import com.jiaruiblog.service.DocReviewService;
import com.jiaruiblog.service.impl.UserServiceImpl;
import com.jiaruiblog.util.BaseApiResult;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 文档评审，日志查询
 *
 * @Author Jarrett Luo
 * @Date 2022/11/25 15:56
 * @Version 1.0
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/docReview")
public class DocReviewController {

    @Autowired
    private DocReviewService docReviewService;

    @Autowired
    private UserServiceImpl userServiceImpl;

    /**
     * 普通用户、管理员用户，列表查询
     * 该接口必须在登录条件下才能查询
     * 普通普通查询到自己上传的文档
     * 管理员查询到所有的文档评审信息
     * 必须是管理员才有资格进行评审
     *
     * @return BaseApiResult
     */
    @Permission({PermissionEnum.ADMIN})
    @ApiOperation(value = "查询需要评审的文档列表", notes = "查询需要评审的文档列表")
    @GetMapping("queryDocForReview")
    public BaseApiResult queryDocReviewList(@ModelAttribute("pageParams") @Valid BasePageDTO pageParams) {
        return docReviewService.queryReviewsByPage(pageParams);
    }


    /**
     * 修改已读， 只有普通用户有此权限
     * 修改评审意见为通过
     * 用户必须是文档的上传人
     *
     * @return BaseApiResult
     */
    @Permission({PermissionEnum.USER})
    @ApiOperation(value = "修改已读", notes = "修改已读功能只有普通用户有此权限")
    @PutMapping("userRead")
    public BaseApiResult updateDocReview(@RequestBody @Valid BatchIdDTO batchIdDTO, HttpServletRequest request) {
        String userId = (String) request.getAttribute("id");
        return docReviewService.userRead(batchIdDTO.getIds(), userId);
    }

    /**
     * @return com.jiaruiblog.util.BaseApiResult
     * @Author luojiarui
     * @Description 单个进行拒绝
     * @Date 21:12 2022/11/30
     * @Param [docId, reason]
     **/
    @Permission({PermissionEnum.ADMIN})
    @ApiOperation(value = "管理员拒绝某个文档", notes = "管理员拒绝某个文档，只有管理员有操作该文档的权限")
    @PostMapping("refuse")
    public BaseApiResult refuse(@RequestBody @Validated RefuseDTO refuseDTO) {
        String docId = refuseDTO.getDocId();
        String reason = refuseDTO.getReason();
        return docReviewService.refuse(docId, reason);
    }

    /**
     * @return com.jiaruiblog.util.BaseApiResult
     * @Author luojiarui
     * @Description 批量进行拒绝，并删除文档
     * @Date 21:12 2022/11/30
     * @Param [docIds]
     **/
    @Permission({PermissionEnum.ADMIN})
    @ApiOperation(value = "管理员拒绝一批文档", notes = "管理员拒绝一批文档，只有管理员有操作该文档的权限")
    @PostMapping("refuseBatch")
    public BaseApiResult refuseBatch(@RequestBody @Valid RefuseBatchDTO refuseBatchDTO) {
        return docReviewService.refuseBatch(refuseBatchDTO.getIds(), refuseBatchDTO.getReason());
    }

    /**
     * @Author luojiarui
     * @Description  缺少同意文档的信息
     * @Date 22:04 2022/12/9
     * @Param [batchIdDTO]
     * @return com.jiaruiblog.util.BaseApiResult
     **/
    @Permission({PermissionEnum.ADMIN})
    @ApiOperation(value = "统一某一批文档", notes = "管理员同意某一批文档")
    @PostMapping("approve")
    public BaseApiResult approve(@RequestBody @Valid BatchIdDTO batchIdDTO) {
        return docReviewService.approveBatch(batchIdDTO.getIds());
    }

    /**
     * @return com.jiaruiblog.util.BaseApiResult
     * @Author luojiarui
     * @Description 管理员和普通用户分别查询
     * @Date 21:15 2022/11/30
     * @Param [pageParams, request]
     **/
    @Permission({PermissionEnum.USER, PermissionEnum.ADMIN})
    @ApiOperation(value = "管理员和普通用户分别查询数据", notes = "查询文档审批的列表")
    @GetMapping("queryReviewResultList")
    public BaseApiResult queryReviewResultList(@ModelAttribute("pageParams") @Valid BasePageDTO pageParams,
                                               HttpServletRequest request) {
        String userId = (String) request.getAttribute("id");
        User user = userServiceImpl.queryById(userId);
        if (user == null) {
            return BaseApiResult.error(MessageConstant.PARAMS_ERROR_CODE, MessageConstant.PARAMS_FORMAT_ERROR);
        }
        return docReviewService.queryReviewLog(pageParams, new User());
    }


    /**
     * 普通用户删除，管理员删除，删除评审日志
     * @return BaseApiResult
     */
    @ApiOperation(value = "2.6 更新评论", notes = "更新评论")
    @DeleteMapping("removeDocReview")
    public BaseApiResult removeDocReview(@RequestBody @Valid BatchIdDTO batchIdDTO, HttpServletRequest request) {
        String userId = (String) request.getAttribute("id");
        User user = userServiceImpl.queryById(userId);
        if (user == null) {
            return BaseApiResult.error(MessageConstant.PARAMS_ERROR_CODE, MessageConstant.PARAMS_FORMAT_ERROR);
        }
        return docReviewService.deleteReviewsBatch(batchIdDTO.getIds());
    }


    /**
     * @return com.jiaruiblog.util.BaseApiResult
     * @Author luojiarui
     * @Description 系统用户日志查询
     * @Date 21:16 2022/11/30
     * @Param [pageParams]
     **/
    @Permission({PermissionEnum.ADMIN})
    @ApiOperation(value = "管理员查询系统日志信息", notes = "只有管理员有权限查询日志列表")
    @GetMapping("queryLogList")
    public BaseApiResult queryLogList(@ModelAttribute("pageParams") @Valid BasePageDTO pageParams) {
        return docReviewService.queryDocLogs(pageParams, new User());
    }

    /**
     * @return com.jiaruiblog.util.BaseApiResult
     * @Author luojiarui
     * @Description 删除用户日志
     * @Date 21:16 2022/11/30
     * @Param [logIds]
     **/
    @Permission(PermissionEnum.ADMIN)
    @ApiOperation(value = "管理员删除文档信息", notes = "只有管理员有权限删除文档的日志")
    @DeleteMapping("removeLog")
    public BaseApiResult removeLog(@RequestBody @Valid BatchIdDTO batchIdDTO) {
        List<String> logIds = batchIdDTO.getIds();
        return docReviewService.deleteDocLogBatch(logIds);
    }

    @Permission({PermissionEnum.ADMIN})
    @ApiOperation(value = "管理员修改系统设置", notes = "只有管理员有权限修改系统的设置信息")
    @PutMapping("systemConfig")
    public BaseApiResult systemConfig(@RequestBody String params) {
        return BaseApiResult.success();
    }

}
