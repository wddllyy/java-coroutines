package com.github.natanbc.coroutine.impl;

import com.github.natanbc.coroutine.AsymmetricCoroutineContext;
import com.github.natanbc.coroutine.SymmetricCoroutineContext;

public class Contexts {
    static final ThreadLocal<AsymmetricCoroutineContext> asymmetric = new ThreadLocal<AsymmetricCoroutineContext>() {
        @Override
        protected AsymmetricCoroutineContext initialValue() {
            return new AsymmetricContextImpl();
        }
    };

    static final ThreadLocal<SymmetricCoroutineContext> symmetric = new ThreadLocal<SymmetricCoroutineContext>() {
        @Override
        protected SymmetricCoroutineContext initialValue() {
            return new SymmetricContextImpl();
        }
    };

    public static AsymmetricCoroutineContext getAsymmetric() {
        return asymmetric.get();
    }

    public static void destroyAsymmetric() {
        asymmetric.get().destroy();
    }

    public static SymmetricCoroutineContext getSymmetric() {
        return symmetric.get();
    }

    public static void destroySymmetric() {
        symmetric.get().destroy();
    }
}
