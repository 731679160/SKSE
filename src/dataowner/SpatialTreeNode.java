package dataowner;

import tools.BloomFilter;

import java.util.HashSet;
import java.util.List;

public class SpatialTreeNode {
    int id = -1;
    public List<Integer> keywords;
    public List<String> prefixes;
    public List<SpatialData> dataList;
    public SpatialTreeNode[] childes;

    public SpatialTreeNode(List<SpatialData> dataList, List<Integer> keywords, List<String> prefixes) {
        childes = new SpatialTreeNode[4];
        this.dataList = dataList;
        this.keywords = keywords;
        this.prefixes = prefixes;
    }
}
