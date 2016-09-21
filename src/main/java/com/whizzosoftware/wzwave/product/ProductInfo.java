/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.product;

/**
 * Class that encapsulates information about a product.
 *
 * @author Dan Noguerol
 */
public class ProductInfo {
    public static final String UNKNOWN = "Unknown";

    private Integer manufacturerId;
    private String manufacturer;
    private Integer productTypeId;
    private Integer productId;
    private String name;

    public ProductInfo(Integer manufacturerId, Integer productTypeId, Integer productId) {
        this.manufacturerId = manufacturerId;
        this.productTypeId = productTypeId;
        this.productId = productId;
    }

    public Integer getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(Integer manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(Integer productTypeId) {
        this.productTypeId = productTypeId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isComplete() {
        return (manufacturer != null && name != null && !UNKNOWN.equals(manufacturer) && !UNKNOWN.equals(name));
    }

    public String toString() {
        if (manufacturer != null) {
            if (name == null) {
                name = UNKNOWN;
            }
            return manufacturer + " " + name;
        } else {
            return UNKNOWN;
        }
    }
}

