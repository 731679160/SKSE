package dataowner;

import HVE.monash.crypto.hve.SHVE;
import HVE.monash.crypto.hve.param.KeyParameter;
import HVE.monash.crypto.hve.param.impl.SHVESecretKeyParameter;
import tools.BloomFilter;

import java.util.BitSet;
import java.util.HashSet;

public class SearchTokenTreeNode {
    public SHVESecretKeyParameter encQuery;
    public HashSet<SearchTokenTreeNode> childes;


    public SearchTokenTreeNode(KeyParameter MSK, String prefix, int[] keywords, double falsePositiveProbability, int expectedNumberOfElements) {
        getEncQuery(MSK, prefix, keywords, falsePositiveProbability, expectedNumberOfElements);
    }

    public void getEncQuery(KeyParameter MSK, String prefix, int[] keywords, double falsePositiveProbability, int expectedNumberOfElements) {
        //创建布隆过滤器
        BloomFilter<String> bloomFilter = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfElements);
        bloomFilter.add(prefix);
        for (int keyword : keywords) bloomFilter.add(Integer.toString(keyword));//添加关键字

        //将bf转换成vector,即int[]
        BitSet bitSet = bloomFilter.getBitSet();
        int[] vector = new int[bloomFilter.size()];
        for (int i = 0; i < vector.length; ++i) {
            if (bitSet.get(i)) vector[i] = 1;
            else vector[i] = -1;
        }

        encQuery = (SHVESecretKeyParameter) SHVE.keyGen(MSK, vector);
    }
}
