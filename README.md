# Filesystem Event Aggregator

[![Build Status](https://travis-ci.org/p2p-sync/aggregator.svg?branch=master)](https://travis-ci.org/p2p-sync/aggregator)
[![Coverage Status](https://coveralls.io/repos/p2p-sync/aggregator/badge.svg?branch=master&service=github)](https://coveralls.io/github/p2p-sync/aggregator?branch=master)

# Install
Use Maven to add this component as your dependency:

```xml

<repositories>
  <repository>
    <id>aggregator-mvn-repo</id>
    <url>https://raw.github.com/p2p-sync/aggregator/mvn-repo/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
      <groupId>org.rmatil.sync.event.aggregator</groupId>
      <artifactId>sync-event-aggregator</artifactId>
      <version>0.1-SNAPSHOT</version>
  </dependency>
</dependencies>

```

# Architectural Overview

[![Architectural Overview](https://cdn.rawgit.com/p2p-sync/aggregator/master/src/main/resources/img/architectural-overview.svg)](https://cdn.rawgit.com/p2p-sync/aggregator/master/src/main/resources/img/architectural-overview.svg)

This component relies on the functionality of [Daniel Mittendorfer's Perlock](https://github.com/danielmitterdorfer/perlock) to get notified about changes in a directory. These events are then processed by the [EventAggregator](https://github.com/p2p-sync/aggregator/blob/master/src/main/java/org/rmatil/sync/event/aggregator/api/IEventAggregator.java). Any registered event listener on the EventAggregator will finally receive all events still remaining after processing. 

## Modifier
Modifiers are responsible to reduce the amount of work which has to be made by removing events which are not needed.

### IgnoreDirectoryModifier
This modifier discards all received events which notify about the change of a watched directory. Since each change is either
an add or a delete event of a path element inside it (i.e. another directory or a file), we still get notified about
the change in the watched folder.

### IgnorePathsModifier
To avoid filesystem notifications about certain paths in the watched directory, one can specify to ignore all events
created for a particular element by using this modifier. Two ways are supported to ignore paths: Either by specifying
an actual path which should be ignored or by defining glob patterns as documented [here](https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-)

### RelativePathsModifier
To receive all path values, contained in the aggregated events, relative to the watched folder, one can use this modifier. On instantiation, the 
folder to which all event-paths are relativized must be provided.

### IgnoreSameHashModifier
This modifier filters all `ModifyEvents` having a value for the hash which is already known by the `ObjectStore`.

## Aggregator
Aggregators are responsible to aggregate a bunch of filesystem events into one or multiple other events.

### HistoryMoveAggregator
The HistoryMoveAggregator tries to detect a move of a file or directory by the combination of a delete and create event.
If a well-defined hash of the file contents are equal, then one can assume, that the remove and the newly created file are identical. However, since the computation of a hash over contents of a deleted file is not possible, this aggregator relies
on a local history of file contents. For more information about versioning of files, see [P2P-Sync Versions](https://github.com/p2p-sync/versions).

# Usage

```java
import org.rmatil.sync.event.aggregator.api.IEventAggregator;
import org.rmatil.sync.event.aggregator.api.IEventListener;
import org.rmatil.sync.event.aggregator.core.EventAggregator;
import org.rmatil.sync.event.aggregator.core.aggregator.HistoryMoveAggregator;
import org.rmatil.sync.event.aggregator.core.aggregator.IAggregator;
import org.rmatil.sync.event.aggregator.core.modifier.*;
import org.rmatil.sync.event.aggregator.core.pathwatcher.PerlockPathWatcherFactory;
import org.rmatil.sync.persistence.core.tree.local.LocalStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.rmatil.sync.version.api.IObjectStore;
import org.rmatil.sync.version.core.ObjectStore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// ...


    // Specify the path to the folder which should be watched
    Path rootPath = Paths.get("path/to/root/watched/folder");
    // Specify the path to the object store folder
    Path osPath = rootPath.resolve(".sync");

    // ignore .sync directory
    List<Path>ignoredPaths = new ArrayList<>();
    ignoredPaths.add(Paths.get(".sync"));

    // ignore all .DS_Store files
    List<String>ignoredPatterns = new ArrayList<>();
    ignoredPatterns.add("**/*.DS_Store");
    ignoredPatterns.add(".sync/*");

    // create a new ObjectStore
    IObjectStore objectStore = new ObjectStore(
      new LocalStorageAdapter(rootPath),
      "index.json",
      "object",
      new LocalStorageAdapter(osPath)
    );


    // all file events will contain a path resolved to the rootPath defined above,
    // e.g. path/to/root/watched/folder/someFile.txt will be relativized to someFile.txt
    IModifier relativePathModifier = new RelativePathModifier(rootPath);

    // if a directory is modified, i.e. an element contained is added / removed
    // we like to have the event for that element too, not only the modify event of the directory
    IModifier addDirectoryContentModifier = new AddDirectoryContentModifier(rootPath,objectStore);

    // Ignore the specified paths, i.e. all events matching any of the paths resp. patterns
    // will be discarded
    IModifier ignorePathsModifier = new IgnorePathsModifier(ignoredPaths,ignoredPatterns);

    // Ignore modify events of directories
    IModifier ignoreDirectoryModifier = new IgnoreDirectoryModifier(rootPath);

    // Ignore all modify events which contain a hash already known
    IModifier sameHashModifier = new IgnoreSameHashModifier(objectStore.getObjectManager());

    // Aggregate delete & create events to a move event
    IAggregator historyMoveAggregator = new HistoryMoveAggregator(objectStore.getObjectManager());

    IEventAggregator eventAggregator = new EventAggregator(rootPath,new PerlockPathWatcherFactory());
    eventAggregator.setAggregationInterval(5000L); // aggregate events every 5 seconds

    // register a new event listener
    IEventListener eventListener = ...;
    eventAggregator.addListener(eventListener);

    // add modifiers
    eventAggregator.addModifier(relativePathModifier);
    eventAggregator.addModifier(addDirectoryContentModifier);
    eventAggregator.addModifier(ignoreDirectoryModifier);
    eventAggregator.addModifier(ignorePathsModifier);
    eventAggregator.addModifier(sameHashModifier);

    // add aggregator
    eventAggregator.addAggregator(historyMoveAggregator);
    
    
// ...


```

# License
```
   Copyright 2015 rmatil

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
