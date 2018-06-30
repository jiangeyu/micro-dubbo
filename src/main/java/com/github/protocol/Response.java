package com.github.protocol;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:14 2018/6/27
 * @desc
 */
@Builder
@Data
@NoArgsConstructor
public class Response {

    /**
     * 全局唯一id标志一次请求
     */
    private Long requestId;

    private String error;

    private Object result;
}
