/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


/**
 * @author guifei.qin
 * @Classname DegradeRuleZookeeperPublisher
 * @Description 降级规则push
 * @Date 2019-07-08
 * @Version V1.0
 */
@Component("degradeRuleZookeeperPublisher")
public class DegradeRuleZookeeperPublisher implements DynamicRulePublisher<List<DegradeRuleEntity>> {

    @Autowired
    private CuratorFramework zkClient;
    @Autowired
    private Converter<List<DegradeRuleEntity>, String> converter;
    @Autowired
    private ZookeeperSentinelConfig zkConfig;

    @Override
    public void publish(String app, List<DegradeRuleEntity> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");

        String path = zkConfig.getDegradeRulePath(app);
        Stat stat = zkClient.checkExists()
                            .forPath(path);
        if (stat == null) {
            zkClient.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path, null);
        }
        byte[] data = CollectionUtils.isEmpty(rules) ? "[]".getBytes() : converter.convert(rules)
                                                                                  .getBytes();
        zkClient.setData()
                .forPath(path, data);
    }
}