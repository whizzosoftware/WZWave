/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.security.EncryptionHelper;
import com.whizzosoftware.wzwave.security.NonceProvider;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;

public class SecurityCommandClass extends CommandClass {
    private static final Logger logger = LoggerFactory.getLogger(SecurityCommandClass.class);

    public static final byte ID = (byte)0x98;

    private static final byte SECURITY_COMMANDS_SUPPORTED_GET = 0x02;
    private static final byte SECURITY_COMMANDS_SUPPORTED_REPORT = 0x03;
    private static final byte SECURITY_SCHEME_GET = 0x04;
    private static final byte SECURITY_SCHEME_REPORT = 0x05;
    private static final byte NETWORK_KEY_SET = 0x06;
    private static final byte NETWORK_KEY_VERIFY = 0x07;
    private static final byte SECURITY_SCHEME_INHERIT = 0x08;
    private static final byte SECURITY_NONCE_GET = 0x40;
    private static final byte SECURITY_NONCE_REPORT = (byte)0x80;
    private static final byte SECURITY_MESSAGE_ENCAPSULATION = (byte)0x81;
    private static final byte SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET = (byte)0xC1;

    private Byte supportedSecuritySchemes;
    private byte[] nonceReport;

    public SecurityCommandClass() {
        this(false);
    }

    public SecurityCommandClass(boolean secure) {
        super();
        setSecure(secure);
    }

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_SECURITY";
    }

    public boolean isSchemeReport() {
        return (supportedSecuritySchemes != null);
    }

    public boolean isNonceReport() {
        return (nonceReport != null);
    }

    public Byte getSupportedSecuritySchemes() {
        return supportedSecuritySchemes;
    }

    public byte[] getNonceReport() {
        return nonceReport;
    }

    @Override
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        switch (ccb[startIndex+1]) {
            case SECURITY_SCHEME_REPORT:
                supportedSecuritySchemes = ccb[startIndex+2];
                break;
            case SECURITY_NONCE_REPORT:
                nonceReport = new byte[8];
                for (int i=0; i < 8; i++) {
                    nonceReport[i] = ccb[startIndex+2+i];
                }
                break;
            default:
                logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        return 0;
    }

    static public DataFrame createSchemeGetv1(byte nodeId) {
        return createSendDataFrame("SECURITY_SCHEME_GET", nodeId, new byte[] {SecurityCommandClass.ID, SECURITY_SCHEME_GET}, true);
    }

    static public DataFrame createGetNoncev1(byte nodeId) {
        return createSendDataFrame("GET_NONCE", nodeId, new byte[] {SecurityCommandClass.ID, SECURITY_NONCE_GET}, true);
    }

    static public DataFrame createNonceReportv1(byte nodeId, byte[] nonce) {
        return createSendDataFrame("GET_NONCE_REPORT", nodeId, new byte[] {
            SecurityCommandClass.ID,
            SECURITY_NONCE_REPORT,
            nonce[0],
            nonce[1],
            nonce[2],
            nonce[3],
            nonce[4],
            nonce[5],
            nonce[6],
            nonce[7],
        }, true);
    }

    static public DataFrame createMessageEncapsulationv1(String name, byte nodeId, NonceProvider noncep, byte commandClassId, byte commandId, byte[] commandBytes, byte[] encKey, byte[] authKey) throws GeneralSecurityException {
        byte[] randomNonce = noncep.getRandomNonce();
        byte[] targetNonce = noncep.getTargetNonce();
        System.out.println("Target nonce: " + ByteUtil.createString(targetNonce, 8));
        byte[] msg = new byte[22 + commandBytes.length];
        byte[] iv = EncryptionHelper.createInitializationVector(randomNonce, targetNonce);

        msg[0] = ID;
        msg[1] = SECURITY_MESSAGE_ENCAPSULATION;
        System.arraycopy(randomNonce, 0, msg, 2, 8);

        byte[] payload = new byte[3+commandBytes.length];
        payload[0] = 0;
        payload[1] = commandClassId;
        payload[2] = commandId;
        System.arraycopy(commandBytes, 0, payload, 3, commandBytes.length);
        byte[] encPayload = EncryptionHelper.encryptOFB(payload, iv, encKey);
        System.arraycopy(encPayload, 0, msg, 10, encPayload.length);

        msg[10+encPayload.length] = targetNonce[0];

        byte[] mac = EncryptionHelper.createMAC(SECURITY_MESSAGE_ENCAPSULATION, (byte)0x01, nodeId, targetNonce, encPayload, authKey);
        System.out.println("MAC: " + ByteUtil.createString(mac, 8));
        System.arraycopy(mac, 0, msg, 11+encPayload.length, 8);

        return createSendDataFrame("foo", nodeId, msg, true);
    }

    static public DataFrame createNetworkKeySetv1(ZWaveControllerContext ctx, byte srcNodeId, byte dstNodeId, NonceProvider noncep) throws GeneralSecurityException {
        byte[] networkKey = ctx.getNetworkKey();
        byte[] randomNonce = noncep.getRandomNonce();
        byte[] iv = EncryptionHelper.createInitializationVector(randomNonce, noncep.getTargetNonce());
        byte[] scheme0Key = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] encPayload = EncryptionHelper.encryptOFB(new byte[] {
            0,
            ID,
            NETWORK_KEY_SET,
            networkKey[0],
            networkKey[1],
            networkKey[2],
            networkKey[3],
            networkKey[4],
            networkKey[5],
            networkKey[6],
            networkKey[7],
            networkKey[8],
            networkKey[9],
            networkKey[10],
            networkKey[11],
            networkKey[12],
            networkKey[13],
            networkKey[14],
            networkKey[15]
        }, iv, scheme0Key);

        logger.trace("Generating NetworkKeySet MAC with src {} and dst {}", srcNodeId, dstNodeId);
        byte[] mac = EncryptionHelper.createMAC(SECURITY_MESSAGE_ENCAPSULATION, srcNodeId, dstNodeId, iv, encPayload, scheme0Key);

        DataFrame df = createSendDataFrame("SECURITY_MESSAGE_ENCAPSULATION_KEY_SET", dstNodeId, new byte[] {
            SecurityCommandClass.ID,
            SECURITY_MESSAGE_ENCAPSULATION,
            randomNonce[0],
            randomNonce[1],
            randomNonce[2],
            randomNonce[3],
            randomNonce[4],
            randomNonce[5],
            randomNonce[6],
            randomNonce[7],
            encPayload[0],
            encPayload[1],
            encPayload[2],
            encPayload[3],
            encPayload[4],
            encPayload[5],
            encPayload[6],
            encPayload[7],
            encPayload[8],
            encPayload[9],
            encPayload[10],
            encPayload[11],
            encPayload[12],
            encPayload[13],
            encPayload[14],
            encPayload[15],
            encPayload[16],
            encPayload[17],
            encPayload[18],
            0x00, // receiver's nonce identifier
            mac[0],
            mac[1],
            mac[2],
            mac[3],
            mac[4],
            mac[5],
            mac[6],
            mac[7]
        }, true);

        byte[] b = df.getBytes();
        System.out.println(ByteUtil.createString(b, b.length));

        return df;
    }
}
