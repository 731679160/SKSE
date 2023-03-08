package dataowner;

import HILBERT.davidmoten.hilbert.HilbertCurve;
import HILBERT.davidmoten.hilbert.Range;
import HILBERT.davidmoten.hilbert.Ranges;
import HILBERT.davidmoten.hilbert.SmallHilbertCurve;
import HVE.monash.crypto.hve.SHVE;
import HVE.monash.crypto.hve.param.KeyParameter;
import HVE.monash.crypto.hve.param.impl.SHVEMasterSecretKeyParameter;
import HVE.monash.crypto.hve.param.impl.SHVESecretKeyParameter;
import HVE.monash.crypto.hve.util.AESUtil;
import com.sun.xml.internal.org.jvnet.mimepull.CleanUpExecutorFactory;
import tools.BloomFilter;

import java.util.*;

import static test.SKSE_1.B;
import static test.SKSE_1.L;
import static tools.Utils.getRangePrefix;

public class DO_promote {
    int vectorSize;
    int rangeBitSize;
    int[] levelMaxElements;
    KeyParameter[] levelMSK;
    SmallHilbertCurve c;
    double falsePositiveProbability;
    EncBFTreeNode encBFTree;

    public EncBFTreeNode getEncBFTree() {
        return encBFTree;
    }

    public int getVectorSize() {
        return vectorSize;
    }

    public DO_promote(List<SpatialData> spatialData, double falsePositiveProbability) {
        //设置布隆过滤器参数
        this.falsePositiveProbability = falsePositiveProbability;

        //创建HilbertCurve
        c = HilbertCurve.small().bits(B).dimensions(L);

        //利用HilbertCurve将所有数据转换为一维范围数据
        long maxRange = 0;
        for (SpatialData data : spatialData) {
            data.range = c.index(data.x, data.y);//利用HilbertCurve转换为一维范围数据
            if (data.range > maxRange) maxRange = data.range;
        }

        //计算转换成一维范围数据后，数据的最长bit位
        rangeBitSize = Long.toBinaryString(maxRange).length();
        while (rangeBitSize % 2 != 0) rangeBitSize++;//要能被4整除
        levelMaxElements = new int[rangeBitSize / 2 + 1];//初始化树的层数，用于记录树各层元素的最大数目
        levelMSK = new KeyParameter[rangeBitSize / 2 + 1];//因为每层的HVE长度不同，所以每层的MSK不同
        //建立空间树
        SpatialTreeNode spatialTree = buildSpatialTree(spatialData, 0, (1 << rangeBitSize) - 1, 0);

        //初始化HVE
        for (int i = 0; i < levelMSK.length; ++i) {
            BloomFilter<String> testBf = new BloomFilter<>(falsePositiveProbability, levelMaxElements[i]);
            levelMSK[i] = SHVE.setup(testBf.size());
            AESUtil.encode("test".getBytes(), ((SHVEMasterSecretKeyParameter) levelMSK[i]).getMSK());// warm-up AES here, warm-up phase is used to load all static java classes into memory, which can avoid extra classloader overhead
        }

        //为空间树生成布隆过滤器并利用HVE加密
        encBFTree = EncSpatialTree(spatialTree, 0);
    }


    public EncBFTreeNode EncSpatialTree(SpatialTreeNode spatialTreeNode, int level) {
        if (spatialTreeNode == null) return null;

        //创建布隆过滤器
        BloomFilter<String> bf = new BloomFilter<String>(falsePositiveProbability, levelMaxElements[level]);

        //添加数据
        for (String prefix : spatialTreeNode.prefixes) bf.add(prefix);
        for (int keyword : spatialTreeNode.keywords) bf.add(String.valueOf(keyword));

        //将bf转换成vector,即int[]
        BitSet bitSet = bf.getBitSet();
        int[] vector = new int[bitSet.size()];
        for (int i = 0; i < vector.length; ++i) {
            vector[i] = bitSet.get(i) ? 1 : 0;
        }

        List<byte[]> encBF = SHVE.enc(levelMSK[level], vector);//加密
        EncBFTreeNode node = new EncBFTreeNode(encBF);//创建结点

        if (level == levelMaxElements.length - 1) {//为叶结点
            node.id = spatialTreeNode.id;
        } else {//递归建立子树
            for (SpatialTreeNode child : spatialTreeNode.childes) {
                node.childes.add(EncSpatialTree(child, level + 1));
            }
        }

        return node;
    }

