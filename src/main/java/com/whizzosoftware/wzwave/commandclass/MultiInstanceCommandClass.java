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
import com.whizzosoftware.wzwave.node.ZWaveNodeEndpoint;
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

    private Map<Byte, ZWaveNodeEndpoint> endpointMap = new HashMap<Byte, ZWaveNodeEndpoint>();
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
    public Collection<ZWaveNodeEndpoint> getEndpoints() {
        return endpointMap.values();
    }

    /**
     * Returns a specific endpoint.
     *
     * @param number the endpoint number
     *
     * @return an Endpoint instance (or null if not found)
     */
    public ZWaveNodeEndpoint getEndpoint(byte number) {
        return endpointMap.get(number);
    }

    @Override
    public void onDataFrame(DataFrame m, NodeContext context) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();
            switch (ccb[1]) {
                case MULTI_INSTANCE_REPORT: // v1
                    processMultiInstanceReport();
                    break;

                case MULTI_CHANNEL_END_POINT_REPORT: // v2
                    processMultiChannelEndpointReport(ccb, context);
                    break;

                case MULTI_CHANNEL_CAPABILITY_REPORT: // v2
                    processMultiChannelCapabilityReport(ccb, context);
                    break;

                case MULTI_CHANNEL_CMD_ENCAP: // v2
                    processMultiChannelCommandEncapsulation(ccb, context);
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

    protected void processMultiInstanceReport() {
        // TODO
        logger.debug("Received multi instance report -- not currently supported");
    }

    protected void processMultiChannelEndpointReport(byte[] ccb, NodeContext context) {
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
    }

    protected void processMultiChannelCapabilityReport(byte[] ccb, NodeContext context) {
        byte endpoint = ccb[2];
        logger.debug("Received multi channel capability report for endpoint {}", endpoint);

        if (endpointsIdentical) {
            // if endpoints are identical, use the same information for all of them
            for (int i=1; i <= endpointCount; i++) {
                createNewEndpoint((byte)i, ccb);
            }
        } else {
            // otherwise, create an endpoint for just the number we received
            createNewEndpoint(endpoint, ccb);
        }
    }

    protected void createNewEndpoint(byte number, byte[] ccb) {
        byte genericDeviceClass = ccb[3];
        byte specificDeviceClass = ccb[4];
        ZWaveNodeEndpoint ep = new ZWaveNodeEndpoint(number, genericDeviceClass, specificDeviceClass);
        for (int x=5; x < ccb.length; x++) {
            CommandClass cc = CommandClassFactory.createCommandClass(ccb[x]);
            if (cc != null) {
                ep.addCommandClass(cc);
            } else {
                logger.warn("Endpoint reported unknown command class: {}", ccb[x]);
            }
        }
        endpointMap.put(number, ep);
    }

    protected void processMultiChannelCommandEncapsulation(byte[] ccb, NodeContext context) {
        // TODO
        logger.debug("Got multi channel cmd encap response");
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
