package nl.adamg.baizel.internal.common.io;

import javax.annotation.CheckForNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/// Immutable file index optimized for compact memory representation and fast lookup.
public class FileIndex {
    public static class Entity implements Serializable {
        /**
         * sorted all the unique file and directory names
         */
        public String[] stringPool;
        /**
         * offset into {@link #childNodeIndices}
         */
        public int[] nodeFirstChildIndex;
        /**
         * number of children for each node
         */
        public int[] nodeChildCount;
        /**
         * pointers to {@link #stringPool}
         */
        public int[] childNameIds;
        /**
         * flat node index for each child
         */
        public int[] childNodeIndices;

        //region generated code
        private Entity(String[] stringPool, int[] nodeFirstChildIndex, int[] nodeChildCount, int[] childNameIds, int[] childNodeIndices) {
            this.stringPool = stringPool;
            this.nodeFirstChildIndex = nodeFirstChildIndex;
            this.nodeChildCount = nodeChildCount;
            this.childNameIds = childNameIds;
            this.childNodeIndices = childNodeIndices;
        }
        //endregion
    }

    public static class Path implements Serializable {
        /**
         * pointers to {@link FileIndex.Entity#stringPool}
         */
        private final int[] nameIds;
        private final FileIndex index;

        private Path(int[] nameIds, FileIndex index) {
            this.nameIds = nameIds;
            this.index = index;
        }

        public java.nio.file.Path toPath() {
            return java.nio.file.Path.of(toString());
        }

        public List<Path> children() {
            return index.children(nameIds);
        }

        public List<Path> descendants() {
            return index.descendants(this);
        }

        @CheckForNull
        public Path parent() {
            return nameIds.length == 0 ? null : new Path(Arrays.copyOf(nameIds, nameIds.length - 1), index);
        }

        public List<Path> ancestors() {
            var result = new ArrayList<Path>();
            var current = this;
            while ((current = current.parent()) != null) {
                result.addFirst(current);
            }
            return result;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new Object[]{nameIds, System.identityHashCode(index)});
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Path other && other.index == this.index && Arrays.equals(nameIds, other.nameIds);
        }