    public SpatialTreeNode buildSpatialTree(List<SpatialData> dataList, int low, int high, int level) {//包括low，包括high
        //若没有数据
        if (dataList.size() == 0) return null;

        //获取范围前缀
        List<String> prefixes = new LinkedList<>();
        List<String> rangePrefix = getRangePrefix(low, high, rangeBitSize);
        prefixes.add(rangePrefix.get(0));
        char[] chars = rangePrefix.get(0).toCharArray();
        for (int pos = chars.length - 1; pos >= 0; pos--) {
            if (chars[pos] != '*') {
                chars[pos] = '*';
                prefixes.add(String.valueOf(chars));
            }
        }
        //获取合并的关键字
        HashSet<Integer> st = new HashSet<>();
        List<Integer> keywords = new LinkedList<>();
        for (SpatialData data : dataList) {
            for (int keyword : data.keywords) {
                if (!st.contains(keyword)) {
                    keywords.add(keyword);
                    st.add(keyword);
                }
            }
        }
        SpatialTreeNode node = new SpatialTreeNode(dataList, keywords, prefixes);
        if (levelMaxElements[level] < keywords.size() + prefixes.size())
            levelMaxElements[level] = keywords.size() + prefixes.size();

        //若为叶子结点
        if (low == high) {
            node.id = dataList.get(0).id;
            return node;
        }

        //将dataList分成4份
        int dis = (high - low + 1) / 4;
        List<SpatialData>[] subDataList = new List[4];
        for (int i = 0; i < 4; ++i) {
            subDataList[i] = new LinkedList<>();
        }
        for (SpatialData data : dataList) {
            if (data.range < low + dis) {
                subDataList[0].add(data);
            } else if (data.range < low + 2 * dis) {
                subDataList[1].add(data);
            } else if (data.range < low + 3 * dis) {
                subDataList[2].add(data);
            } else {
                subDataList[3].add(data);
            }
        }

        //建立子树
        for (int i = 0; i < 4; ++i) {
            node.childes[i] = buildSpatialTree(subDataList[i], low + dis * i, low + dis * (i + 1) - 1, level + 1);
        }
        return node;
    }


    public SearchTokenTreeNode getSearchToken(long[] point1, long[] point2, int[] keywords) {
        //根据二维范围获取Hilbert曲线的范围
        Ranges ranges = c.query(point1, point2, 0);
        List<long[]> queryRanges = new LinkedList<>();

        //建立前缀树，用于获取查询令牌
        PrefixTrie trie = new PrefixTrie();
        for (Range range : ranges) {
            long low = range.low();
            long high = range.high();
            List<String> prefixes = getRangePrefix(low, high, rangeBitSize);//获取二进制前缀
            for (String prefix : prefixes) {
                trie.insert(prefix);
            }
        }

        //获取查询令牌
        SearchTokenTreeNode searchTokenTree = buildSearchTokenTree(trie, keywords, 0);
        return searchTokenTree;
    }

    SearchTokenTreeNode buildSearchTokenTree(PrefixTrie trie, int[] keywords, int level) {
        String prefix = trie.prefix;
        while (prefix.length() != rangeBitSize) prefix += '*';
        SearchTokenTreeNode root = new SearchTokenTreeNode(levelMSK[level], prefix, keywords, falsePositiveProbability, levelMaxElements[level]);
        if (trie.childes != null) {
            root.childes = new HashSet<>();
            for (PrefixTrie child : trie.childes) {
                if (child == null) continue;
                for (PrefixTrie c : child.childes) {
                    if (c == null) continue;
                    SearchTokenTreeNode treeChild = buildSearchTokenTree(c, keywords, level + 1);
                    root.childes.add(treeChild);
                }
            }
        }
        return root;
    }

}
