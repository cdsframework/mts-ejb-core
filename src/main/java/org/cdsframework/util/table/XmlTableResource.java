//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.30 at 02:36:30 PM MST 
//


package org.cdsframework.util.table;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for xmlTable complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="xmlTable"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{org.cdsframework.util.table}tableName"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}databaseId"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}registerStandardDMLInterfaces" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}create" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}createVersion" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}drop"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}primaryKeyConstraint" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}foreignKeyConstraint" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}otherConstraint" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}index" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}alterOperation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}select"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}selectByPrimaryKey"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}orderBy"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}insert"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}update"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}delete"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}queryOperation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{org.cdsframework.util.table}initialInsert" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xmlTable", propOrder = {
    "tableName",
    "databaseId",
    "registerStandardDMLInterfaces",
    "creates",
    "createVersion",
    "drop",
    "primaryKeyConstraint",
    "foreignKeyConstraints",
    "otherConstraints",
    "indices",
    "alterOperations",
    "select",
    "selectByPrimaryKey",
    "orderBy",
    "insert",
    "update",
    "delete",
    "queryOperations",
    "initialInserts"
})
@XmlRootElement(name = "xmlTableResource")
public class XmlTableResource
    implements Serializable
{

    private final static long serialVersionUID = -2458734520159483988L;
    @XmlElement(required = true)
    protected String tableName;
    @XmlElement(required = true)
    protected String databaseId;
    @XmlElement(defaultValue = "false")
    protected Boolean registerStandardDMLInterfaces;
    protected BigInteger createVersion;
    @XmlElement(required = true)
    protected String drop;
    protected String primaryKeyConstraint;
    @XmlElement(name = "otherConstraint")
    protected List<String> otherConstraints;
    @XmlElement(name = "index")
    protected List<String> indices;
    @XmlElement(required = true)
    protected SelectElement select;
    @XmlElement(required = true)
    protected String selectByPrimaryKey;
    @XmlElement(required = true)
    protected String orderBy;
    @XmlElement(required = true)
    protected String insert;
    @XmlElement(required = true)
    protected String update;
    @XmlElement(required = true)
    protected String delete;
    @XmlElement(name = "queryOperation")
    protected List<QueryOperation> queryOperations;
    @XmlElement(name = "initialInsert")
    protected List<String> initialInserts;

    /**
     * Gets the value of the tableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableName(String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the databaseId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatabaseId() {
        return databaseId;
    }

    /**
     * Sets the value of the databaseId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatabaseId(String value) {
        this.databaseId = value;
    }

    /**
     * Gets the value of the registerStandardDMLInterfaces property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRegisterStandardDMLInterfaces() {
        return registerStandardDMLInterfaces;
    }

    /**
     * Sets the value of the registerStandardDMLInterfaces property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRegisterStandardDMLInterfaces(Boolean value) {
        this.registerStandardDMLInterfaces = value;
    }

    /**
     * Gets the value of the createVersion property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCreateVersion() {
        return createVersion;
    }

    /**
     * Sets the value of the createVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCreateVersion(BigInteger value) {
        this.createVersion = value;
    }

    /**
     * Gets the value of the drop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDrop() {
        return drop;
    }

    /**
     * Sets the value of the drop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDrop(String value) {
        this.drop = value;
    }

    /**
     * Gets the value of the primaryKeyConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrimaryKeyConstraint() {
        return primaryKeyConstraint;
    }

    /**
     * Sets the value of the primaryKeyConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrimaryKeyConstraint(String value) {
        this.primaryKeyConstraint = value;
    }

    /**
     * Gets the value of the otherConstraints property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherConstraints property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherConstraints().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOtherConstraints() {
        if (otherConstraints == null) {
            otherConstraints = new ArrayList<String>();
        }
        return this.otherConstraints;
    }

    /**
     * Gets the value of the indices property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the indices property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIndices().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getIndices() {
        if (indices == null) {
            indices = new ArrayList<String>();
        }
        return this.indices;
    }

    /**
     * Gets the value of the select property.
     * 
     * @return
     *     possible object is
     *     {@link SelectElement }
     *     
     */
    public SelectElement getSelect() {
        return select;
    }

    /**
     * Sets the value of the select property.
     * 
     * @param value
     *     allowed object is
     *     {@link SelectElement }
     *     
     */
    public void setSelect(SelectElement value) {
        this.select = value;
    }

    /**
     * Gets the value of the selectByPrimaryKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSelectByPrimaryKey() {
        return selectByPrimaryKey;
    }

    /**
     * Sets the value of the selectByPrimaryKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSelectByPrimaryKey(String value) {
        this.selectByPrimaryKey = value;
    }

    /**
     * Gets the value of the orderBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the value of the orderBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderBy(String value) {
        this.orderBy = value;
    }

    /**
     * Gets the value of the insert property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInsert() {
        return insert;
    }

    /**
     * Sets the value of the insert property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInsert(String value) {
        this.insert = value;
    }

    /**
     * Gets the value of the update property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpdate() {
        return update;
    }

    /**
     * Sets the value of the update property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdate(String value) {
        this.update = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelete() {
        return delete;
    }

    /**
     * Sets the value of the delete property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelete(String value) {
        this.delete = value;
    }

    /**
     * Gets the value of the queryOperations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the queryOperations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQueryOperations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QueryOperation }
     * 
     * 
     */
    public List<QueryOperation> getQueryOperations() {
        if (queryOperations == null) {
            queryOperations = new ArrayList<QueryOperation>();
        }
        return this.queryOperations;
    }

    /**
     * Gets the value of the initialInserts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the initialInserts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInitialInserts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getInitialInserts() {
        if (initialInserts == null) {
            initialInserts = new ArrayList<String>();
        }
        return this.initialInserts;
    }

}
