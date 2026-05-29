package com.xd11cc.single.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-26
 */
public class TreeUtils {

    /**
     * 构建树形结构（O(n) HashMap 索引实现）
     */
    public static <T> List<T> buildTree(List<T> allNodes,
                                        Function<T, Long> getId,
                                        Function<T, Long> getParentId,
                                        BiConsumer<T, List<T>> setChildren,
                                        Function<T, Integer> getSort,
                                        Long rootParentId) {
        if (allNodes == null || allNodes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<T>> parentIdMap = allNodes.stream()
                .collect(Collectors.groupingBy(
                        node -> getParentId.apply(node) == null ? -1L : getParentId.apply(node)
                ));

        for (T node : allNodes) {
            List<T> children = parentIdMap.getOrDefault(getId.apply(node), Collections.emptyList());
            if (getSort != null && !children.isEmpty()) {
                children.sort(Comparator.comparingInt(n -> getSort.apply(n) == null ? 0 : getSort.apply(n)));
            }
            setChildren.accept(node, children);
        }

        Long key = rootParentId == null ? -1L : rootParentId;
        List<T> roots = parentIdMap.getOrDefault(key, Collections.emptyList());
        if (getSort != null && !roots.isEmpty()) {
            roots.sort(Comparator.comparingInt(n -> getSort.apply(n) == null ? 0 : getSort.apply(n)));
        }
        return roots;
    }
}
