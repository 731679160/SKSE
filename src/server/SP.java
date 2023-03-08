package server;

import HVE.monash.crypto.hve.SHVE;
import HVE.monash.crypto.hve.param.impl.SHVESecretKeyParameter;

import java.util.*;

public class SP {
    HashMap<Integer, List<byte[]>> encBFs;
    int vectorSize;
    public SP(HashMap<Integer, List<byte[]>> encBFs, int vectorSize) {
        this.encBFs = encBFs;
        this.vectorSize = vectorSize;
    }

    public List<Integer> search_SKSE1(List<SHVESecretKeyParameter> query) {
        List<Integer> ans = new LinkedList<>();
        for (Map.Entry<Integer, List<byte[]>> entry : encBFs.entrySet()) {
            for (SHVESecretKeyParameter q : query) {
                if (SHVE.evaluate(q, entry.getValue())) {
                    ans.add(entry.getKey());
                    break;
                }
            }
        }
        return ans;
    }
}
