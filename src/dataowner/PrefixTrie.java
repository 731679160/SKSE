package dataowner;

public class PrefixTrie {
    String prefix = "";
    PrefixTrie[] childes;

    public PrefixTrie() {
    }

    public PrefixTrie(String prefix) {
        this.prefix = prefix;
    }

    void insert(String prefix) {
        PrefixTrie root = this;
        for (char c : prefix.toCharArray()) {
            if (root.childes == null) root.childes = new PrefixTrie[3];
            if (c == '0') {
                if (root.childes[0] == null) root.childes[0] = new PrefixTrie(root.prefix + 0);
                root = root.childes[0];
            } else if (c == '1') {
                if (root.childes[1] == null) root.childes[1] = new PrefixTrie(root.prefix + 1);
                root = root.childes[1];
            } else {
                if (root.childes[2] == null) root.childes[2] = new PrefixTrie(root.prefix + '*');
                root = root.childes[2];
            }
        }
    }
}
