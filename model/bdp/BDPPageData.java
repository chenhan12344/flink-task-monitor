package com.demo.model.bdp;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:00
 */
@Data
public class BDPPageData implements Serializable {
    private static final long serialVersionUID = 7945420739578035938L;
    private Integer firstIndex;
    private Boolean hasPrePage;
    private Integer offset;
    private Boolean hasNextPage;
    private Integer prePage;
    private Integer totalPage;
    private Integer nextPage;
    private Integer pageSize;
    private Integer lastIndex;
    private Integer totalCount;
    private Integer pageNum;
    private Object data;
}
