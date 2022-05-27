package com.example.bluetoothgram;

// Reference
// Nikolay Elenkov and Paul Stöhr. (2015). nelenkov/ecdh-kx. [online] Available at:
// https://github.com/nelenkov/ecdh-kx [Accessed Date: 15 May 2022]

import android.util.Log;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;

public class Cryptography {

    private static final String TAG = Cryptography.class.getSimpleName();
    private static final String PROVIDER = "SC";
    private static final String KEYGEN_ALGORITHM = "ECDH";
    private static Cryptography instance;

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    private KeyFactory key_factory;
    private KeyPairGenerator key_pair_generate;

    static synchronized Cryptography getInstance() {
        if (instance == null) {
            instance = new Cryptography();
        }

        return instance;
    }

    // Instance KeyFactory and KeyPairGenerator
    private Cryptography() {
        try {
            key_factory = KeyFactory.getInstance(KEYGEN_ALGORITHM, PROVIDER);
            key_pair_generate = KeyPairGenerator.getInstance(KEYGEN_ALGORITHM, PROVIDER);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    // Generate the parameter of key pair
    synchronized KeyPair GenKeyPairParams(EC_Parameter ec_param) throws Exception {
        EllipticCurve curve = toCurve(ec_param);
        ECParameterSpec esSpec = new ECParameterSpec(curve, ec_param.getG(),
                ec_param.getN(), ec_param.h);

        key_pair_generate.initialize(esSpec);

        return key_pair_generate.generateKeyPair();
    }

    // Generate ECC curve
    synchronized KeyPair generateKeyPairNamedCurve(String curveName)
            throws Exception {
        ECGenParameterSpec EC_Param_Spec = new ECGenParameterSpec(curveName);
        key_pair_generate.initialize(EC_Param_Spec);

        return key_pair_generate.generateKeyPair();
    }

    // use Base64 to encrypt
    static String Encode_Base64(byte[] b) {
        try {
            return new String(Base64.encode(b), "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    static String hex(byte[] bytes) {
        try {
            return new String(Hex.encode(bytes), "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // use Base64 to decode
    static byte[] Decode_base64(String str) {
        return Base64.decode(str);
    }

    static EllipticCurve toCurve(EC_Parameter ec_param) {
        ECFieldFp fp = new ECFieldFp(ec_param.getP());

        return new EllipticCurve(fp, ec_param.getA(), ec_param.getB());
    }

    // Generate the common secret by combine the received public key and own private key
    byte[] ecdh(PrivateKey Own_PrivateKey, PublicKey Received_PublicKey) throws Exception {
        ECPublicKey ecPubKey = (ECPublicKey) Received_PublicKey;
        Log.d(TAG, "public key Wx: "
                + ecPubKey.getW().getAffineX().toString(16));
        Log.d(TAG, "public key Wy: "
                + ecPubKey.getW().getAffineY().toString(16));

        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", PROVIDER);
        keyAgreement.init(Own_PrivateKey);
        keyAgreement.doPhase(Received_PublicKey, true);

        return keyAgreement.generateSecret();
    }

    // read the public key
    synchronized PublicKey Read_PublicKey(String keyString) throws Exception {
        X509EncodedKeySpec x509ks = new X509EncodedKeySpec(
                Base64.decode(keyString));
        return key_factory.generatePublic(x509ks);
    }

    // read the private key
    synchronized PrivateKey Read_PrivateKey(String keyString) throws Exception {
        PKCS8EncodedKeySpec p8ks = new PKCS8EncodedKeySpec(
                Base64.decode(keyString));

        return key_factory.generatePrivate(p8ks);
    }

    // read the keypair
    synchronized KeyPair Read_KeyPair(String public_keyString, String private_keyString)
            throws Exception {
        return new KeyPair(Read_PublicKey(public_keyString), Read_PrivateKey(private_keyString));
    }
}
