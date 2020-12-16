/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.deploy.controller;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.deploy.entity.ReqDeploy;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.deploy.service.HostService;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * todo add host, delete host, init host
 */
@Log4j2
@RestController
@RequestMapping("host")
public class HostController {
    @Autowired
    private TbHostMapper tbHostMapper;
    @Autowired
    private HostService hostService;

    /**
     * list added host
     * @return
     * @throws IOException
     */
    @GetMapping(value = "list")
    public BaseResponse listHost() throws IOException {
        Instant startTime = Instant.now();
        log.info("Start get host list info, now:[{}]",  startTime);
        List<TbHost> resList = this.tbHostMapper.selectAll();
        return new BaseResponse(ConstantCode.SUCCESS, resList);
    }

    /**
     * Deploy by ipconf and tagId.
     */
//    @PostMapping(value = "add")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public BaseResponse addHost(@RequestBody @Valid ReqDeploy deploy,
//        BindingResult result) throws NodeMgrException {
//        checkBindResult(result);
//        Instant startTime = Instant.now();
//        deploy.setWebaseSignAddr(constantProperties.getWebaseSignAddress());
//        deploy.setRootDirOnHost(constantProperties.getRootDirOnHost());
//        log.info("Start deploy:[{}], start:[{}]", JsonTools.toJSONString(deploy), startTime);
//
//        try {
//            // generate node config and return shell execution log
//            this.deployService.deployChain(deploy.getChainName(),
//                deploy.getIpconf(), deploy.getTagId(), deploy.getRootDirOnHost(),
//                deploy.getWebaseSignAddr(),deploy.getDockerImageType());
//
//            return new BaseResponse(ConstantCode.SUCCESS);
//        } catch (NodeMgrException e) {
//            return new BaseResponse(e.getRetCode());
//        }
//    }

}