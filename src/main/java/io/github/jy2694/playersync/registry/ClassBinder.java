package io.github.jy2694.playersync.registry;

import java.io.NotSerializableException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.jy2694.playersync.annotations.SyncID;
import io.github.jy2694.playersync.annotations.Synchronizable;
import io.github.jy2694.playersync.exception.NotSynchronizableClassException;

public class ClassBinder {
    private List<Class<?>> classList = new ArrayList<>();

    public void bindClass(Class<?> clazz) throws NotSynchronizableClassException, NotSerializableException {
        if(!clazz.isAnnotationPresent(Synchronizable.class)) {
            throw new NotSynchronizableClassException("Class " + clazz.getName() + " is not annotated with @Synchronizable");
        }
        boolean hasSyncID = false;
        for(Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(SyncID.class) && field.getType().equals(UUID.class)) {
                hasSyncID = true;
                break;
            }
        }
        if(!hasSyncID) {
            throw new NotSynchronizableClassException("Class " + clazz.getName() + " does not have a field annotated with @SyncID of type UUID");
        }
        classList.add(clazz);
    }

    public void unbindClass(Class<?> clazz) {
        classList.remove(clazz);
    }

    public List<Class<?>> getClassList() {
        return new ArrayList<>(classList);
    }

    public UUID findKeyFromObject(Object object) throws IllegalArgumentException, IllegalAccessException{
        for(Class<?> clazz : classList){
            for(Field field : clazz.getDeclaredFields()){
                if(field.isAnnotationPresent(SyncID.class) && field.getType().equals(UUID.class)){
                    return (UUID) field.get(object);
                }
            }
        }
        return null;
    }
}
