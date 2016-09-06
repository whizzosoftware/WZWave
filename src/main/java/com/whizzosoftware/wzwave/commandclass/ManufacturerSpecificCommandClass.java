/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.commandclass;

import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.persist.PersistenceContext;
import com.whizzosoftware.wzwave.product.ProductInfo;
import com.whizzosoftware.wzwave.node.NodeContext;
import com.whizzosoftware.wzwave.product.ProductRegistry;
import com.whizzosoftware.wzwave.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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

    public DataFrame createGet(byte nodeId) {
        return createSendDataFrame("MANUFACTURER_SPECIFIC_GET", nodeId, new byte[] {ManufacturerSpecificCommandClass.ID, MANUFACTURER_SPECIFIC_GET}, true);
    }

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
    public void onApplicationCommand(NodeContext context, byte[] ccb, int startIndex) {
        logger.debug("Manufacturer specific data: {}", ByteUtil.createString(ccb, startIndex, ccb.length));
        if (ccb[startIndex+1] == MANUFACTURER_SPECIFIC_REPORT) {
            try {
                productInfo = parseManufacturerSpecificData(ccb, startIndex);
                logger.debug("Received MANUFACTURER_SPECIFIC_REPORT: {}", productInfo);
            } catch (CommandClassParseException e) {
                logger.error("Error parsing manufacturer specific data ({}); will request again", e.getLocalizedMessage());
                context.sendDataFrame(createGet(context.getNodeId()));
            }
        } else {
            logger.warn("Ignoring unsupported command: {}", ByteUtil.createString(ccb[startIndex+1]));
        }
    }

    ProductInfo parseManufacturerSpecificData(byte[] ccb, int startIndex) throws CommandClassParseException {
        if (ccb.length >= startIndex + 7) {
            return ProductRegistry.lookupProduct(
                    ByteUtil.convertTwoBytesToInt(ccb[startIndex + 2], ccb[startIndex + 3]),
                    ByteUtil.convertTwoBytesToInt(ccb[startIndex + 4], ccb[startIndex + 5]),
                    ByteUtil.convertTwoBytesToInt(ccb[startIndex + 6], ccb[startIndex + 7])
            );
        } else {
            throw new CommandClassParseException("Manufacturer data is too short");
        }
    }

    @Override
    public int queueStartupMessages(NodeContext context, byte nodeId) {
        if (productInfo == null) {
            context.sendDataFrame(createGet(nodeId));
            return 1;
        } else {
            logger.trace("Skipping initial get since product info known: {}", productInfo);
            return 0;
        }
    }

    @Override
    public Map<String,Object> restore(PersistenceContext ctx, byte nodeId) {
        Map<String,Object> map = super.restore(ctx, nodeId);
        productInfo = ProductRegistry.lookupProduct(
            (Integer)map.get("manufacturerId"),
            (Integer)map.get("productTypeId"),
            (Integer)map.get("productId")
        );
        return map;
    }

    @Override
    public Map<String,Object> save(PersistenceContext ctx, byte nodeId) {
        Map<String,Object> map = super.save(ctx, nodeId);
        if (productInfo.getManufacturerId() != null) {
            map.put("manufacturerId", productInfo.getManufacturerId());
        }
        if (productInfo.getProductTypeId() != null) {
            map.put("productTypeId", productInfo.getProductTypeId());
        }
        if (productInfo.getProductId() != null) {
            map.put("productId", productInfo.getProductId());
        }
        return map;
    }

    void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }
}
