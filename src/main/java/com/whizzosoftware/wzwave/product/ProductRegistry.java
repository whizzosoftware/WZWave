/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that returns information for all products known by the library.
 *
 * @author Dan Noguerol
 */
public class ProductRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ProductRegistry.class);

    // Find info here: http://www.cd-jackson.com/index.php/zwave/zwave-device-database/zwave-device-list
    public final static String M_PHILIO = "Philio Technology Corporation";
    public final static String M_AEON_LABS = "Aeon Labs";
    public final static String M_EVERSPRING = "Everspring";
    public final static String M_GE_JASCO = "GE/Jasco";
    public final static String M_FIBARGROUP = "Fibargroup";

    public final static String P_PAN11 = "Smart Energy Plug In Switch (PAN11)";
    public final static String P_PAN16 = "Smart Energy Plug In Switch (PAN16)";
    public final static String P_45604_OUTDOOR_MODULE = "45604 Outdoor Module";
    public final static String P_45609_RELAY_SWITCH = "45609 On/Off Relay Switch";
    public final static String P_45612_DIMMER_SWITCH = "45612 Dimmer Switch";
    public final static String P_HOME_ENERGY_METER = "Home Energy Meter";
    public final static String P_HOME_ENERGY_METER_G2 = "Home Energy Meter G2";
    public final static String P_SM103_DOOR_WINDOW_SENSOR = "SM103 Door/Window Sensor";
    public final static String P_SMART_ENERGY_SWITCH = "Smart Energy Switch";
    public final static String P_SMART_ENERGY_STRIP = "Smart Energy Strip";
    public final static String P_WATER_SENSOR = "Water Sensor";
    public final static String P_METERED_WALL_PLUG = "Metered Wall Plug Switch";

    static public ProductInfo lookupProduct(Integer manufacturerId, Integer productTypeId, Integer productId) {
        ProductInfo info = new ProductInfo(manufacturerId, productTypeId, productId);
        info.setManufacturer(ProductInfo.UNKNOWN);
        info.setName(ProductInfo.UNKNOWN);

        logger.debug("Looking up product info: {}, {}, {}", manufacturerId, productTypeId, productId);

        int mid = manufacturerId != null ? manufacturerId : 0;
        int ptid = productTypeId != null ? productTypeId : 0;
        int pid = productId != null ? productId : 0;

        switch (mid) {
            // Aeon Labs
            case -122:
                info.setManufacturer(M_AEON_LABS);
                switch (ptid) {
                    case 2:
                        switch (pid) {
                            case 9:
                                info.setName(P_HOME_ENERGY_METER);
                                break;
                            case 28:
                                info.setName(P_HOME_ENERGY_METER_G2);
                                break;
                            case 45:
                                info.setName(P_WATER_SENSOR);
                                break;
                        }
                        break;
                    case 3:
                        switch (pid) {
                            case 6:
                                info.setName(P_SMART_ENERGY_SWITCH);
                                break;
                            case 11:
                                info.setName(P_SMART_ENERGY_STRIP);
                                break;
                        }
                        break;
                }
                break;

            // Everspring
            case 96:
                info.setManufacturer(M_EVERSPRING);
                switch (ptid) {
                    case 2:
                        switch (pid) {
                            case 1:
                                info.setName(P_SM103_DOOR_WINDOW_SENSOR);
                                break;
                        }
                        break;
                }
                break;

            // GE/Jasco
            case 99:
                info.setManufacturer(M_GE_JASCO);
                switch (ptid) {
                    case 17495:
                        switch (pid) {
                            case 12848:
                                info.setName(P_45612_DIMMER_SWITCH);
                                break;
                        }
                        break;
                    case 21072:
                        switch (pid) {
                            case 12592:
                                info.setName(P_45604_OUTDOOR_MODULE);
                                break;
                        }
                        break;
                    case 21079:
                        switch (pid) {
                            case 13619:
                                info.setName(P_45609_RELAY_SWITCH);
                                break;
                        }
                        break;
                }
                break;

            // Fibaro
            case 271:
                info.setManufacturer(M_FIBARGROUP);
                switch (ptid) {
                    case 1538:
                        switch (pid) {
                            case 4097:
                                info.setName(P_METERED_WALL_PLUG);
                                break;
                        }
                        break;
                }
                break;

            // Philio
            case 316:
                info.setManufacturer(M_PHILIO);
                switch (ptid) {
                    case 1:
                        switch (pid) {
                            case 17:
                                info.setName(P_PAN11);
                                break;
                            case 41:
                                info.setName(P_PAN16);
                                break;
                        }
                        break;
                }

            default:
                break;
        }

        if (!info.isComplete() && manufacturerId != null && productTypeId != null && productId != null) {
            logger.error("You are using a product that is not in WZWave's product registry. If the product is " +
                    "working properly with WZWave, please submit the following to the project so it can be " +
                    "added to the registry: Resolved {} from Manufacturer: {}, Product Type: {}, Product Id:{}", info, manufacturerId, productTypeId, productId);
        }

        return info;
    }
}
