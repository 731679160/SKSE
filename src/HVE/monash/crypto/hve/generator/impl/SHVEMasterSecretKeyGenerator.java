package HVE.monash.crypto.hve.generator.impl;

import HVE.monash.crypto.hve.generator.SecretKeyGenerator;
import HVE.monash.crypto.hve.param.KeyGenerationParameter;
import HVE.monash.crypto.hve.param.KeyParameter;
import HVE.monash.crypto.hve.param.impl.SHVEMasterSecretKeyGenerationParameter;
import HVE.monash.crypto.hve.param.impl.SHVEMasterSecretKeyParameter;
import HVE.monash.crypto.hve.param.impl.SHVEParameter;
import HVE.monash.crypto.hve.util.RandomUtil;

/**
 * Master secret key (msk) generator uses the msk generation parameter
 * to generate a msk.
 *
 * @author Shangqi
 */
public final class SHVEMasterSecretKeyGenerator implements SecretKeyGenerator {

    private SHVEMasterSecretKeyGenerationParameter keyParameter;

    public SHVEMasterSecretKeyGenerator() {
    }

    public void init(KeyGenerationParameter parameter) {
        this.keyParameter = (SHVEMasterSecretKeyGenerationParameter) parameter;
    }

    // generate a key with the given security parameter
    public KeyParameter generateKey() {
        SHVEParameter parameter = this.keyParameter.getParameter();
        byte[] MSK = RandomUtil.getRandom(keyParameter.keyLength - 1);

        return new SHVEMasterSecretKeyParameter(parameter, MSK);
    }
}
