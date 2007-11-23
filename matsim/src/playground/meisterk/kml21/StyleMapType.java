/* *********************************************************************** *
 * project: org.matsim.*
 * StyleMapType.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.03.13 at 04:54:32 PM CET 
//


package playground.meisterk.kml21;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for StyleMapType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StyleMapType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://earth.google.com/kml/2.1}StyleSelectorType">
 *       &lt;sequence>
 *         &lt;element name="Pair" type="{http://earth.google.com/kml/2.1}StyleMapPairType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StyleMapType", propOrder = {
    "pair"
})
public class StyleMapType
    extends StyleSelectorType
{

    @XmlElement(name = "Pair", required = true)
    protected List<StyleMapPairType> pair;

    /**
     * Gets the value of the pair property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pair property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPair().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StyleMapPairType }
     * 
     * 
     */
    public List<StyleMapPairType> getPair() {
        if (pair == null) {
            pair = new ArrayList<StyleMapPairType>();
        }
        return this.pair;
    }

}
