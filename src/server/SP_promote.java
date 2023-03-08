package server;

import HVE.monash.crypto.hve.SHVE;
import HVE.monash.crypto.hve.param.impl.SHVESecretKeyParameter;
import dataowner.EncBFTreeNode;
import dataowner.SearchTokenTreeNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SP_promote {
    EncBFTreeNode encBFTree;
    public SP_promote(EncBFTreeNode encBFTree) {
        this.encBFTree = encBFTree;
    }

    public void SKSE2Main(SearchTokenTreeNode queryNode, EncBFTreeNode encTreeNode, List<Integer> res) {
        if (queryNode == null || encTreeNode == null) return;
        if (SHVE.evaluate(queryNode.encQuery, encTreeNode.encBF)) {
            if (encTreeNode.id != -1) res.add(encTreeNode.id);
            else {
                for (SearchTokenTreeNode queryChild : queryNode.childes) {
                    for (EncBFTreeNode encNodeChild : encTreeNode.childes) {
                        SKSE2Main(queryChild, encNodeChild, res);
                    }
                }
            }
        }

    }
    public List<Integer> search_SKSE2(SearchTokenTreeNode queryRoot) {
        List<Integer> res = new LinkedList<>();
        SKSE2Main(queryRoot, encBFTree, res);
        return res;
    }
}
