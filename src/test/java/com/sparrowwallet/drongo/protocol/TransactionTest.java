package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.address.P2PKHAddress;
import com.sparrowwallet.drongo.crypto.ECKey;
import org.junit.Assert;
import org.junit.Test;

public class TransactionTest {
    @Test
    public void verifyP2WPKH() {
        String hex = "0100000002fff7f7881a8099afa6940d42d1e7f6362bec38171ea3edf433541db4e4ad969f0000000000eeffffffef51e1b804cc89d182d279655c3aa89e815b1b309fe287d9b2b55d57b90ec68a0100000000ffffffff02202cb206000000001976a9148280b37df378db99f66f85c95a783a76ac7a6d5988ac9093510d000000001976a9143bde42dbee7e4dbe6a21b2d50ce2f0167faa815988ac11000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("025476c2e83188368da1ff3e292e7acafcdb3566bb0ad253f62fc70f07aeee6357"));
        P2PKHAddress address = new P2PKHAddress(pubKey.getPubKeyHash());
        Sha256Hash hash = transaction.hashForWitnessSignature(1, address.getOutputScript(),600000000L, Transaction.SigHash.ALL, false);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("304402203609e17b84f6a7d30c80bfa610b5b4542f32a8a0d5447a12fb1366d7f01cc44a0220573a954c4518331561406f90300e8f3358f51928d43c212a8caed02de67eebee"), false, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2SHP2WPKH() {
        String hex = "0100000001db6b1b20aa0fd7b23880be2ecbd4a98130974cf4748fb66092ac4d3ceb1a54770100000000feffffff02b8b4eb0b000000001976a914a457b684d7f0d539a46a45bbc043f35b59d0d96388ac0008af2f000000001976a914fd270b1ee6abcaea97fea7ad0402e8bd8ad6d77c88ac92040000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("03ad1d8e89212f0b92c74d23bb710c00662ad1470198ac48c43f7d6f93a2a26873"));
        Address address = new P2PKHAddress(pubKey.getPubKeyHash());
        Sha256Hash hash = transaction.hashForWitnessSignature(0, address.getOutputScript(),1000000000L, Transaction.SigHash.ALL, false);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("3044022047ac8e878352d3ebbde1c94ce3a10d057c24175747116f8288e5d794d12d482f0220217f36a485cae903c713331d877c1f64677e3622ad4010726870540656fe9dcb01"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2WSHSigHashSingle() {
        String hex = "0100000002fe3dc9208094f3ffd12645477b3dc56f60ec4fa8e6f5d67c565d1c6b9216b36e0000000000ffffffff0815cf020f013ed6cf91d29f4202e8a58726b1ac6c79da47c23d1bee0a6925f80000000000ffffffff0100f2052a010000001976a914a30741f8145e5acadf23f751864167f32e0963f788ac00000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("026dccc749adc2a9d0d89497ac511f760f45c47dc5ed9cf352a58ac706453880ae"));
        Script script = new Script(Utils.hexToBytes("21026dccc749adc2a9d0d89497ac511f760f45c47dc5ed9cf352a58ac706453880aeadab210255a9626aebf5e29c0e6538428ba0d1dcf6ca98ffdf086aa8ced5e0d0215ea465ac"));
        Sha256Hash hash = transaction.hashForWitnessSignature(1, script,4900000000L, Transaction.SigHash.SINGLE, false);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("3044022027dc95ad6b740fe5129e7e62a75dd00f291a2aeb1200b84b09d9e3789406b6c002201a9ecd315dd6a0e632ab20bbb98948bc0c6fb204f2c286963bb48517a7058e2703"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2WSHSigHashSingleAnyoneCanPay() {
        String hex = "0100000002e9b542c5176808107ff1df906f46bb1f2583b16112b95ee5380665ba7fcfc0010000000000ffffffff80e68831516392fcd100d186b3c2c7b95c80b53c77e77c35ba03a66b429a2a1b0000000000ffffffff0280969800000000001976a914de4b231626ef508c9a74a8517e6783c0546d6b2888ac80969800000000001976a9146648a8cd4531e1ec47f35916de8e259237294d1e88ac00000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("0392972e2eb617b2388771abe27235fd5ac44af8e61693261550447a4c3e39da98"));
        Script script = new Script(Utils.hexToBytes("68210392972e2eb617b2388771abe27235fd5ac44af8e61693261550447a4c3e39da98ac"));
        Sha256Hash hash = transaction.hashForWitnessSignature(1, script,16777215L, Transaction.SigHash.SINGLE, true);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("30440220032521802a76ad7bf74d0e2c218b72cf0cbc867066e2e53db905ba37f130397e02207709e2188ed7f08f4c952d9d13986da504502b8c3be59617e043552f506c46ff83"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2SHP2WSHSigHashAll() {
        String hex = "010000000136641869ca081e70f394c6948e8af409e18b619df2ed74aa106c1ca29787b96e0100000000ffffffff0200e9a435000000001976a914389ffce9cd9ae88dcc0631e88a821ffdbe9bfe2688acc0832f05000000001976a9147480a33f950689af511e6e84c138dbbd3c3ee41588ac00000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("0307b8ae49ac90a048e9b53357a2354b3334e9c8bee813ecb98e99a7e07e8c3ba3"));
        Script script = new Script(Utils.hexToBytes("56210307b8ae49ac90a048e9b53357a2354b3334e9c8bee813ecb98e99a7e07e8c3ba32103b28f0c28bfab54554ae8c658ac5c3e0ce6e79ad336331f78c428dd43eea8449b21034b8113d703413d57761b8b9781957b8c0ac1dfe69f492580ca4195f50376ba4a21033400f6afecb833092a9a21cfdf1ed1376e58c5d1f47de74683123987e967a8f42103a6d48b1131e94ba04d9737d61acdaa1322008af9602b3b14862c07a1789aac162102d8b661b0b3302ee2f162b09e07a55ad5dfbe673a9f01d9f0c19617681024306b56ae"));
        Sha256Hash hash = transaction.hashForWitnessSignature(0, script,987654321L, Transaction.SigHash.ALL, false);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("304402206ac44d672dac41f9b00e28f4df20c52eeb087207e8d758d76d92c6fab3b73e2b0220367750dbbe19290069cba53d096f44530e4f98acaa594810388cf7409a1870ce01"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2SHP2WSHSigHashNone() {
        String hex = "010000000136641869ca081e70f394c6948e8af409e18b619df2ed74aa106c1ca29787b96e0100000000ffffffff0200e9a435000000001976a914389ffce9cd9ae88dcc0631e88a821ffdbe9bfe2688acc0832f05000000001976a9147480a33f950689af511e6e84c138dbbd3c3ee41588ac00000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("03b28f0c28bfab54554ae8c658ac5c3e0ce6e79ad336331f78c428dd43eea8449b"));
        Script script = new Script(Utils.hexToBytes("56210307b8ae49ac90a048e9b53357a2354b3334e9c8bee813ecb98e99a7e07e8c3ba32103b28f0c28bfab54554ae8c658ac5c3e0ce6e79ad336331f78c428dd43eea8449b21034b8113d703413d57761b8b9781957b8c0ac1dfe69f492580ca4195f50376ba4a21033400f6afecb833092a9a21cfdf1ed1376e58c5d1f47de74683123987e967a8f42103a6d48b1131e94ba04d9737d61acdaa1322008af9602b3b14862c07a1789aac162102d8b661b0b3302ee2f162b09e07a55ad5dfbe673a9f01d9f0c19617681024306b56ae"));
        Sha256Hash hash = transaction.hashForWitnessSignature(0, script,987654321L, Transaction.SigHash.NONE, false);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("3044022068c7946a43232757cbdf9176f009a928e1cd9a1a8c212f15c1e11ac9f2925d9002205b75f937ff2f9f3c1246e547e54f62e027f64eefa2695578cc6432cdabce271502"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2SHP2WSHSigHashSingle() {
        String hex = "010000000136641869ca081e70f394c6948e8af409e18b619df2ed74aa106c1ca29787b96e0100000000ffffffff0200e9a435000000001976a914389ffce9cd9ae88dcc0631e88a821ffdbe9bfe2688acc0832f05000000001976a9147480a33f950689af511e6e84c138dbbd3c3ee41588ac00000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("034b8113d703413d57761b8b9781957b8c0ac1dfe69f492580ca4195f50376ba4a"));
        Script script = new Script(Utils.hexToBytes("56210307b8ae49ac90a048e9b53357a2354b3334e9c8bee813ecb98e99a7e07e8c3ba32103b28f0c28bfab54554ae8c658ac5c3e0ce6e79ad336331f78c428dd43eea8449b21034b8113d703413d57761b8b9781957b8c0ac1dfe69f492580ca4195f50376ba4a21033400f6afecb833092a9a21cfdf1ed1376e58c5d1f47de74683123987e967a8f42103a6d48b1131e94ba04d9737d61acdaa1322008af9602b3b14862c07a1789aac162102d8b661b0b3302ee2f162b09e07a55ad5dfbe673a9f01d9f0c19617681024306b56ae"));
        Sha256Hash hash = transaction.hashForWitnessSignature(0, script,987654321L, Transaction.SigHash.SINGLE, false);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("3044022059ebf56d98010a932cf8ecfec54c48e6139ed6adb0728c09cbe1e4fa0915302e022007cd986c8fa870ff5d2b3a89139c9fe7e499259875357e20fcbb15571c76795403"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2SHP2WSHSigHashAllAnyoneCanPay() {
        String hex = "010000000136641869ca081e70f394c6948e8af409e18b619df2ed74aa106c1ca29787b96e0100000000ffffffff0200e9a435000000001976a914389ffce9cd9ae88dcc0631e88a821ffdbe9bfe2688acc0832f05000000001976a9147480a33f950689af511e6e84c138dbbd3c3ee41588ac00000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("033400f6afecb833092a9a21cfdf1ed1376e58c5d1f47de74683123987e967a8f4"));
        Script script = new Script(Utils.hexToBytes("56210307b8ae49ac90a048e9b53357a2354b3334e9c8bee813ecb98e99a7e07e8c3ba32103b28f0c28bfab54554ae8c658ac5c3e0ce6e79ad336331f78c428dd43eea8449b21034b8113d703413d57761b8b9781957b8c0ac1dfe69f492580ca4195f50376ba4a21033400f6afecb833092a9a21cfdf1ed1376e58c5d1f47de74683123987e967a8f42103a6d48b1131e94ba04d9737d61acdaa1322008af9602b3b14862c07a1789aac162102d8b661b0b3302ee2f162b09e07a55ad5dfbe673a9f01d9f0c19617681024306b56ae"));
        Sha256Hash hash = transaction.hashForWitnessSignature(0, script,987654321L, Transaction.SigHash.ALL, true);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("3045022100fbefd94bd0a488d50b79102b5dad4ab6ced30c4069f1eaa69a4b5a763414067e02203156c6a5c9cf88f91265f5a942e96213afae16d83321c8b31bb342142a14d16381"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2SHP2WSHSigHashNoneAnyoneCanPay() {
        String hex = "010000000136641869ca081e70f394c6948e8af409e18b619df2ed74aa106c1ca29787b96e0100000000ffffffff0200e9a435000000001976a914389ffce9cd9ae88dcc0631e88a821ffdbe9bfe2688acc0832f05000000001976a9147480a33f950689af511e6e84c138dbbd3c3ee41588ac00000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("03a6d48b1131e94ba04d9737d61acdaa1322008af9602b3b14862c07a1789aac16"));
        Script script = new Script(Utils.hexToBytes("56210307b8ae49ac90a048e9b53357a2354b3334e9c8bee813ecb98e99a7e07e8c3ba32103b28f0c28bfab54554ae8c658ac5c3e0ce6e79ad336331f78c428dd43eea8449b21034b8113d703413d57761b8b9781957b8c0ac1dfe69f492580ca4195f50376ba4a21033400f6afecb833092a9a21cfdf1ed1376e58c5d1f47de74683123987e967a8f42103a6d48b1131e94ba04d9737d61acdaa1322008af9602b3b14862c07a1789aac162102d8b661b0b3302ee2f162b09e07a55ad5dfbe673a9f01d9f0c19617681024306b56ae"));
        Sha256Hash hash = transaction.hashForWitnessSignature(0, script,987654321L, Transaction.SigHash.NONE, true);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("3045022100a5263ea0553ba89221984bd7f0b13613db16e7a70c549a86de0cc0444141a407022005c360ef0ae5a5d4f9f2f87a56c1546cc8268cab08c73501d6b3be2e1e1a8a0882"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }

    @Test
    public void verifyP2SHP2WSHSigHashSingleAnyoneCanPay() {
        String hex = "010000000136641869ca081e70f394c6948e8af409e18b619df2ed74aa106c1ca29787b96e0100000000ffffffff0200e9a435000000001976a914389ffce9cd9ae88dcc0631e88a821ffdbe9bfe2688acc0832f05000000001976a9147480a33f950689af511e6e84c138dbbd3c3ee41588ac00000000";
        Transaction transaction = new Transaction(Utils.hexToBytes(hex));

        ECKey pubKey = ECKey.fromPublicOnly(Utils.hexToBytes("02d8b661b0b3302ee2f162b09e07a55ad5dfbe673a9f01d9f0c19617681024306b"));
        Script script = new Script(Utils.hexToBytes("56210307b8ae49ac90a048e9b53357a2354b3334e9c8bee813ecb98e99a7e07e8c3ba32103b28f0c28bfab54554ae8c658ac5c3e0ce6e79ad336331f78c428dd43eea8449b21034b8113d703413d57761b8b9781957b8c0ac1dfe69f492580ca4195f50376ba4a21033400f6afecb833092a9a21cfdf1ed1376e58c5d1f47de74683123987e967a8f42103a6d48b1131e94ba04d9737d61acdaa1322008af9602b3b14862c07a1789aac162102d8b661b0b3302ee2f162b09e07a55ad5dfbe673a9f01d9f0c19617681024306b56ae"));
        Sha256Hash hash = transaction.hashForWitnessSignature(0, script,987654321L, Transaction.SigHash.SINGLE, true);
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(Utils.hexToBytes("30440220525406a1482936d5a21888260dc165497a90a15669636d8edca6b9fe490d309c022032af0c646a34a44d1f4576bf6a4a74b67940f8faa84c7df9abe12a01a11e2b4783"), true, true);
        Assert.assertTrue(pubKey.verify(hash, signature));
    }
}