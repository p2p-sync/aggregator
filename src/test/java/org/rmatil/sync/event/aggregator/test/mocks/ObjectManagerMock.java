package org.rmatil.sync.event.aggregator.test.mocks;

import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.event.aggregator.config.Config;
import org.rmatil.sync.persistence.api.IPathElement;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.version.api.IObjectManager;
import org.rmatil.sync.version.core.model.Index;
import org.rmatil.sync.version.core.model.PathObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mocks an object manager
 *
 * @see IObjectManager
 */
public class ObjectManagerMock implements IObjectManager {

    Index index;

    Map<String, PathObject> pathObjects;

    public ObjectManagerMock() {
        this.index = new Index(new HashMap<>(), new HashMap<>());
        this.pathObjects = new HashMap<>();
    }

    @Override
    public void clear()
            throws InputOutputException {
        this.pathObjects = new HashMap<>();
        this.index = new Index(new HashMap<>(), new HashMap<>());
    }

    @Override
    public void writeObject(PathObject path)
            throws InputOutputException {
        this.pathObjects.put(Hash.hash(Config.DEFAULT.getHashingAlgorithm(), path.getAbsolutePath()), path);
    }

    @Override
    public PathObject getObject(String fileNameHash)
            throws InputOutputException {
        return this.pathObjects.get(fileNameHash);
    }

    @Override
    public PathObject getObjectForPath(String s)
            throws InputOutputException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void removeObject(String fileNameHash)
            throws InputOutputException {
        this.pathObjects.remove(fileNameHash);
    }

    @Override
    public List<PathObject> getChildren(String s)
            throws InputOutputException {
        List<PathObject> pathObjects = new ArrayList<>();
        for (Map.Entry<String, PathObject> entry : this.pathObjects.entrySet()) {
            if (entry.getKey().startsWith(s + "/")) {
                pathObjects.add(entry.getValue());
            }
        }

        return pathObjects;
    }

    @Override
    public IPathElement getObjectDir() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public IStorageAdapter getStorageAdapater() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Index getIndex() {
        return this.index;
    }

    @Override
    public String getIndexFileName() {
        return "index.json";
    }
}
