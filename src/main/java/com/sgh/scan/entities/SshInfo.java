package com.sgh.scan.entities;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.sun.jersey.core.util.Base64;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FS;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Getter
@Slf4j
public class SshInfo {
    String passPhase;
    String sshPrivateKey; //file key
    String sshPublicKey; //file key
    SshSessionFactory sshSessionFactory;

    String sshDir = System.getProperty("user.dir") + "/project-base/.ssh/";

    public SshInfo(String sshPrivateKey, String passwordPhase) {
        this.sshPrivateKey = sshPrivateKey;
        this.passPhase = passwordPhase;
        initSessionFactory();
    }

    public static SshInfo newInstance() { return new SshInfo(); }
    private SshInfo() {}

    private void initSessionFactory() {
        sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                var defaultJSch = super.createDefaultJSch(fs);
                defaultJSch.addIdentity(sshPrivateKey);
                return defaultJSch;
            }

            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");//TODO: should not do ?
                session.setUserInfo(new UserInfo() {
                    @Override
                    public String getPassphrase() { return passPhase; }

                    @Override
                    public String getPassword() { return null; }

                    @Override
                    public boolean promptPassword(String message) { return false; }

                    @Override
                    public boolean promptPassphrase(String message) { return true; }

                    @Override
                    public boolean promptYesNo(String message) { return false; }

                    @Override
                    public void showMessage(String message) {}
                });
            }
        };
    }

    @Deprecated
    public void generateKeys(String keyAlgorithm, int numBits, String keyFileName) {
        try {
            log.info("Start Generating key/value pair using " + keyAlgorithm + " algorithm with " + numBits + " bits");

            // Get the public/private key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
            keyGen.initialize(numBits);
            KeyPair keyPair = keyGen.genKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Get the formats of the encoded bytes
            log.info("Private Key Format : " + privateKey.getFormat()); //PKCS#8
            log.info("Public Key Format : " + publicKey.getFormat()); //X.509

            // Get the bytes of the public and private keys
            byte[] privateKeyBytes = privateKey.getEncoded();
            byte[] publicKeyBytes = publicKey.getEncoded();
            log.info("Private Key : " + Base64.encode(String.valueOf(privateKeyBytes)));
            log.info("Public Key : " + Base64.encode(String.valueOf(publicKeyBytes)));

            log.info("Write as binary file");
            String pvtBinKeyFile = sshDir + keyFileName + "_bin.key";
            FileOutputStream outBinary = new FileOutputStream(pvtBinKeyFile);
            outBinary.write(privateKey.getEncoded());
            outBinary.close();

            String pubBinKeyFile = sshDir + keyFileName + "_bin.pub";
            outBinary = new FileOutputStream(pubBinKeyFile);
            outBinary.write(publicKey.getEncoded());
            outBinary.close();

            /*log.info("Add Digital Signature");
            String dataFile = sshDir + "id_rsa.data";
            String pvtSignedKeyFile = sshDir + "id_rsa_signed.key";
            generateDigitalSignature(keyAlgorithm, pvtBinKeyFile, dataFile, pvtSignedKeyFile);*/

            log.info("Write file as text file");
            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

            Path path = Paths.get(pvtBinKeyFile);
            privateKeyBytes = Files.readAllBytes(path);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateSignedKey = keyFactory.generatePrivate(privateKeySpec);

            path = Paths.get(pubBinKeyFile);
            publicKeyBytes = Files.readAllBytes(path);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicSignedKey = keyFactory.generatePublic(publicKeySpec);

            String pvtKeyFile = sshDir + keyFileName + ".key";
            Writer outText = new FileWriter(pvtKeyFile);
            outText.write("-----BEGIN RSA PRIVATE KEY-----\n");
            outText.write(encoder.encodeToString(privateSignedKey.getEncoded()));
            outText.write("\n-----END RSA PRIVATE KEY-----\n");
            outText.close();

            String pubKeyFile = sshDir + keyFileName + ".pub";
            outText = new FileWriter(pubKeyFile);
            //outText.write("-----BEGIN RSA PUBLIC KEY-----\n");
            //outText.write(encoder.encodeToString(publicSignedKey.getEncoded()));
            outText.write(encodePublicKey(publicSignedKey, "Admin@Sgh"));
            //outText.write("\n-----END RSA PUBLIC KEY-----\n");
            outText.close();

            sshPrivateKey = pvtKeyFile;
            sshPublicKey = pubKeyFile;

            log.info("Done Generating key/value pair using " + privateKey.getAlgorithm() + " algorithm with " + numBits + " bits");
        } catch (InvalidKeySpecException specException) {
            log.info("Invalid Key Spec Exception");
        } catch (NoSuchAlgorithmException e) {
            log.info("No such algorithm: " + keyAlgorithm);
        } catch (IOException e) {
            log.info("Write File Exception");
        }
    }

    @Deprecated
    String encodePublicKey(PublicKey publicKey, String user) throws IOException {
        String publicKeyEncoded;
        if(publicKey.getAlgorithm().equals("RSA")){
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);
            dos.writeInt("ssh-rsa".getBytes().length);
            dos.write("ssh-rsa".getBytes());
            dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dos.write(rsaPublicKey.getPublicExponent().toByteArray());
            dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dos.write(rsaPublicKey.getModulus().toByteArray());
            publicKeyEncoded = java.util.Base64.getEncoder().encodeToString(byteOs.toByteArray());
            return "ssh-rsa " + publicKeyEncoded + " " + user;
        }
        else if(publicKey.getAlgorithm().equals("DSA")){
            DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
            DSAParams dsaParams = dsaPublicKey.getParams();

            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);
            dos.writeInt("ssh-dss".getBytes().length);
            dos.write("ssh-dss".getBytes());
            dos.writeInt(dsaParams.getP().toByteArray().length);
            dos.write(dsaParams.getP().toByteArray());
            dos.writeInt(dsaParams.getQ().toByteArray().length);
            dos.write(dsaParams.getQ().toByteArray());
            dos.writeInt(dsaParams.getG().toByteArray().length);
            dos.write(dsaParams.getG().toByteArray());
            dos.writeInt(dsaPublicKey.getY().toByteArray().length);
            dos.write(dsaPublicKey.getY().toByteArray());
            publicKeyEncoded = java.util.Base64.getEncoder().encodeToString(byteOs.toByteArray());
            return "ssh-dss " + publicKeyEncoded + " " + user;
        } else {
            throw new IllegalArgumentException("Unknown public key encoding: " + publicKey.getAlgorithm());
        }
    }

    public boolean generateJSchKeys(String keyAlgorithm, String keyFileName, String passPhase, String userComment) {
        try {
            log.info("Start Generating key/value pair using " + keyAlgorithm);

            var type = 0;
            if (keyAlgorithm.equals("RSA")) { type = com.jcraft.jsch.KeyPair.RSA; }
            else if(keyAlgorithm.equals("DSA")) { type = com.jcraft.jsch.KeyPair.DSA;}

            var jsch = new JSch();

            com.jcraft.jsch.KeyPair kpair = com.jcraft.jsch.KeyPair.genKeyPair(jsch, type, 2048);
            kpair.setPassphrase(passPhase);
            sshPrivateKey = sshDir + keyFileName;
            sshPublicKey = sshDir + keyFileName + ".pub";
            kpair.writePrivateKey(sshPrivateKey);
            kpair.writePublicKey(sshPublicKey, userComment);
            log.info("Finger print: " + kpair.getFingerPrint());
            kpair.dispose();

            log.info("Done Generating key/value pair using " + keyAlgorithm);
            return true;
        } catch (Exception e) {
            log.info("Generate JSch Keys Failed");
            e.printStackTrace();
            return false;
        }
    }

    boolean verifyDigitalSignature(String keyAlgorithm, String pubKeyFile, String dataFile, String pvtSignedFile) {
        try {
            Path path = Paths.get(pubKeyFile);
            byte[] bytes = Files.readAllBytes(path);

            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(keyAlgorithm);
            PublicKey pub = kf.generatePublic(ks);

            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(pub);

            InputStream in = null;
            try {
                in = new FileInputStream(dataFile);
                byte[] buf = new byte[2048];
                int len;
                while ((len = in.read(buf)) != -1) {
                    sign.update(buf, 0, len);
                }
            } finally {
                if (in != null) { in.close(); }
            }

            path = Paths.get(pvtSignedFile);
            bytes = Files.readAllBytes(path);
            boolean verify = sign.verify(bytes);
            log.info(dataFile + ": Signature " +  (verify ? "OK" : "Not OK"));
            return verify;
        } catch (IOException e) {
            log.info("File Read/Write Exception");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            log.info("Generate Digital Signature Exception");
        }
        return false;
    }

    void generateDigitalSignature(String keyAlgorithm, String keyFile, String dataFile, String signedFile) {
        try {
            Path path = Paths.get(keyFile);
            byte[] bytes = Files.readAllBytes(path);

            KeyFactory kf = KeyFactory.getInstance(keyAlgorithm);
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);

            Signature sign = Signature.getInstance("SHA256withRSA");
            PrivateKey key = kf.generatePrivate(ks);
            sign.initSign(key);

            InputStream in = null;
            try {
                in = new FileInputStream(dataFile);
                byte[] buf = new byte[2048];
                int len;
                while ((len = in.read(buf)) != -1) {
                    sign.update(buf, 0, len);
                }
            } catch (SignatureException e) {
                log.info("Generate Digital Signature Exception");
            } finally {
                if (in != null) { in.close(); }
            }

            OutputStream out = null;
            try {
                out = new FileOutputStream(signedFile);
                byte[] signature = sign.sign();
                out.write(signature);
            } finally {
                if (out != null) { out.close(); }
            }
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.info("Generate Digital Signature Exception");
        } catch (IOException e) {
            log.info("Write File Exception");
        }
    }
}
