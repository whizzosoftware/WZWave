/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.ApplicationCommand;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.frame.SendData;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Multi Instance Command Class
 *
 * @author Dan Noguerol
 */
public class MultiInstanceCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final byte ID = 0x60;

    // version 1
    private static final byte MULTI_INSTANCE_GET = 0x04;
    private static final byte MULTI_INSTANCE_REPORT = 0x05;
    private static final byte MULTI_INSTANCE_CMD_ENCAP = 0x06;

    // version 2
    private static final byte MULTI_CHANNEL_END_POINT_GET = 0x07;
    private static final byte MULTI_CHANNEL_END_POINT_REPORT = 0x08;
    private static final byte MULTI_CHANNEL_CAPABILITY_GET = 0x09;
    private static final byte MULTI_CHANNEL_CAPABILITY_REPORT = 0x0A;
    private static final byte MULTI_CHANNEL_END_POINT_FIND = 0x0B;
    private static final byte MULTI_CHANNEL_END_POINT_FIND_REPORT = 0x0C;
    private static final byte MULTI_CHANNEL_CMD_ENCAP = 0x0D;

    private Map<Byte, Endpoint> endpointMap = new HashMap<Byte, Endpoint>();
    private int endpointCount;
    private boolean endpointsIdentical;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_MULTI_INSTANCE";
    }

    @Override
    public int getMaxSupportedVersion() {
        return 2;
    }

    /**
     * Returns all the endpoints associated with this node.
     *
     * @return a Collection of Endpoint instances
     */
    public Collection<Endpoint> getEndpoints() {
        return endpointMap.values();
    }

    /**
     * Returns a specific endpoint.
     *
     * @param number the endpoint number
     *
     * @return an Endpoint instance (or null if not found)
     */
    public Endpoint getEndpoint(byte number) {
        return endpointMap.get(number);
    }

    @Override
    public void onDataFrame(DataFrame m, NodeContext context) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();

            switch (ccb[1]) {
                case MULTI_INSTANCE_REPORT: // v1
                    // TODO
                    logger.debug("Received multi instance report -- not currently supported");
                    break;

                case MULTI_CHANNEL_END_POINT_REPORT: // v2
                    this.endpointCount = ccb[3] & 0x3F;
                    this.endpointsIdentical = ((ccb[2] & 0x40) > 0);

                    if (endpointsIdentical) {
                        // if the node reports all endpoints are identical, we simply query the first one for its
                        // capabilities
                        logger.debug(
                            "Node {} has {} identical endpoints; querying for endpoint 1 capability",
                            context.getNodeId(),
                            endpointCount
                        );
                        context.queueDataFrame(createMultiChannelCapabilityGetv2(context.getNodeId(), (byte) 0x01));
                    } else {
                        // if the node reports all endpoints are NOT identical, query each endpoint individually
                        logger.debug(
                                "Node {} has {} non-identical endpoints; querying each endpoint",
                                context.getNodeId(),
                                endpointCount
                        );
                        for (int i=1; i <= endpointCount; i++) {
                            context.queueDataFrame(createMultiChannelCapabilityGetv2(context.getNodeId(), (byte) i));
                        }
                    }
                    break;

                case MULTI_CHANNEL_CAPABILITY_REPORT: // v2
                    byte endpoint = ccb[2];
                    byte genericDeviceClass = ccb[3];
                    byte specificDeviceClass = ccb[4];
                    logger.debug(
                        "Received multi channel capability report for endpoint {}: generic={}, specific={}",
                        endpoint,
                        ByteUtil.createString(genericDeviceClass),
                        ByteUtil.createString(specificDeviceClass)
                    );
                    if (endpointsIdentical) {
                        for (int i=1; i <= endpointCount; i++) {
                            endpointMap.put((byte)i, new Endpoint((byte)i, genericDeviceClass, specificDeviceClass));
                        }
                    } else {
                        endpointMap.put(endpoint, new Endpoint(endpoint, genericDeviceClass, specificDeviceClass));
                    }
                    break;

                default:
                    logger.warn("Ignoring unsupported message: " + m);
                    break;
            }

        } else {
            logger.error("Received unexpected message: " + m);
        }
    }

    @Override
    public void queueStartupMessages(byte nodeId, NodeContext context) {
        if (getVersion() == 1) {
            for (CommandClass cc : context.getCommandClasses()) {
                context.queueDataFrame(createMultiInstanceGetv1(nodeId, cc.getId()));
            }
        } else {
            context.queueDataFrame(createMultiChannelEndPointGetv2(nodeId));
        }
    }

    /**
     * Class that encapsulates information about a multi channel endpoint.
     */
    public class Endpoint {
        public static final byte ALARM_SENSOR = (byte)0xA1;
        public static final byte AV_CONTROL_POINT = 0x03;
        public static final byte BINARY_SENSOR = 0x20;
        public static final byte BINARY_SWITCH = 0x10;
        public static final byte DISPLAY = 0x04;
        public static final byte ENERGY_CONTROL = 0x40;
        public static final byte METER = 0x31;
        public static final byte MULTI_LEVEL_SENSOR = 0x21;
        public static final byte MULTI_LEVEL_SWITCH = 0x11;
        public static final byte PULSE_METER = 0x30;
        public static final byte REMOTE_SWITCH = 0x12;
        public static final byte THERMOSTAT = 0x08;
        public static final byte TOGGLE_SWITCH = 0x13;
        public static final byte VENTILATION = 0x16;

        private byte number;
        private byte genericDeviceClass;
        private byte specificDeviceClass;

        public Endpoint(byte number, byte genericDeviceClass, byte specificDeviceClass) {
            this.number = number;
            this.genericDeviceClass = genericDeviceClass;
            this.specificDeviceClass = specificDeviceClass;
        }

        public byte getNumber() {
            return number;
        }

        public byte getGenericDeviceClass() {
            return genericDeviceClass;
        }

        public byte getSpecificDeviceClass() {
            return specificDeviceClass;
        }
    }

    /**
     * Create a MULTI_INSTANCE_GET command.
     *
     * @param nodeId the target node ID
     * @param commandClass the command class being requested
     *
     * @return a DataFrame instance
     */
    static public DataFrame createMultiInstanceGetv1(byte nodeId, byte commandClass) {
        return createSendDataFrame(
            "MULTI_INSTANCE_GET",
            nodeId,
            new byte[] {
                MultiInstanceCommandClass.ID,
                MULTI_INSTANCE_GET,
                commandClass
            },
            true
        );
    }

    /**
     * Create a MULTI_CHANNEL_END_POINT_GET command.
     *
     * @param nodeId the target node ID
     *
     * @return a DataFrame instance
     */
    static public DataFrame createMultiChannelEndPointGetv2(byte nodeId) {
        return createSendDataFrame(
            "MULTI_CHANNEL_END_POINT_GET",
            nodeId,
            new byte[] {
                MultiInstanceCommandClass.ID,
                MULTI_CHANNEL_END_POINT_GET
            },
            true
        );
    }

    /**
     * Create a MULTI_CHANNEL_CAPABILITY_GET command.
     *
     * @param nodeId the target node ID
     * @param endPoint the endpoint ID
     *
     * @return a DataFrame instance
     */
    static public DataFrame createMultiChannelCapabilityGetv2(byte nodeId, byte endPoint) {
        return createSendDataFrame(
            "MULTI_CHANNEL_CAPABILITY_GET",
            nodeId,
            new byte[] {
                MultiInstanceCommandClass.ID,
                MULTI_CHANNEL_CAPABILITY_GET,
                endPoint
            },
            true
        );
    }

    static public DataFrame createMultiChannelCmdEncapv2(byte sourceEndpoint, byte destEndpoint, SendData command) {
        byte[] data = command.getSendData();

        byte[] newData = new byte[data.length + 4];
        newData[0] = MultiInstanceCommandClass.ID;
        newData[1] = MULTI_CHANNEL_CMD_ENCAP;
        newData[2] = sourceEndpoint;
        newData[3] = destEndpoint;
        System.arraycopy(data, 0, newData, 4, data.length);

        return createSendDataFrame(
            "MULTI_CHANNEL_CMD_ENCAP",
            command.getNodeId(),
            newData,
            false
        );
    }
}
