package com.webank.webase.node.mgr.precompiled;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precompiled.entity.ConsensusHandle;
import com.webank.webase.node.mgr.precompiled.entity.CrudHandle;
import com.webank.webase.node.mgr.precompiled.permission.PermissionParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Service
public class PrecompiledService {

    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private FrontInterfaceService frontInterfaceService;

    /**
     * get cns list
     */
    public Object listCnsService(int groupId, String contractName, String version, int pageSize, int pageNumber) {
        log.debug("start listCnsService. param:{}" + groupId + contractName + version);
        String uri;
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("contractName", contractName);
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        if(Objects.isNull(version)) {
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CNS_LIST, map);
        } else {
            map.put("version", version);
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CNS_LIST, map);
        }

        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end listCnsService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * get node list with consensus status
     */
    public Object getNodeListService(int groupId, int pageSize, int pageNumber) {
        log.debug("start getNodeListService. param:{}" + groupId);
        String uri;
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CONSENSUS_LIST, map);

        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end getNodeListService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }


    /**
     * post node manage consensus status
     */

    public Object nodeManageService(ConsensusHandle consensusHandle) {
        log.debug("start nodeManageService. param:{}", JSON.toJSONString(consensusHandle));
        if (Objects.isNull(consensusHandle)) {
            log.info("fail nodeManageService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.postForEntity(
                consensusHandle.getGroupId(), FrontRestTools.URI_PERMISSION,
                consensusHandle, Object.class);
        log.debug("end nodeManageService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     *  post CRUD opperation
     */

    public Object crudService(CrudHandle crudHandle) {
        log.debug("start crudService. param:{}", JSON.toJSONString(crudHandle));
        if (Objects.isNull(crudHandle)) {
            log.info("fail crudService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.deleteForEntity(
                crudHandle.getGroupId(), FrontRestTools.URI_PERMISSION,
                crudHandle, Object.class);
        log.debug("end crudService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }
}
