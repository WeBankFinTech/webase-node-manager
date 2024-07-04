/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.node.mgr.statistic;

import com.webank.common.mybatis.helper.DataPermissionHelper;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.service.ConfigService;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.front.frontinterface.entity.RspStatBlock;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.GroupGeneral;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.statistic.entity.ChainStat;
import com.webank.webase.node.mgr.statistic.entity.TbStat;
import com.webank.webase.node.mgr.statistic.mapper.TbStatMapper;
import com.webank.webase.node.mgr.statistic.result.Data;
import com.webank.webase.node.mgr.statistic.result.LineDataList;
import com.webank.webase.node.mgr.statistic.result.PerformanceData;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import com.webank.webase.node.mgr.tools.pagetools.List2Page;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.v3.client.protocol.response.TotalTransactionCount;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class StatService {

    @Autowired
    private TbStatMapper tbStatMapper;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private ConstantProperties constants;

    @Autowired
    private CryptoSuite cryptoSuite;

    @Autowired
    private ConfigService configService;

    @Lazy
    @Autowired
    private GroupService groupService;

    @Autowired
    private FrontInterfaceService frontInterface;


    // 每出一个块，获得上一个区块的时间戳相差时间，获得出块周期； 获取多条数据，计算平均值
    // 获得一个块的交易数，除以出块周期则是TPS
    // 交易数是区块大小，
    // 出块了才保存数据，

    // 每5s统计一次，每次计算区块链高度是否变化，没有则tps为0，区块大小不变，区块周期大小不变/周期叠加
    // 出块了则计算两次区块的高度查，算出tps
    // 计算当前块高减去DB最大块高，计算多个区块在这5s内的数据：tps是交易数加起来除以timestamp时间差，区块大小算5s内的平均值，cycle为相邻区块平均值

    /**
     * 以区块的时间戳为记录，出块时记录数据。
     * select的时候，以5s为粒度分离数据，
     * 如0-5s有三个区块，则三个区块的值累加到0-5区间，
     * 如果5-15都没有区块，则5-10, 10-15插入空数据
     * 如15-20有数据，则记录在15-20的区间
     * @param latch
     * @param groupId
     */
    @Async(value = "mgrAsyncExecutor")
    public void pullBlockStatistic(CountDownLatch latch, String groupId) {
        try {
            Instant startTime = Instant.now();
            log.debug("pullBlockStatistic startTime:{}, groupId:{}", startTime, groupId);
            TbStat latestStat = tbStatMapper.getMaxByGroupId(groupId);
            log.debug("pullBlockStatistic local latestStat :{}", latestStat);
            // local largest block num of group id
            int localBlockNum;
            // chain block timestamp
            Long lastBlockTimestamp;
            if (latestStat != null) {
                localBlockNum = latestStat.getBlockNumber();
                lastBlockTimestamp = Long.parseLong(latestStat.getStatTimestamp());
            } else {
                log.info("groupId:{} local block is null, start pull from zero", groupId);
                // if no stat data local, pull from zero
                localBlockNum = 0;
                RspStatBlock zeroBlock = frontInterfaceService.getBlockStatisticByNumber(groupId, BigInteger.ZERO);
                lastBlockTimestamp = zeroBlock.getTimestamp();
            }
            // largest block num on chain
            int blockNumOnChain;
            try {
                blockNumOnChain = frontInterfaceService.getLatestBlockNumber(groupId).intValue();
            } catch (Exception e) {
                log.error("pullBlockStatistic get latest block num error:{}", e.getMessage());
                return;
            }
            // if chain's height is zero, not pull
            if (localBlockNum >= blockNumOnChain) {
                log.debug("pullBlockStatistic jump for local is :{}, block height is :{}!",
                    localBlockNum, blockNumOnChain);
                return;
            }
            // if local is too far from chain's height, just pull
            if (blockNumOnChain - localBlockNum > constants.getStatBlockPageSize()) {
                log.debug(
                    "pullBlockStatistic local {} is too far away from latest blockHeight:{}, pull 10 block one time",
                    localBlockNum, blockNumOnChain);
                blockNumOnChain += constants.getStatBlockPageSize();
            }

            for (int height = localBlockNum + 1; height <= blockNumOnChain; height++) {
//                if (height >= blockNumOnChain) {
//                    return;
//                }
                log.debug("pullBlockStatistic height:{}", height);
                RspStatBlock chainBlockStat = frontInterfaceService.getBlockStatisticByNumber(groupId,
                        new BigInteger(String.valueOf(height)));
                if (chainBlockStat == null) {
                    log.error("pullBlockStatistic getBlockStatisticByNumber on chain get null");
                    return;
                }
                Long chainTimestamp = chainBlockStat.getTimestamp();
                // get time interval(unit: s)
                double blockCycle = (chainTimestamp - lastBlockTimestamp) / 1000.0;
                int blockSize = chainBlockStat.getTxCount();
                double tpsDouble = blockSize / blockCycle;
                // save
                this.saveStat(groupId, height, blockSize, (int) tpsDouble, blockCycle,
                    String.valueOf(chainTimestamp));
                // update last timestamp
                lastBlockTimestamp = chainTimestamp;
            }
            log.info("=== end monitor. groupId:{} allUseTime:{}", groupId,
                Duration.between(startTime, Instant.now()).toMillis());

        } catch (Exception ex) {
            log.error("fail transMonitorByGroupId, group:{}", groupId, ex);
        } finally {
            if (Objects.nonNull(latch)) {
                // finish one group, count down
                latch.countDown();
            }
        }

    }

    public void saveStat(String groupId, int blockNum, int blockSize, int tps, double blockCycle, String timestamp) {
        if (this.checkStatExist(groupId, blockNum)) {
            log.warn("saveStat skip for block num already exist!" +
                    " groupId:{},blockNum:{}", groupId, blockNum);
            return;
        }
        TbStat tbStat = new TbStat();
        tbStat.setGroupId(groupId);
        tbStat.setBlockNumber(blockNum);
        tbStat.setBlockSize(blockSize);
        tbStat.setTps(tps);
        tbStat.setBlockCycle(blockCycle);
        tbStat.setStatTimestamp(timestamp);
        Date now = new Date();
        tbStat.setCreateTime(now);
        tbStat.setModifyTime(now);
        log.debug("saveStat tbStat:{}", tbStat);
        tbStatMapper.insertSelective(tbStat);
    }


    public List<PerformanceData> findContrastDataByTime(String groupId, Long startTimestamp, Long endTimestamp,
        Long contrastStartTimestamp, Long contrastEndTimestamp, int gap) {

        List<TbStat> statList;
        if (startTimestamp == null || endTimestamp == null) {
            statList = new ArrayList<>();
        } else {
            statList = tbStatMapper.findByTimeBetween(groupId, startTimestamp.toString(),
                endTimestamp.toString());
        }
        List<TbStat> contrastMonitorList = new ArrayList<>();
        if (contrastStartTimestamp != null && contrastEndTimestamp != null) {
            contrastMonitorList = tbStatMapper.findByTimeBetween(groupId, contrastStartTimestamp.toString(),
                contrastEndTimestamp.toString());
        }
        return transferToPerformanceData(transferListByGap(statList, gap),
            transferListByGap(contrastMonitorList, gap));
    }
    
    private List<PerformanceData> transferToPerformanceData(List<TbStat> statList,
        List<TbStat> contrastMonitorList) {
        List<Long> timestampList = new ArrayList<>();
        List<BigDecimal> blockSizeValueList = new ArrayList<>();
        List<BigDecimal> blockCycleValueList = new ArrayList<>();
        List<BigDecimal> tpsValueList = new ArrayList<>();
        for (TbStat tbStat : statList) {
            blockSizeValueList.add(tbStat.getBlockSize() == null ? null
                : new BigDecimal(tbStat.getBlockSize()));
            blockCycleValueList.add(
                tbStat.getBlockCycle() == null ? null : new BigDecimal(tbStat.getBlockCycle()));
            tpsValueList.add(tbStat.getTps() == null ? null
                : new BigDecimal(tbStat.getTps()));
            timestampList.add(Long.valueOf(tbStat.getStatTimestamp()));
        }
        statList.clear();

        List<Long> contrastTimestampList = new ArrayList<>();
        List<BigDecimal> contrastBlockSizeValueList = new ArrayList<>();
        List<BigDecimal> contrastBlockCycleValueList = new ArrayList<>();
        List<BigDecimal> contrastTpsValueList = new ArrayList<>();
        for (TbStat tbStat : contrastMonitorList) {
            contrastBlockSizeValueList.add(tbStat.getBlockNumber() == null ? null
                : new BigDecimal(tbStat.getBlockNumber()));
            contrastBlockCycleValueList.add(
                tbStat.getBlockCycle() == null ? null : new BigDecimal(tbStat.getBlockCycle()));
            contrastTpsValueList.add(tbStat.getTps() == null ? null
                : new BigDecimal(tbStat.getTps()));
            contrastTimestampList.add(Long.valueOf(tbStat.getStatTimestamp()));
        }
        contrastMonitorList.clear();
        List<PerformanceData> performanceDataList = new ArrayList<>();
        performanceDataList.add(new PerformanceData("blockSize",
            new Data(new LineDataList(timestampList, blockSizeValueList),
                new LineDataList(contrastTimestampList, contrastBlockSizeValueList))));
        performanceDataList.add(
            new PerformanceData("blockCycle", new Data(new LineDataList(null, blockCycleValueList),
                new LineDataList(null, contrastBlockCycleValueList))));
        performanceDataList.add(new PerformanceData("tps",
            new Data(new LineDataList(null, tpsValueList),
                new LineDataList(null, contrastTpsValueList))));
        return performanceDataList;
    }

    public List transferListByGap(List arrayList, int gap) {
        if (gap == 0) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION.getCode(), "gap cannot be 0");
        }
        List newStatList = fillList(arrayList);
        List ilist = new ArrayList<>();
        int len = newStatList.size();
        for (int i = 0; i < len; i = i + gap) {
            ilist.add(newStatList.get(i));
        }
        return ilist;
    }

    private List<TbStat> fillList(List<TbStat> statList) {
        // result
        List<TbStat> newStatList = new ArrayList<>();
        for (int i = 0; i < statList.size() - 1; i++) {
            Long startTime = Long.parseLong(statList.get(i).getStatTimestamp());
            Long endTime = Long.parseLong(statList.get(i + 1).getStatTimestamp());
            if (endTime - startTime > 10000) {
                log.debug("====startTime:{}", startTime);
                log.debug("====endTime:{}", endTime);
                while (endTime - startTime > 5000) {
                    TbStat emptyMonitor = new TbStat();
                    emptyMonitor.setStatTimestamp(String.valueOf(startTime + 5000));
                    newStatList.add(emptyMonitor);
                    log.debug("====insert" + startTime);
                    startTime = startTime + 5000;
                }
            }
            else if (endTime - startTime < 5000) {
                // add the data after endTime until gap beyond 5s
                List<TbStat> stat2Sum = new ArrayList<>();
                do {
                    log.debug("==== index:{}", i);
                    stat2Sum.add(statList.get(i));
//                    targetStat = sumStatWithin5sec(statList.get(i), statList.get(i + 1));
//                    log.debug("==== new endTime:{}", targetStat.getStatTimestamp());
                    i++;
                } while (i < statList.size() - 2 && Long.parseLong(statList.get(i + 1).getStatTimestamp()) - startTime < 5000);
                log.debug("==== sumStatWithin5sec:{}", stat2Sum.size());
                TbStat targetStat = this.sumStatWithin5sec(stat2Sum, endTime);
                newStatList.add(targetStat);
            }
            else {
                // 5s < gap < 10s
                newStatList.add(statList.get(i));
            }
        }
        return newStatList;
    }

    /**
     * get average value of two
     * @param statSum later one to store all sum result
     * @param stat2Add
     * @return
     */
    private TbStat sumStatWithin5sec(TbStat statSum, TbStat stat2Add) {
        int tps = (statSum.getTps() + stat2Add.getTps()) / 2;
        int blockSize = (statSum.getBlockSize() + stat2Add.getBlockSize()) / 2;
        double blockCycle = (statSum.getBlockCycle() + stat2Add.getBlockCycle()) / 2;
        statSum.setTps(tps);
        statSum.setBlockSize(blockSize);
        statSum.setBlockCycle(blockCycle);
        return statSum;
    }

    private TbStat sumStatWithin5sec(List<TbStat> stat2Sum, Long endTime) {
        int size = stat2Sum.size();
        int tpsSum = 0;
        int blockSizeSum = 0;
        double blockCycleSum = 0.0;
        for (TbStat s : stat2Sum) {
            tpsSum += s.getTps();
            blockSizeSum += s.getBlockSize();
            blockCycleSum += s.getBlockCycle();
        }

        TbStat statSum = new TbStat();
        statSum.setStatTimestamp(String.valueOf(endTime));
        statSum.setTps(tpsSum / size);
        statSum.setBlockSize(blockSizeSum / size);
        statSum.setBlockCycle(blockCycleSum / size);
        return statSum;
    }


    /**
     * remove block stat info.
     */
    public Integer remove(String groupId, BigInteger blockRetainMax) {
        log.info("remove groupId:{}, blockRetainMax:{}", groupId, blockRetainMax);
        Integer affectRow = tbStatMapper.remove(groupId, blockRetainMax);
        log.info("remove affectRow{}", affectRow);
        return affectRow;
    }

    public void deleteByGroupId(String groupId) {
        int affected = tbStatMapper.deleteByGroupId(groupId);
        log.warn("deleteByGroupId:{} affected:{}", groupId, affected);
    }

    public boolean checkStatExist(String groupId, int blockNum) {
        TbStat maxStat = tbStatMapper.findByGroupAndBlockNum(groupId, blockNum);
        return maxStat != null;
    }

    static class ChainStatInfo {
        long nodeCount = 0;
        long contractCount = 0;
        long transCount = 0;
    }

    public ChainStat getChainStat() {
        ChainStat chainStat = new ChainStat();

        // 加密类型
        int encrypt = cryptoSuite.cryptoTypeConfig;

        // 链版本信息
        List<TbConfig> chainConfigs = configService.selectConfigList(false, 1);

        chainStat.setEncryptType(encrypt);
        if (chainConfigs.size() > 0) {
            chainStat.setVersion(chainConfigs.get(0).getConfigValue());
        } else {
            chainStat.setVersion("3.4.0");
        }

        // 群组和节点信息
        // get group list include invalid status
        long count = groupService.countOfGroup(null, null);
        ChainStatInfo chainStatInfoRet = new ChainStatInfo();
        if (count > 0) {
            chainStatInfoRet = DataPermissionHelper.ignore(() -> {
                List<TbGroup> groupList = groupService.getGroupList(null);
                ChainStatInfo chainStatInfo = new ChainStatInfo();
                if (groupList != null && !groupList.isEmpty()) {
                    for (TbGroup group : groupList) {
                        try {
                            chainStatInfo.nodeCount += group.getNodeCount();
                            GroupGeneral groupGeneral = groupService.getGeneralAndUpdateNodeCount(group.getGroupId());
                            chainStatInfo.contractCount += groupGeneral.getContractCount();

                            TotalTransactionCount.TransactionCountInfo transCountInfo = frontInterface.getTotalTransactionCount(group.getGroupId());
                            if (transCountInfo != null) {
                                chainStatInfo.transCount += Long.parseLong(transCountInfo.getTransactionCount());
                            }
                        } catch (Exception e) {
                            log.warn("getChainStat, err: {}, continue next group", e.getMessage());
                        }
                    }
                }

                return chainStatInfo;
            });
        }

        chainStat.setGroupCount(count);
        chainStat.setNodeCount(chainStatInfoRet.nodeCount);
        chainStat.setContractCount(chainStatInfoRet.contractCount);
        chainStat.setTransCount(chainStatInfoRet.transCount);

        return chainStat;
    }
}
