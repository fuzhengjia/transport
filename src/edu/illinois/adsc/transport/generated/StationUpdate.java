/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package edu.illinois.adsc.transport.generated;

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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-10-9")
public class StationUpdate implements org.apache.thrift.TBase<StationUpdate, StationUpdate._Fields>, java.io.Serializable, Cloneable, Comparable<StationUpdate> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("StationUpdate");

  private static final org.apache.thrift.protocol.TField STATION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("stationId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField UPDATE_MATRIX_FIELD_DESC = new org.apache.thrift.protocol.TField("updateMatrix", org.apache.thrift.protocol.TType.STRUCT, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new StationUpdateStandardSchemeFactory());
    schemes.put(TupleScheme.class, new StationUpdateTupleSchemeFactory());
  }

  public String stationId; // required
  public Matrix updateMatrix; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    STATION_ID((short)1, "stationId"),
    UPDATE_MATRIX((short)2, "updateMatrix");

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
        case 1: // STATION_ID
          return STATION_ID;
        case 2: // UPDATE_MATRIX
          return UPDATE_MATRIX;
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
    tmpMap.put(_Fields.STATION_ID, new org.apache.thrift.meta_data.FieldMetaData("stationId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.UPDATE_MATRIX, new org.apache.thrift.meta_data.FieldMetaData("updateMatrix", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Matrix.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(StationUpdate.class, metaDataMap);
  }

  public StationUpdate() {
  }

  public StationUpdate(
    String stationId,
    Matrix updateMatrix)
  {
    this();
    this.stationId = stationId;
    this.updateMatrix = updateMatrix;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public StationUpdate(StationUpdate other) {
    if (other.isSetStationId()) {
      this.stationId = other.stationId;
    }
    if (other.isSetUpdateMatrix()) {
      this.updateMatrix = new Matrix(other.updateMatrix);
    }
  }

  public StationUpdate deepCopy() {
    return new StationUpdate(this);
  }

  @Override
  public void clear() {
    this.stationId = null;
    this.updateMatrix = null;
  }

  public String getStationId() {
    return this.stationId;
  }

  public StationUpdate setStationId(String stationId) {
    this.stationId = stationId;
    return this;
  }

  public void unsetStationId() {
    this.stationId = null;
  }

  /** Returns true if field stationId is set (has been assigned a value) and false otherwise */
  public boolean isSetStationId() {
    return this.stationId != null;
  }

  public void setStationIdIsSet(boolean value) {
    if (!value) {
      this.stationId = null;
    }
  }

  public Matrix getUpdateMatrix() {
    return this.updateMatrix;
  }

  public StationUpdate setUpdateMatrix(Matrix updateMatrix) {
    this.updateMatrix = updateMatrix;
    return this;
  }

  public void unsetUpdateMatrix() {
    this.updateMatrix = null;
  }

  /** Returns true if field updateMatrix is set (has been assigned a value) and false otherwise */
  public boolean isSetUpdateMatrix() {
    return this.updateMatrix != null;
  }

  public void setUpdateMatrixIsSet(boolean value) {
    if (!value) {
      this.updateMatrix = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case STATION_ID:
      if (value == null) {
        unsetStationId();
      } else {
        setStationId((String)value);
      }
      break;

    case UPDATE_MATRIX:
      if (value == null) {
        unsetUpdateMatrix();
      } else {
        setUpdateMatrix((Matrix)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case STATION_ID:
      return getStationId();

    case UPDATE_MATRIX:
      return getUpdateMatrix();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case STATION_ID:
      return isSetStationId();
    case UPDATE_MATRIX:
      return isSetUpdateMatrix();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof StationUpdate)
      return this.equals((StationUpdate)that);
    return false;
  }

  public boolean equals(StationUpdate that) {
    if (that == null)
      return false;

    boolean this_present_stationId = true && this.isSetStationId();
    boolean that_present_stationId = true && that.isSetStationId();
    if (this_present_stationId || that_present_stationId) {
      if (!(this_present_stationId && that_present_stationId))
        return false;
      if (!this.stationId.equals(that.stationId))
        return false;
    }

    boolean this_present_updateMatrix = true && this.isSetUpdateMatrix();
    boolean that_present_updateMatrix = true && that.isSetUpdateMatrix();
    if (this_present_updateMatrix || that_present_updateMatrix) {
      if (!(this_present_updateMatrix && that_present_updateMatrix))
        return false;
      if (!this.updateMatrix.equals(that.updateMatrix))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_stationId = true && (isSetStationId());
    list.add(present_stationId);
    if (present_stationId)
      list.add(stationId);

    boolean present_updateMatrix = true && (isSetUpdateMatrix());
    list.add(present_updateMatrix);
    if (present_updateMatrix)
      list.add(updateMatrix);

    return list.hashCode();
  }

  @Override
  public int compareTo(StationUpdate other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetStationId()).compareTo(other.isSetStationId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStationId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.stationId, other.stationId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetUpdateMatrix()).compareTo(other.isSetUpdateMatrix());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUpdateMatrix()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.updateMatrix, other.updateMatrix);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("StationUpdate(");
    boolean first = true;

    sb.append("stationId:");
    if (this.stationId == null) {
      sb.append("null");
    } else {
      sb.append(this.stationId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("updateMatrix:");
    if (this.updateMatrix == null) {
      sb.append("null");
    } else {
      sb.append(this.updateMatrix);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
    if (updateMatrix != null) {
      updateMatrix.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class StationUpdateStandardSchemeFactory implements SchemeFactory {
    public StationUpdateStandardScheme getScheme() {
      return new StationUpdateStandardScheme();
    }
  }

  private static class StationUpdateStandardScheme extends StandardScheme<StationUpdate> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, StationUpdate struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // STATION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.stationId = iprot.readString();
              struct.setStationIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // UPDATE_MATRIX
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.updateMatrix = new Matrix();
              struct.updateMatrix.read(iprot);
              struct.setUpdateMatrixIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, StationUpdate struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.stationId != null) {
        oprot.writeFieldBegin(STATION_ID_FIELD_DESC);
        oprot.writeString(struct.stationId);
        oprot.writeFieldEnd();
      }
      if (struct.updateMatrix != null) {
        oprot.writeFieldBegin(UPDATE_MATRIX_FIELD_DESC);
        struct.updateMatrix.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class StationUpdateTupleSchemeFactory implements SchemeFactory {
    public StationUpdateTupleScheme getScheme() {
      return new StationUpdateTupleScheme();
    }
  }

  private static class StationUpdateTupleScheme extends TupleScheme<StationUpdate> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, StationUpdate struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetStationId()) {
        optionals.set(0);
      }
      if (struct.isSetUpdateMatrix()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetStationId()) {
        oprot.writeString(struct.stationId);
      }
      if (struct.isSetUpdateMatrix()) {
        struct.updateMatrix.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, StationUpdate struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.stationId = iprot.readString();
        struct.setStationIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.updateMatrix = new Matrix();
        struct.updateMatrix.read(iprot);
        struct.setUpdateMatrixIsSet(true);
      }
    }
  }

}

