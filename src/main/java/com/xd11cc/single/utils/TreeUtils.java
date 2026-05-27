package com.xd11cc.single.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
public class TreeUtils {

    /**
     * 构建树形结构
     * @param allNodes 所有节点的平铺列表
     * @param getId 获取节点id
     * @param getParentId 获取节点父id
     * @param setChildren 设置子节点列表
     * @param getSort 获取排序字段（可为null，不排序）
     * @param rootParentId 根节点的parentId
     * @param <T> 节点类型
     * @return 树形结构列表
     */
    public static <T> List<T> buildTree(List<T> allNodes,
                                        Function<T, Long> getId,
                                        Function<T, Long> getParentId,
                                        BiConsumer<T, List<T>> setChildren,
                                        Function<T, Integer> getSort,
                                        Long rootParentId) {
        List<T> tree = new ArrayList<>();
        for (T node : allNodes) {
            if (Objects.equals(rootParentId, getParentId.apply(node))) {
                List<T> children = buildTree(allNodes, getId, getParentId, setChildren, getSort, getId.apply(node));
                setChildren.accept(node, children);
                tree.add(node);
            }
        }
        if (getSort != null) {
            tree.sort(Comparator.comparing(n -> getSort.apply(n) == null ? 0 : getSort.apply(n)));
        }
        return tree;
    }
}
