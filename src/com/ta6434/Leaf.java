package com.ta6434;

/**
 * Leaf node to store symbol value
 */

class Leaf extends Node {

    public final int symbol;

    public Leaf(int symbol) {
        if (symbol < 0)
            throw new IllegalArgumentException("Illegal symbol value");
        this.symbol = symbol;
    }

}
