package test;

import HVE.monash.crypto.hve.param.impl.SHVESecretKeyParameter;
import dataowner.DO;
import dataowner.SpatialData;
import server.SP;
import tools.DataProcess;

import java.util.List;

public class SKSE_1 {
    public static final int B = 3;//空间数据bit数
    public static final int L = 2;//空间数据维数

    public static void main(String[] args) throws Exception {
        //布隆过滤器参数
        double falsePositiveProbability = 0.1;
        int expectedNumberOfElements = 100;

        //获取空间数据
        String path = "./src/test.txt";
        List<SpatialData> spatialData = DataProcess.readSpatialData(path);

        DO dataOwner = new DO(spatialData, falsePositiveProbability, expectedNumberOfElements);
        SP server = new SP(dataOwner.getEncBFs(), dataOwner.getVectorSize());

        long[] point1 = {1, 1};
        long[] point2 = {2, 2};
        int[] keywords = {1049, 9703};
        List<SHVESecretKeyParameter> searchToken = dataOwner.getSearchToken(point1, point2, keywords);
        List<Integer> res = server.search_SKSE1(searchToken);
        System.out.println(res);
    }
}
