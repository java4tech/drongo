package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.crypto.ECKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

public class TransactionSignature extends ECKey.ECDSASignature {
    /**
     * A byte that controls which parts of a transaction are signed. This is exposed because signatures
     * parsed off the wire may have sighash flags that aren't "normal" serializations of the enum values.
     * Because Bitcoin Core works via bit testing, we must not lose the exact value when round-tripping
     * otherwise we'll fail to verify signature hashes.
     */
    public final byte sighashFlags;

    /** Constructs a signature with the given components and SIGHASH_ALL. */
    public TransactionSignature(BigInteger r, BigInteger s) {
        this(r, s, SigHash.ALL.value);
    }

    /** Constructs a signature with the given components and raw sighash flag bytes (needed for rule compatibility). */
    public TransactionSignature(BigInteger r, BigInteger s, byte sighashFlags) {
        super(r, s);
        this.sighashFlags = sighashFlags;
    }

    /** Constructs a transaction signature based on the ECDSA signature. */
    public TransactionSignature(ECKey.ECDSASignature signature, SigHash sigHash) {
        super(signature.r, signature.s);
        sighashFlags = sigHash.value;
    }

    /**
     * Returns a dummy invalid signature whose R/S values are set such that they will take up the same number of
     * encoded bytes as a real signature. This can be useful when you want to fill out a transaction to be of the
     * right size (e.g. for fee calculations) but don't have the requisite signing key yet and will fill out the
     * real signature later.
     */
    public static TransactionSignature dummy() {
        BigInteger val = ECKey.HALF_CURVE_ORDER;
        return new TransactionSignature(val, val);
    }

    /**
     * Returns true if the given signature is has canonical encoding, and will thus be accepted as standard by
     * Bitcoin Core. DER and the SIGHASH encoding allow for quite some flexibility in how the same structures
     * are encoded, and this can open up novel attacks in which a man in the middle takes a transaction and then
     * changes its signature such that the transaction hash is different but it's still valid. This can confuse wallets
     * and generally violates people's mental model of how Bitcoin should work, thus, non-canonical signatures are now
     * not relayed by default.
     */
    public static boolean isEncodingCanonical(byte[] signature) {
        // See Bitcoin Core's IsCanonicalSignature, https://bitcointalk.org/index.php?topic=8392.msg127623#msg127623
        // A canonical signature exists of: <30> <total len> <02> <len R> <R> <02> <len S> <S> <hashtype>
        // Where R and S are not negative (their first byte has its highest bit not set), and not
        // excessively padded (do not start with a 0 byte, unless an otherwise negative number follows,
        // in which case a single 0 byte is necessary and even required).

        // Empty signatures, while not strictly DER encoded, are allowed.
        if (signature.length == 0)
            return true;

        if (signature.length < 9 || signature.length > 73)
            return false;

        int hashType = (signature[signature.length-1] & 0xff) & ~SigHash.ANYONECANPAY.value; // mask the byte to prevent sign-extension hurting us
        if (hashType < SigHash.ALL.value || hashType > SigHash.SINGLE.value)
            return false;

        //                   "wrong type"                  "wrong length marker"
        if ((signature[0] & 0xff) != 0x30 || (signature[1] & 0xff) != signature.length-3)
            return false;

        int lenR = signature[3] & 0xff;
        if (5 + lenR >= signature.length || lenR == 0)
            return false;
        int lenS = signature[5+lenR] & 0xff;
        if (lenR + lenS + 7 != signature.length || lenS == 0)
            return false;

        //    R value type mismatch          R value negative
        if (signature[4-2] != 0x02 || (signature[4] & 0x80) == 0x80)
            return false;
        if (lenR > 1 && signature[4] == 0x00 && (signature[4+1] & 0x80) != 0x80)
            return false; // R value excessively padded

        //       S value type mismatch                    S value negative
        if (signature[6 + lenR - 2] != 0x02 || (signature[6 + lenR] & 0x80) == 0x80)
            return false;
        if (lenS > 1 && signature[6 + lenR] == 0x00 && (signature[6 + lenR + 1] & 0x80) != 0x80)
            return false; // S value excessively padded

        return true;
    }

    public boolean anyoneCanPay() {
        return (sighashFlags & SigHash.ANYONECANPAY.value) != 0;
    }

    public SigHash getSigHash() {
        boolean anyoneCanPay = anyoneCanPay();
        final int mode = sighashFlags & 0x1f;
        if (mode == SigHash.NONE.value) {
            return anyoneCanPay ? SigHash.ANYONECANPAY_NONE : SigHash.NONE;
        } else if (mode == SigHash.SINGLE.value) {
            return anyoneCanPay ? SigHash.ANYONECANPAY_SINGLE : SigHash.SINGLE;
        } else {
            return anyoneCanPay ? SigHash.ANYONECANPAY_ALL : SigHash.ALL;
        }
    }

    /**
     * What we get back from the signer are the two components of a signature, r and s. To get a flat byte stream
     * of the type used by Bitcoin we have to encode them using DER encoding, which is just a way to pack the two
     * components into a structure, and then we append a byte to the end for the sighash flags.
     */
    public byte[] encodeToBitcoin() {
        try {
            ByteArrayOutputStream bos = derByteStream();
            bos.write(sighashFlags);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    @Override
    public ECKey.ECDSASignature toCanonicalised() {
        return new TransactionSignature(super.toCanonicalised(), getSigHash());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        if(!super.equals(o)) {
            return false;
        }
        TransactionSignature signature = (TransactionSignature) o;
        return sighashFlags == signature.sighashFlags;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sighashFlags);
    }

    /**
     * Returns a decoded signature.
     *
     * @param requireCanonicalEncoding if the encoding of the signature must
     * be canonical.
     * @param requireCanonicalSValue if the S-value must be canonical (below half
     * the order of the curve).
     * @throws SignatureDecodeException if the signature is unparseable in some way.
     * @throws VerificationException if the signature is invalid.
     */
    public static TransactionSignature decodeFromBitcoin(byte[] bytes, boolean requireCanonicalEncoding,
                                                         boolean requireCanonicalSValue) throws SignatureDecodeException, VerificationException {
        // Bitcoin encoding is DER signature + sighash byte.
        if (requireCanonicalEncoding && !isEncodingCanonical(bytes))
            throw new VerificationException.NoncanonicalSignature();
        ECKey.ECDSASignature sig = ECKey.ECDSASignature.decodeFromDER(bytes);
        if (requireCanonicalSValue && !sig.isCanonical())
            throw new VerificationException("S-value is not canonical.");

        // In Bitcoin, any value of the final byte is valid, but not necessarily canonical. See javadocs for
        // isEncodingCanonical to learn more about this. So we must store the exact byte found.
        return new TransactionSignature(sig.r, sig.s, bytes[bytes.length - 1]);
    }
}