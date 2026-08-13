package com.xd11cc.single.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class TreeUtilsTest {

    static class TreeNode {
        Long id;
        Long parentId;
        Integer orderNum;
        List<TreeNode> children = new ArrayList<>();

        TreeNode(Long id, Long parentId) {
            this(id, parentId, 0);
        }

        TreeNode(Long id, Long parentId, Integer orderNum) {
            this.id = id;
            this.parentId = parentId;
            this.orderNum = orderNum;
        }

        void setChildren(List<TreeNode> children) {
            this.children = children;
        }

        Long getId() {
            return id;
        }

        Long getParentId() {
            return parentId;
        }

        Integer getOrderNum() {
            return orderNum;
        }
    }

    private static final BiConsumer<TreeNode, List<TreeNode>> SET_CHILDREN = TreeNode::setChildren;

    @Test
    void buildTree_空列表_返回空列表() {
        List<TreeNode> result = TreeUtils.buildTree(
                Collections.emptyList(),
                TreeNode::getId,
                TreeNode::getParentId,
                SET_CHILDREN,
                TreeNode::getOrderNum,
                0L
        );
        assertThat(result).isEmpty();
    }

    @Test
    void buildTree_单节点根节点_返回单节点且无子节点() {
        TreeNode root = new TreeNode(1L, 0L, 1);
        List<TreeNode> all = Collections.singletonList(root);

        List<TreeNode> result = TreeUtils.buildTree(
                all, TreeNode::getId, TreeNode::getParentId, SET_CHILDREN,
                TreeNode::getOrderNum, 0L
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id).isEqualTo(1L);
        assertThat(result.get(0).children).isEmpty();
    }

    @Test
    void buildTree_多级父子结构_正确组装树() {
        // 结构: root(1) -> children(2,3) -> grandchild(4 under 2)
        TreeNode n4 = new TreeNode(4L, 2L);
        TreeNode n2 = new TreeNode(2L, 1L, 1);
        TreeNode n3 = new TreeNode(3L, 1L, 2);
        TreeNode n1 = new TreeNode(1L, 0L);
        List<TreeNode> all = new ArrayList<>();
        // 乱序传入
        all.add(n3);
        all.add(n4);
        all.add(n1);
        all.add(n2);

        List<TreeNode> result = TreeUtils.buildTree(
                all, TreeNode::getId, TreeNode::getParentId, SET_CHILDREN,
                TreeNode::getOrderNum, 0L
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id).isEqualTo(1L);
        assertThat(result.get(0).children).hasSize(2);
        // orderNum 排序：2 在 3 前
        assertThat(result.get(0).children.get(0).id).isEqualTo(2L);
        assertThat(result.get(0).children.get(1).id).isEqualTo(3L);
        assertThat(result.get(0).children.get(0).children).hasSize(1);
        assertThat(result.get(0).children.get(0).children.get(0).id).isEqualTo(4L);
    }

    @Test
    void buildTree_根节点parentId为null_当作根节点处理() {
        TreeNode n1 = new TreeNode(1L, null);
        TreeNode n2 = new TreeNode(2L, 1L);
        List<TreeNode> all = new ArrayList<>();
        all.add(n2);
        all.add(n1);

        List<TreeNode> result = TreeUtils.buildTree(
                all, TreeNode::getId, TreeNode::getParentId, SET_CHILDREN,
                null, null
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id).isEqualTo(1L);
    }

    @Test
    void buildTree_多个顶级根节点_全部返回() {
        TreeNode root1 = new TreeNode(1L, 0L, 1);
        TreeNode root2 = new TreeNode(2L, 0L, 2);
        List<TreeNode> all = new ArrayList<>();
        all.add(root2);
        all.add(root1);

        List<TreeNode> result = TreeUtils.buildTree(
                all, TreeNode::getId, TreeNode::getParentId, SET_CHILDREN,
                TreeNode::getOrderNum, 0L
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id).isEqualTo(1L);
        assertThat(result.get(1).id).isEqualTo(2L);
    }

    @Test
    void buildTree_不传排序函数_不报错() {
        TreeNode root = new TreeNode(1L, 0L);
        List<TreeNode> result = TreeUtils.buildTree(
                Collections.singletonList(root),
                TreeNode::getId,
                TreeNode::getParentId,
                SET_CHILDREN,
                null,
                0L
        );
        assertThat(result).hasSize(1);
    }
}
