package test;

import HVE.monash.crypto.hve.param.impl.SHVESecretKeyParameter;
import dataowner.DO;
import dataowner.DO_promote;
import dataowner.SearchTokenTreeNode;
import dataowner.SpatialData;
import server.SP;
import server.SP_promote;
import tools.DataProcess;

import java.util.List;

public class SKSE_2 {
    public static final int B = 21;//空间数据bit数
    public static final int L = 2;//空间数据维数

    public static void main(String[] args) throws Exception {
        //布隆过滤器参数
        double falsePositiveProbability = 0.01;

        //获取空间数据
        String path = "./src/LA.txt";
        List<SpatialData> spatialData = DataProcess.readSpatialData(path);

        DO_promote dataOwner = new DO_promote(spatialData, falsePositiveProbability);
        SP_promote server = new SP_promote(dataOwner.getEncBFTree());

        long[] point1 = {1154903, 326486};
        long[] point2 = {1154903, 326486};
        int[] keywords = {9837};
        SearchTokenTreeNode searchToken = dataOwner.getSearchToken(point1, point2, keywords);
        List<Integer> res = server.search_SKSE2(searchToken);
        System.out.println(res);
    }
}
