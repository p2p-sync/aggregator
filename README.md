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

[![Architectural Overview](https://github.com/p2p-sync/aggregator/blob/master/src/main/resources/img/architectural-overview.svg)](https://github.com/p2p-sync/aggregator/blob/master/src/main/resources/img/architectural-overview.svg)

This component relies on the functionality of [Daniel Mittendorfer's Perlock](https://github.com/danielmitterdorfer/perlock) to get notified about changes in a directory. These events are then processed by the [EventAggregator](https://github.com/p2p-sync/aggregator/blob/master/src/main/java/org/rmatil/sync/event/aggregator/api/IEventAggregator.java). Any registered event listener on the EventAggregator will finally receive all events still remaining after processing. 

## Modifier
Modifiers are responsible to reduce the amount of work which has to be made by removing events which are not needed.

### IgnoreDirectoryModifier
This modifier discards all received events which notify about the change of a watched directory. Since each change is either
an add or a delete event of a path element inside it (i.e. another directory or a file), we still get notified about
the change in the watched folder.

### IgnorePathsModifier
To avoid filesystem notifications about certain paths in the watched directory, one can specify to ignore all events
created for the particular path element by using this modifier.

### RelativePathsModifier
To get all paths contained in the events relative to the watched folder, one can use this modifier. On instantiation, the 
folder to which all event-paths are relativized must be provided.

## Aggregator
Aggregators are responsible to aggregate a bunch of filesystem events into one or multiple other events.

### HistoryMoveAggregator
The HistoryMoveAggregator tries to detect a move of a file or directory by the combination of a delete and create event.
If a well-defined hash of the file contents are equal, then one can assume, that the remove and the newly created file are identical. However, since the computation of a hash over contents of a deleted file is not possible, this aggregator relies
on a local history of file contents. For more information about versioning of files, see [P2P-Sync Versions](https://github.com/p2p-sync/versions).
