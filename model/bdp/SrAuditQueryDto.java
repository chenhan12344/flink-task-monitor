package com.demo.model.bdp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @Description SR审计日志查询参数
 * @Author 
 * @Date 2024/11/12 19:21
 */
@Data
public class SrAuditQueryDto implements Serializable {

    private static final long serialVersionUID = -5907313799366949253L;
    private final String queryJsonStr = "{\"refId\":\"cluster\",\"datasource\":{\"type\":\"mysql\",\"uid\":\"gzQCYxcSz\"},\"rawSql\":\"SELECT TABLE_NAME FROM information_schema.tables WHERE TABLE_SCHEMA='starrocks_audit_db';\",\"format\":\"table\"}";

    private String from = System.currentTimeMillis() - 5 * 60 * 1000 + "";
    private String to = System.currentTimeMillis() + "";
    private List<JSONObject> queries = Collections.singletonList(JSON.parseObject(queryJsonStr, JSONObject.class));

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}