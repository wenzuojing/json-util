package org.wzj.json;

import java.util.EmptyStackException;

/**
 * Created by wens on 15-5-6.
 */
public class Stack<E> extends java.util.Stack<E> {

    @Override
    public synchronized E pop() {

        try {
            return super.pop();
        } catch (EmptyStackException e) {
            return null;
        }
    }
}
