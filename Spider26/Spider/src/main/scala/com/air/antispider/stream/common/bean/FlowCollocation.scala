package com.air.antispider.stream.common.bean

/**
 * 流程类：对应表nh_process_info
 * （规则配置集合和阈值参数）
 */
case class FlowCollocation(
                            flowId: String, //流程ID
                            flowName: String, //流程名字
                            rules: List[RuleCollocation], //流程对应的规则(多个规则)
                            flowLimitScore: Double = 100, //流程的阈值
                            strategyCode:String //流程ID  在数据库中流程的ID和策略的ID是一样的
                          )
