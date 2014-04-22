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
import com.whizzosoftware.wzwave.product.ProductInfo;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.product.ProductRegistry;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manufacturer Specific Command Class
 *
 * @author Dan Noguerol
 */
public class ManufacturerSpecificCommandClass extends CommandClass {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final byte MANUFACTURER_SPECIFIC_GET = 0x04;
    private static final byte MANUFACTURER_SPECIFIC_REPORT = 0x05;

    public static final byte ID = 0x72;

    private ProductInfo productInfo;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "COMMAND_CLASS_MANUFACTURER_SPECIFIC";
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    @Override
    public void onDataFrame(DataFrame m, NodeContext context) {
        if (m instanceof ApplicationCommand) {
            ApplicationCommand cmd = (ApplicationCommand)m;
            byte[] ccb = cmd.getCommandClassBytes();
            logger.debug("Manufacturer specific data: {}", ByteUtil.createString(ccb, ccb.length));
            if (ccb[1] == MANUFACTURER_SPECIFIC_REPORT) {
                productInfo = parseManufacturerSpecificData(ccb);
                logger.debug("Received MANUFACTURER_SPECIFIC_REPORT: {}", productInfo);
            } else {
                logger.warn("Ignoring unsupported message: {}", m);
            }
        } else {
            logger.error("Received unexpected message: {}", m);
        }
    }

    @Override
    public void queueStartupMessages(byte nodeId, NodeContext context) {
        context.queueDataFrame(createGetv1(nodeId));
    }

    public ProductInfo parseManufacturerSpecificData(byte[] ccb) {
        return ProductRegistry.lookupProduct(
                ByteUtil.convertTwoBytesToInt(ccb[2], ccb[3]),
                ByteUtil.convertTwoBytesToInt(ccb[4], ccb[5]),
                ByteUtil.convertTwoBytesToInt(ccb[6], ccb[7])
        );
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("MANUFACTURER_SPECIFIC_GET", nodeId, new byte[] {ManufacturerSpecificCommandClass.ID, MANUFACTURER_SPECIFIC_GET}, true);
    }
}
