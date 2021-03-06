/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.lsh.service.driver.recommend;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-12-06")
public class OutputDriverRec implements org.apache.thrift.TBase<OutputDriverRec, OutputDriverRec._Fields>, java.io.Serializable, Cloneable, Comparable<OutputDriverRec> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("OutputDriverRec");

  private static final org.apache.thrift.protocol.TField RECOMMEND_DRIVER_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("recommendDriverList", org.apache.thrift.protocol.TType.LIST, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new OutputDriverRecStandardSchemeFactory());
    schemes.put(TupleScheme.class, new OutputDriverRecTupleSchemeFactory());
  }

  public List<DriverItem> recommendDriverList; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    RECOMMEND_DRIVER_LIST((short)1, "recommendDriverList");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // RECOMMEND_DRIVER_LIST
          return RECOMMEND_DRIVER_LIST;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.RECOMMEND_DRIVER_LIST, new org.apache.thrift.meta_data.FieldMetaData("recommendDriverList", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, DriverItem.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(OutputDriverRec.class, metaDataMap);
  }

  public OutputDriverRec() {
  }

  public OutputDriverRec(
    List<DriverItem> recommendDriverList)
  {
    this();
    this.recommendDriverList = recommendDriverList;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public OutputDriverRec(OutputDriverRec other) {
    if (other.isSetRecommendDriverList()) {
      List<DriverItem> __this__recommendDriverList = new ArrayList<DriverItem>(other.recommendDriverList.size());
      for (DriverItem other_element : other.recommendDriverList) {
        __this__recommendDriverList.add(new DriverItem(other_element));
      }
      this.recommendDriverList = __this__recommendDriverList;
    }
  }

  public OutputDriverRec deepCopy() {
    return new OutputDriverRec(this);
  }

  @Override
  public void clear() {
    this.recommendDriverList = null;
  }

  public int getRecommendDriverListSize() {
    return (this.recommendDriverList == null) ? 0 : this.recommendDriverList.size();
  }

  public java.util.Iterator<DriverItem> getRecommendDriverListIterator() {
    return (this.recommendDriverList == null) ? null : this.recommendDriverList.iterator();
  }

  public void addToRecommendDriverList(DriverItem elem) {
    if (this.recommendDriverList == null) {
      this.recommendDriverList = new ArrayList<DriverItem>();
    }
    this.recommendDriverList.add(elem);
  }

  public List<DriverItem> getRecommendDriverList() {
    return this.recommendDriverList;
  }

  public OutputDriverRec setRecommendDriverList(List<DriverItem> recommendDriverList) {
    this.recommendDriverList = recommendDriverList;
    return this;
  }

  public void unsetRecommendDriverList() {
    this.recommendDriverList = null;
  }

  /** Returns true if field recommendDriverList is set (has been assigned a value) and false otherwise */
  public boolean isSetRecommendDriverList() {
    return this.recommendDriverList != null;
  }

  public void setRecommendDriverListIsSet(boolean value) {
    if (!value) {
      this.recommendDriverList = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case RECOMMEND_DRIVER_LIST:
      if (value == null) {
        unsetRecommendDriverList();
      } else {
        setRecommendDriverList((List<DriverItem>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case RECOMMEND_DRIVER_LIST:
      return getRecommendDriverList();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case RECOMMEND_DRIVER_LIST:
      return isSetRecommendDriverList();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof OutputDriverRec)
      return this.equals((OutputDriverRec)that);
    return false;
  }

  public boolean equals(OutputDriverRec that) {
    if (that == null)
      return false;

    boolean this_present_recommendDriverList = true && this.isSetRecommendDriverList();
    boolean that_present_recommendDriverList = true && that.isSetRecommendDriverList();
    if (this_present_recommendDriverList || that_present_recommendDriverList) {
      if (!(this_present_recommendDriverList && that_present_recommendDriverList))
        return false;
      if (!this.recommendDriverList.equals(that.recommendDriverList))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_recommendDriverList = true && (isSetRecommendDriverList());
    list.add(present_recommendDriverList);
    if (present_recommendDriverList)
      list.add(recommendDriverList);

    return list.hashCode();
  }

  @Override
  public int compareTo(OutputDriverRec other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetRecommendDriverList()).compareTo(other.isSetRecommendDriverList());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRecommendDriverList()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.recommendDriverList, other.recommendDriverList);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("OutputDriverRec(");
    boolean first = true;

    sb.append("recommendDriverList:");
    if (this.recommendDriverList == null) {
      sb.append("null");
    } else {
      sb.append(this.recommendDriverList);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
    if (recommendDriverList == null) {
      throw new TProtocolException("Required field 'recommendDriverList' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class OutputDriverRecStandardSchemeFactory implements SchemeFactory {
    public OutputDriverRecStandardScheme getScheme() {
      return new OutputDriverRecStandardScheme();
    }
  }

  private static class OutputDriverRecStandardScheme extends StandardScheme<OutputDriverRec> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, OutputDriverRec struct) throws TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // RECOMMEND_DRIVER_LIST
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list16 = iprot.readListBegin();
                struct.recommendDriverList = new ArrayList<DriverItem>(_list16.size);
                DriverItem _elem17;
                for (int _i18 = 0; _i18 < _list16.size; ++_i18)
                {
                  _elem17 = new DriverItem();
                  _elem17.read(iprot);
                  struct.recommendDriverList.add(_elem17);
                }
                iprot.readListEnd();
              }
              struct.setRecommendDriverListIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, OutputDriverRec struct) throws TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.recommendDriverList != null) {
        oprot.writeFieldBegin(RECOMMEND_DRIVER_LIST_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.recommendDriverList.size()));
          for (DriverItem _iter19 : struct.recommendDriverList)
          {
            _iter19.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class OutputDriverRecTupleSchemeFactory implements SchemeFactory {
    public OutputDriverRecTupleScheme getScheme() {
      return new OutputDriverRecTupleScheme();
    }
  }

  private static class OutputDriverRecTupleScheme extends TupleScheme<OutputDriverRec> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, OutputDriverRec struct) throws TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      {
        oprot.writeI32(struct.recommendDriverList.size());
        for (DriverItem _iter20 : struct.recommendDriverList)
        {
          _iter20.write(oprot);
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, OutputDriverRec struct) throws TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      {
        org.apache.thrift.protocol.TList _list21 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.recommendDriverList = new ArrayList<DriverItem>(_list21.size);
        DriverItem _elem22;
        for (int _i23 = 0; _i23 < _list21.size; ++_i23)
        {
          _elem22 = new DriverItem();
          _elem22.read(iprot);
          struct.recommendDriverList.add(_elem22);
        }
      }
      struct.setRecommendDriverListIsSet(true);
    }
  }

}

