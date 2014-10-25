package com.ta6434;

/**
 * Internal node in code tree
 */

class InternalNode extends Node {

    public final Node leftChild;
    public final Node rightChild;

    public InternalNode(Node leftChild, Node rightChild) {

        if (leftChild == null || rightChild == null)
            throw new NullPointerException("Argument is null");

        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

}
