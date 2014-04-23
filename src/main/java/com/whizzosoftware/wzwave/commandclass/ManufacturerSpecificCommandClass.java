/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.commandclass;

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
    public void onApplicationCommand(byte[] ccb, int startIndex, NodeContext context) {
        logger.debug("Manufacturer specific data: {}", ByteUtil.createString(ccb, startIndex, ccb.length));
        if (ccb[startIndex+1] == MANUFACTURER_SPECIFIC_REPORT) {
            productInfo = parseManufacturerSpecificData(ccb, startIndex);
            logger.debug("Received MANUFACTURER_SPECIFIC_REPORT: {}", productInfo);
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    @Override
    public void queueStartupMessages(byte nodeId, NodeContext context) {
        context.queueDataFrame(createGetv1(nodeId));
    }

    public ProductInfo parseManufacturerSpecificData(byte[] ccb, int startIndex) {
        return ProductRegistry.lookupProduct(
                ByteUtil.convertTwoBytesToInt(ccb[startIndex+2], ccb[startIndex+3]),
                ByteUtil.convertTwoBytesToInt(ccb[startIndex+4], ccb[startIndex+5]),
                ByteUtil.convertTwoBytesToInt(ccb[startIndex+6], ccb[startIndex+7])
        );
    }

    static public DataFrame createGetv1(byte nodeId) {
        return createSendDataFrame("MANUFACTURER_SPECIFIC_GET", nodeId, new byte[] {ManufacturerSpecificCommandClass.ID, MANUFACTURER_SPECIFIC_GET}, true);
    }
}
