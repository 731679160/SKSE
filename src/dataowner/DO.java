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
import tools.BloomFilter;
import tools.Utils;

import java.util.*;

import static test.SKSE_1.B;
import static test.SKSE_1.L;

public class DO {
    int vectorSize;
    int rangeBitSize;
    KeyParameter MSK;
    SmallHilbertCurve c;
    double falsePositiveProbability;
    int expectedNumberOfElements;
    HashMap<Integer, List<byte[]>> encBFs;

    public HashMap<Integer, List<byte[]>> getEncBFs() {
        return encBFs;
    }

    public int getVectorSize() {
        return vectorSize;
    }

    public DO(List<SpatialData> spatialData, double falsePositiveProbability, int expectedNumberOfElements) {
        //设置布隆过滤器参数
        this.falsePositiveProbability = falsePositiveProbability;
        this.expectedNumberOfElements = expectedNumberOfElements;

        //创建HilbertCurve
        c = HilbertCurve.small().bits(B).dimensions(L);

        //计算转换成一维范围数据后，数据的最长bit位
        rangeBitSize = Integer.toBinaryString((1 << B) * (1 << B)).length();

        //处理数据，并将每个对象映射到一个布隆过滤器
        HashMap<Integer, BloomFilter<String>> BFs = buildBF(spatialData);

        //初始化HVE
        MSK = SHVE.setup(vectorSize);
        AESUtil.encode("test".getBytes(), ((SHVEMasterSecretKeyParameter) MSK).getMSK());// warm-up AES here, warm-up phase is used to load all static java classes into memory, which can avoid extra classloader overhead

        //利用HVE加密布隆过滤器
        encBFs = encryptBFs(BFs);
    }

    public HashMap<Integer, BloomFilter<String>> buildBF(List<SpatialData> spatialData) {
        //为每个id创建布隆过滤器
        HashMap<Integer, BloomFilter<String>> BFs = new HashMap<>();
        for (SpatialData data : spatialData) {
            int id = data.id;
            long range = c.index(data.x, data.y);//利用HilbertCurve转换为一维范围数据
            List<String> prefix = Utils.getPrefix(range, rangeBitSize);//获取二进制前缀

            //创建布隆过滤器并添加数据
            BloomFilter<String> bloomFilter = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfElements);
            vectorSize = bloomFilter.size();
            for (String p : prefix) bloomFilter.add(p);//添加前缀
            for (int keyword : data.keywords) bloomFilter.add(Integer.toString(keyword));//添加关键字
            BFs.put(id, bloomFilter);
        }
        return BFs;
    }

    public HashMap<Integer, List<byte[]>> encryptBFs(HashMap<Integer, BloomFilter<String>> BFs) {
        HashMap<Integer, List<byte[]>> encBFs = new HashMap<>();
        for (Map.Entry<Integer, BloomFilter<String>> entry : BFs.entrySet()) {
            int id = entry.getKey();
            BloomFilter<String> BF = entry.getValue();
            BitSet bitSet = BF.getBitSet();

            //将bf转换成vector,即int[]
            int[] vector = new int[BF.size()];
            for (int i = 0; i < vector.length; ++i) {
                vector[i] = bitSet.get(i) ? 1 : 0;
            }

            List<byte[]> encBF = SHVE.enc(MSK, vector);//加密
            encBFs.put(id, encBF);
        }
        return encBFs;
    }

    public List<SHVESecretKeyParameter> getSearchToken(long[] point1, long[] point2, int[] keywords) {
        //根据二维范围获取Hilbert曲线的范围
        Ranges ranges = c.query(point1, point2, 0);
        List<long[]> queryRanges = new LinkedList<>();
        for (Range range : ranges) {
            queryRanges.add(new long[]{range.low(), range.high()});
        }

        List<SHVESecretKeyParameter> tokens = new ArrayList<>(queryRanges.size());

        //为每个范围生成一个查询令牌
        for (long[] queryRange : queryRanges) {
            List<String> prefix = Utils.getRangePrefix(queryRange[0], queryRange[1], rangeBitSize);//获取二进制前缀

            //每个前缀都需要生成一个查询
            for (String p : prefix) {
                //创建布隆过滤器
                BloomFilter<String> bloomFilter = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfElements);
                bloomFilter.add(p);//添加前缀
                for (int keyword : keywords) bloomFilter.add(Integer.toString(keyword));//添加关键字

                //将bf转换成vector,即int[]
                BitSet bitSet = bloomFilter.getBitSet();
                int[] vector = new int[bloomFilter.size()];
                for (int i = 0; i < vector.length; ++i) {
                    if (bitSet.get(i)) vector[i] = 1;
                    else vector[i] = -1;
                }

                //加密查询，获取查询令牌
                SHVESecretKeyParameter encQuery = (SHVESecretKeyParameter) SHVE.keyGen(MSK, vector);
                tokens.add(encQuery);
            }
        }
        return tokens;
    }

}
