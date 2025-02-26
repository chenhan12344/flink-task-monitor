package com.demo.utils;

import com.alibaba.fastjson.JSONObject;
import com.demo.constants.BDPApis;
import com.demo.model.bdp.BDPParamDto;
import com.demo.model.bdp.BDPResultVo;
import com.demo.model.bdp.BDPTaskResponseDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Description BDP接口
 * @Author
 * @Date 2023/10/24 09:59
 */
@Slf4j
public class BDPUtil {

    public static List<BDPTaskResponseDto> getTaskList(BDPParamDto paramDto, String cookie) {
        BDPResultVo resultVo = HttpUtilNew.doPost(BDPApis.TASK_LIST_API, paramDto.toString(), cookie, BDPResultVo.class);
        if (resultVo == null || resultVo.getOk() != 1) {
            throw new RuntimeException("[健康巡检] Flink列表获取失败, 请检查Cookie是否过期");
        }

        return JSONObject.parseArray(JSONObject.toJSONString(resultVo.getData().getData()), BDPTaskResponseDto.class);
    }
}
