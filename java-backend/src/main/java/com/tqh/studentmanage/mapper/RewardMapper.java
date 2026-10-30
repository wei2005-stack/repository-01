package com.tqh.studentmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tqh.studentmanage.entity.RewardPunishment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RewardMapper extends BaseMapper<RewardPunishment> {

    List<RewardPunishment> selectRewardPage(@Param("keyword") String keyword,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    long countRewards(@Param("keyword") String keyword);

    RewardPunishment selectRewardDetail(@Param("rpId") Integer rpId);
}
