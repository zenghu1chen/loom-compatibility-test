package org.example.commonspool2;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zenghuichen
 * @date 2023/7/13 19:10
 */
public class SimplePooledObjectFactory implements PooledObjectFactory<Object> {

    @Override
    public PooledObject<Object> makeObject() throws Exception {
        return new DefaultPooledObject<>(new Object());
    }

    @Override
    public void destroyObject(PooledObject<Object> p) throws Exception {
        // Simulate a 100ms I/O operation
        Thread.sleep(100L);
    }

    @Override
    public boolean validateObject(PooledObject<Object> p) {
        return true;
    }

    @Override
    public void activateObject(PooledObject<Object> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<Object> p) throws Exception {

    }
}
