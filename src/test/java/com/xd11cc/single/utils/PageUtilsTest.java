package com.xd11cc.single.utils;

import com.xd11cc.single.entity.base.BasePageVO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageUtilsTest {

    @Test
    void page_列表为空_返回空分页结果() {
        BasePageVO pageVO = new BasePageVO();
        pageVO.setCurrentPage(1);
        pageVO.setPageSize(10);

        com.xd11cc.single.entity.base.ResponseVO<com.xd11cc.single.entity.base.PageResult<String>> result =
                PageUtils.page(pageVO, Collections::emptyList);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).isEmpty();
        assertThat(result.getData().getTotal()).isZero();
    }

    @Test
    void page_有数据_返回正确分页结果() {
        BasePageVO pageVO = new BasePageVO();
        pageVO.setCurrentPage(1);
        pageVO.setPageSize(10);

        List<String> data = Arrays.asList("a", "b", "c");

        com.xd11cc.single.entity.base.ResponseVO<com.xd11cc.single.entity.base.PageResult<String>> result =
                PageUtils.page(pageVO, () -> data);

        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(3);
        assertThat(result.getData().getRows()).containsExactly("a", "b", "c");
        assertThat(result.getData().getTotal()).isEqualTo(3);
    }

    @Test
    void page_带orderBy_正常执行() {
        BasePageVO pageVO = new BasePageVO();
        pageVO.setCurrentPage(1);
        pageVO.setPageSize(5);
        pageVO.setOrderBy("id DESC");

        com.xd11cc.single.entity.base.ResponseVO<com.xd11cc.single.entity.base.PageResult<String>> result =
                PageUtils.page(pageVO, () -> Collections.singletonList("x"));

        assertThat(result.getData().getRows()).hasSize(1);
        assertThat(result.getData().getTotal()).isEqualTo(1);
    }

    @Test
    void empty_返回空分页结果() {
        com.xd11cc.single.entity.base.ResponseVO<com.xd11cc.single.entity.base.PageResult<String>> result =
                PageUtils.empty();

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).isEmpty();
        assertThat(result.getData().getTotal()).isZero();
    }
}
