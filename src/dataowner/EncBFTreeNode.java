package dataowner;

import java.util.HashSet;
import java.util.List;

public class EncBFTreeNode {
    public int id = -1;
    public List<byte[]> encBF;
    public HashSet<EncBFTreeNode> childes;

    public EncBFTreeNode(List<byte[]> encBF) {
        this.encBF = encBF;
        childes = new HashSet<>();
    }
}