        @Override
        public String toString() {
            return Arrays.stream(nameIds).boxed().map(i -> index.entity.stringPool[i]).collect(Collectors.joining("/"));
        }
    }

    private static class MutableTrie {
        Map<Integer, MutableTrie> children = new TreeMap<>();
    }

    private final Entity entity;

    public static FileIndex from(Collection<String> filePaths) {
        filePaths = new TreeSet<>(filePaths); // we force that it's sorted and unique!
        var stringPool = filePaths.stream().flatMap(p -> Arrays.stream(p.split("/"))).
                filter(s -> !s.isEmpty()).distinct().sorted().toArray(String[]::new);
        var nameMap = IntStream.range(0, stringPool.length).boxed().
                collect(Collectors.toMap(i -> stringPool[i], i -> i));
        var trieBuilder = new MutableTrie();
        for (var path : filePaths) {
            var components = path.split("/");
            var node = trieBuilder;

            for (var component : components) {
                if (component.isEmpty()) continue;
                var nameId = nameMap.get(component);
                node = node.children.computeIfAbsent(nameId, k -> new MutableTrie());
            }
        }
        return new FileIndex(flattenTrie(trieBuilder, stringPool));
    }

    @CheckForNull
    public Path find(String path) {
        if (path.isEmpty()) {
            return new Path(new int[0], this);
        }
        var nameIds = new ArrayList<Integer>();
        for (var component : path.split("/")) {
            if (component.isEmpty()) {
                continue;
            }
            var nameId = Arrays.binarySearch(entity.stringPool, component);
            if (nameId < 0) {
                return null;
            }
            nameIds.add(nameId);
        }
        var nodeIndex = 0;
        for (var nameId : nameIds) {
            // If node has no children, path doesn't exist
            if (entity.nodeChildCount[nodeIndex] < 1) {
                return null;
            }
            var firstChildIndex = entity.nodeFirstChildIndex[nodeIndex];
            var childCount = entity.nodeChildCount[nodeIndex];
            var foundIndex = Arrays.binarySearch(entity.childNameIds, firstChildIndex, firstChildIndex + childCount, nameId);
            if (foundIndex < 0) {
                return null;
            }
            nodeIndex = entity.childNodeIndices[foundIndex];
        }
        return new Path(nameIds.stream().mapToInt(i -> i).toArray(), this);
    }

    //region internal utils
    private static Entity flattenTrie(MutableTrie trieRoot, String[] stringPool) {
        var trieNodeIdToIndexMap = new HashMap<MutableTrie, Integer>();
        var queue = (Queue<MutableTrie>) new LinkedList<MutableTrie>();
        trieNodeIdToIndexMap.put(trieRoot, 0);
        queue.add(trieRoot);

        for (var nextTrieNodeId = 1; !queue.isEmpty(); )
            for (var childTrieNode : queue.poll().children.values())
                if (!trieNodeIdToIndexMap.containsKey(childTrieNode)) {
                    trieNodeIdToIndexMap.put(childTrieNode, nextTrieNodeId++);
                    queue.add(childTrieNode);
                }

        var totalTrieNodesCount = trieNodeIdToIndexMap.size();
        var nodeFirstChildIndex = new int[totalTrieNodesCount];
        var nodeChildCount = new int[totalTrieNodesCount];
        var childNameIds = new ArrayList<Integer>();
        var childNodeIndices = new ArrayList<Integer>();
        var trieNodesById = new MutableTrie[totalTrieNodesCount];
        trieNodeIdToIndexMap.forEach((trieNode, i) -> trieNodesById[i] = trieNode);

        for (int trieNodeIndex = 0, childIndex = 0; trieNodeIndex < totalTrieNodesCount; trieNodeIndex++) {
            var children = new ArrayList<>(trieNodesById[trieNodeIndex].children.entrySet());
            nodeFirstChildIndex[trieNodeIndex] = childIndex;
            nodeChildCount[trieNodeIndex] = children.size();
            for (var entry : children) {
                childNameIds.add(entry.getKey());
                childNodeIndices.add(trieNodeIdToIndexMap.get(entry.getValue()));
            }
            childIndex += children.size();
        }

        return new Entity(
                stringPool,
                nodeFirstChildIndex,
                nodeChildCount,
                childNameIds.stream().mapToInt(i -> i).toArray(),
                childNodeIndices.stream().mapToInt(i -> i).toArray()
        );
    }

    private List<Path> children(int[] pathNameIds) {
        var currentNodeIndex = 0;
        for (var nameId : pathNameIds) {
            var searchResultIndex = Arrays.binarySearch(
                    entity.childNameIds,
                    entity.nodeFirstChildIndex[currentNodeIndex],
                    entity.nodeFirstChildIndex[currentNodeIndex] + entity.nodeChildCount[currentNodeIndex],
                    nameId
            );
            currentNodeIndex = entity.childNodeIndices[searchResultIndex];
            if (currentNodeIndex < 0) {
                return Collections.emptyList();
            }
        }
        var childCount = entity.nodeChildCount[currentNodeIndex];
        if (childCount == 0) {
            return Collections.emptyList();
        }
        var childPaths = new ArrayList<Path>(childCount);
        for (var childIndex = 0; childIndex < childCount; childIndex++) {
            var childPath = Arrays.copyOf(pathNameIds, pathNameIds.length + 1);
            childPath[pathNameIds.length] = entity.childNameIds[entity.nodeFirstChildIndex[currentNodeIndex] + childIndex];
            childPaths.add(new Path(childPath, this));
        }
        return childPaths;
    }

    private List<Path> descendants(Path path) {
        var result = new ArrayList<Path>();
        // Use a queue for breadth-first traversal
        var queue = new LinkedList<Path>();
        queue.add(path);
        var first = true; // Skip adding self to results
        while (!queue.isEmpty()) {
            var current = queue.removeFirst();
            if (!first) {
                result.add(current);
            } else {
                first = false;
            }
            queue.addAll(current.children());
        }
        return result;
    }
    //endregion

    //region generated code
    public FileIndex(Entity entity) {
        this.entity = entity;
    }
    //endregion
}
