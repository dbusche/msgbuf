/*
 * Copyright (c) 2021 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.msgbuf.generator;

import static de.haumacher.msgbuf.generator.CodeConvention.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.haumacher.msgbuf.binary.DataType;
import de.haumacher.msgbuf.generator.ast.CustomType;
import de.haumacher.msgbuf.generator.ast.Definition;
import de.haumacher.msgbuf.generator.ast.EnumDef;
import de.haumacher.msgbuf.generator.ast.Field;
import de.haumacher.msgbuf.generator.ast.Flag;
import de.haumacher.msgbuf.generator.ast.MapType;
import de.haumacher.msgbuf.generator.ast.MessageDef;
import de.haumacher.msgbuf.generator.ast.Option;
import de.haumacher.msgbuf.generator.ast.PrimitiveType;
import de.haumacher.msgbuf.generator.ast.PrimitiveType.Kind;
import de.haumacher.msgbuf.generator.ast.QName;
import de.haumacher.msgbuf.generator.ast.StringOption;
import de.haumacher.msgbuf.generator.ast.Type;

/**
 * {@link Generator} for message data classes.
 */
public class MessageGenerator extends AbstractFileGenerator implements Type.Visitor<String, Boolean>, Definition.Visitor<Void, Void> {

	private final NameTable _table;
	private final MessageDef _def;
	private boolean _binary;
	private boolean _reflection;

	private Map<String, Option> _options;

	/** 
	 * Creates a {@link MessageGenerator}.
	 */
	public MessageGenerator(NameTable table, Map<String, Option> options, MessageDef def) {
		_table = table;
		_options = options;
		_def = def;
		_binary = !isTrue(options.get("NoBinary"), false);
		_reflection = !isTrue(options.get("NoReflection"), false);
	}
	
	private boolean isTrue(Option option, boolean defaultValue) {
		return option == null ? defaultValue : ((Flag) option).isValue();
	}

	/**
	 * Whether binary IO should be generated.
	 */
	public boolean isBinary() {
		return _binary;
	}
	
	/**
	 * @see #isBinary()
	 */
	public void setBinary(boolean binary) {
		_binary = binary;
	}
	
	/**
	 * Whether reflective access should be generated.
	 */
	public boolean isReflection() {
		return _reflection;
	}
	
	/**
	 * @see #isReflection()
	 */
	public void setReflection(boolean reflection) {
		_reflection = reflection;
	}

	@Override
	protected void generate() {
		String modifier;
		if (_def.getFile() != null) {
			QName packageName = _def.getFile().getPackage();
			if (packageName != null) {
				line("package " + packageName(packageName) + ";");
				nl();
			}
			modifier = "";
		} else {
			modifier = " static";
		}
		docComment(_def.getComment());
		line("public" + modifier + mkAbstract() + " class " + typeName(_def) + mkExtends() + " {");
		generateClassContents();
		nl();
		line("}");
	}
	
	private String mkAbstract() {
		return _def.isAbstract() ? " abstract" : "";
	}

	private String mkExtends() {
		if (_def.getExtends() == null) {
			return " extends " + (_reflection ? "de.haumacher.msgbuf.data.AbstractReflectiveDataObject" : "de.haumacher.msgbuf.data.AbstractDataObject") + 
					(_binary ? " implements " : "") + 
					(_binary ? "de.haumacher.msgbuf.binary.BinaryDataObject" : "")
				;
		} else {
			return " extends " + qTypeName(_def.getExtends());
		}
	}

	@Override
	protected void docComment(String comment) {
		Pattern ref = Pattern.compile("([a-zA-Z_][a-zA-Z_0-9]*)?#([a-zA-Z_][a-zA-Z_0-9]*)");
		Matcher matcher = ref.matcher(comment);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String type = matcher.group(1);
			String name = matcher.group(2);
			Definition def = _def;
			if (type != null) {
				def = _table.lookup(_def, qName(type));
			}
			Field field = def instanceof MessageDef ? getField((MessageDef) def, name) : null;
			String replacement = (type == null ? "" : type) + "#";
			if (field == null) {
				replacement += name;
			} else {
				replacement += getterCall(field);
			}
			
			matcher.appendReplacement(buffer, replacement);
		}
		matcher.appendTail(buffer);
		
		super.docComment(buffer.toString());
	}

	private Field getField(MessageDef def, String name) {
		Field result = getLocalField(def, name);
		if (result != null) {
			return result;
		}
		MessageDef extendedDef = def.getExtendedDef();
		if (extendedDef != null) {
			return getField(extendedDef, name);
		}
		return null;
	}

	private void generateClassContents() {
		generateVisitorInterface();
		generateInnerDefinitions();
		generateFactoryMethod();
		generateConstants();
		generateFieldMembers();
		generateConstructor();
		generateAccessors();
		
		if (_reflection) {
			generateReflection();
		}
		
		generateJson();
		
		if (_binary) {
			generateBinaryIO();
		}
		
		generateVisitMethods();
	}

	private void generateVisitorInterface() {
		if (_def.isAbstract()) {
			nl();
			line("/** Visitor interface for the {@link " + typeName(_def) + "} hierarchy.*/");
			lineStart("public interface Visitor<R,A>");
			boolean first = true;
			for (MessageDef specialization : _def.getSpecializations()) {
				if (!specialization.isAbstract()) {
					continue;
				}
				if (first) {
					first = false;
					append(" extends ");
				} else {
					append(", ");
				}
				append(typeName(specialization) + ".Visitor<R,A>");
			}
			append(" {");
			nl();
			{
				boolean hasCase = false;
				for (MessageDef specialization : _def.getSpecializations()) {
					if (specialization.isAbstract()) {
						continue;
					}
	
					nl();
					line("/** Visit case for {@link " + typeName(specialization) + "}.*/");
					line("R visit(" + typeName(specialization) + " self, A arg);");
					
					hasCase = true;
				}
				
				if (!hasCase) {
					nl();
					line("// Pure sum interface.");
				}
			}
			nl();
			line("}");
		}
	}

	private void generateInnerDefinitions() {
		for (Definition def : _def.getDefinitions()) {
			def.visit(this, null);
		}
	}

	private void generateFactoryMethod() {
		if (!_def.isAbstract()) {
			nl();
			line("/**");
			line(" * Creates a {@link " + typeName(_def) + "} instance.");
			line(" */");
			line("public static " + typeName(_def) + " " + factoryName(_def) + "() {");
			{
				line("return new " + typeName(_def) + "();");
			}
			line("}");
		}
	}

	private void generateConstants() {
		if (!_def.isAbstract()) {
			nl();
			line("/** Identifier for the {@link " + typeName(_def) + "} type in JSON format. */");
			line("public static final String " + jsonTypeConstant(_def) + " = " + jsonTypeID(_def) + ";");
		}
		
		for (Field field : getFields()) {
			nl();
			line("/** @see #" + getterCall(field) + " */");
			line("public static final String " + constant(field) + " = " + getFieldNameString(field) + ";");
		}
		
		if (_binary) {
			if (!_def.isAbstract() && getRoot(_def).isAbstract()) {
				nl();
				line("/** Identifier for the {@link " + typeName(_def) + "} type in binary format. */");
				line("public static final int " + mkBinaryTypeConstant(_def) + " = " + _def.getId() + ";");
			}
			
			for (Field field : getFields()) {
				if (field.isTransient()) {
					continue;
				}
				
				nl();
				line("/** Identifier for the property {@link #" + getterCall(field) + "} in binary format. */");
				line("public static final int " + binaryConstant(field) + " = " + field.getIndex() + ";");
			}
		}
	}

	private String getFieldNameString(Field field) {
		Optional<Option> fieldName = Optional.ofNullable(field.getOptions().get("Name"));
		String name = fieldName.isPresent() ? ((StringOption) fieldName.get()).getValue() : field.getName();
		return "\"" + name + "\"";
	}

	private void generateConstructor() {
		nl();
		line("/**");
		line(" * Creates a {@link " + typeName(_def) + "} instance.");
		if (!_def.isAbstract()) {
			line(" *");
			line(" * @see #" + factoryName(_def) + "()");
		}
		line(" */");
		line("protected " + typeName(_def) + "() {");
		{
			line("super();");
		}
		line("}");
	}

	private void generateFieldMembers() {
		for (Field field : getFields()) {
			nl();
			line("private" + mkTransient(field) + mkFinal(field) +  " " + mkType(field) + " " + "_" + name(field) + mkInitializer(field) + ";");
		}
	}

	private void generateAccessors() {
		for (Field field : getFields()) {
			generateAccessors(field);
		}
	}

	private void generateAccessors(Field field) {
		accessorGetter(field);
		accessorSetter(field);
		accessorAdder(field);
		accessorHasValue(field);
	}

	private void accessorGetter(Field field) {
		nl();
		docComment(field.getComment());
		line("public final " + mkType(field) + " " + getterName(field) + "()" + " {");
		line("return " + "_" + name(field) + ";");
		line("}");
	}

	private void accessorSetter(Field field) {
		nl();
		line("/**");
		line(" * @see #" + getterName(field) + "()");
		line(" */");
		line("public final " + typeName(_def) + " " + setterName(field) + "(" + mkType(field) + " " + "value" + ")" + " {");
		Type type = field.getType();
		{
			if (field.isRepeated()) {
				line("_" + name(field) + ".clear();");
				line("_" + name(field) + ".addAll(value);");
			} else if (type instanceof MapType) {
				line("_" + name(field) + ".clear();");
				line("_" + name(field) + ".putAll(value);");
			} else {
				line("_" + name(field) + " = " + "value" + ";");
			}
			line("return this;");
		}
		line("}");
	}

	private void accessorAdder(Field field) {
		Type type = field.getType();
		if (field.isRepeated()) {
			nl();
			line("/**");
			line(" * Adds a value to the {@link #" + getterName(field) + "()"+ "} list.");
			line(" */");
			line("public final " + typeName(_def) + " " + adderName(field) + "(" + mkType(type) + " " + "value" + ")" + " {");
			{
				line("_" + name(field) + ".add(value);");
				line("return this;");
			}
			line("}");
		}
		else if (type instanceof MapType) {
			MapType mapType = (MapType) type;
			nl();
			line("/**");
			line(" * Adds a value to the {@link #" + getterName(field) + "()"+ "} map.");
			line(" */");
			line("public final " + "void" + " " + adderName(field) + "(" + mkType(mapType.getKeyType()) + " key" + ", " + mkType(mapType.getValueType()) + " value" + ")" + " {");
			{
				line("if (" + "_" + name(field) + ".containsKey(key)) {");
				{
					line("throw new IllegalArgumentException(\"Property '" + field.getName() + "' already contains a value for key '\" + key + \"'.\");");
				}
				line("}");
				line("_" + name(field) + ".put(key, value);");
			}
			line("}");
		}
	}

	private void accessorHasValue(Field field) {
		if (isNullable(field)) {
			nl();
			line("/**");
			line(" * Checks, whether {@link #" + getterName(field) + "()"+ "} has a value.");
			line(" */");
			line("public final " + "boolean" + " " + hasName(field) + "()" + " {");
			{
				line("return _" + name(field) + " != null;");
			}
			line("}");
		}
	}

	private void generateReflection() {
		if (hasFields()) {
			reflectionPropertiesConstant();
			reflectionProperties();
			reflectionGet();
			reflectionSet();
		}
	}

	private void reflectionPropertiesConstant() {
		nl();
		line("private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(");
		{
			line("java.util.Arrays.asList(");
			{
				boolean first = true;
				for (Field field : getFields()) {
					if (first) {
						first = false;
					} else {
						append(", ");
						nl();
					}
					lineStart(constant(field));
				}
				append("));");
				nl();
			}
		}
	}

	private void reflectionProperties() {
		nl();
		line("@Override");
		line("public java.util.List<String> properties() {");
		{
			line("return PROPERTIES;");
		}
		line("}");
	}

	private void reflectionGet() {
		nl();
		line("@Override");
		line("public Object get(String field) {");
		{
			line("switch (field) {");
			for (Field field : getFields()) {
				line("case " + constant(field) + ": return " + getterName(field) + "()" + ";");
			}
			line("default: return super.get(field);");
			line("}");
		}
		line("}");
	}

	private void reflectionSet() {
		nl();
		line("@Override");
		line("public void set(String field, Object value) {");
		{
			{
				line("switch (field) {");
				for (Field field : getFields()) {
					line("case " + constant(field) + ": " + setterName(field) + "(" + mkCast(field, "value") + ")" + "; break;");
				}
				if (!isBaseClass()) {
					line("default: super.set(field, value); break;");
				}
				line("}");
			}
		}
		line("}");
	}

	private void generateJson() {
		nl();
		line("/** Reads a new instance from the given reader. */");
		line("public static " + typeName(_def) + " " + readerName(_def) + "(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {");
		{
			if (_def.isAbstract()) {
				line(typeName(_def) + " result;");
				line("in.beginArray();");
				line("String type = in.nextString();");
				line("switch (type) {");
				for (MessageDef specialization : getTransitiveSpecializations(_def)) {
					if (specialization.isAbstract()) {
						continue;
					}
					line("case " + jsonTypeConstantRef(specialization) + ": result = " + typeName(specialization) + "." + readerName(specialization) + "(in); break;");
				}
				line("default: in.skipValue(); result = null; break;");
				line("}");
				line("in.endArray();");
			} else {
				line(typeName(_def) + " result = new " + typeName(_def) + "();");
				line("in.beginObject();");
				line("result.readFields(in);");
				line("in.endObject();");
			}
			line("return result;");
		}
		line("}");
		
		if (isBaseClass()) {
			nl();
			line("@Override");
			line("public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {");
			if (_def.isAbstract()) {
				line("out.beginArray();");
				line("out.value(jsonType());");
				line("writeContent(out);");
				line("out.endArray();");
			} else {
				line("writeContent(out);");
			}
			line("}");
			
			if (_def.isAbstract()) {
				nl();
				line("/** The type identifier for this concrete subtype. */");
				line("public abstract String jsonType();");
			}
		}
		
		if (_def.getExtendedDef() != null && !_def.isAbstract()) {
			nl();
			line("@Override");
			line("public String jsonType() {");
			{
				line("return " + jsonTypeConstant(_def) + ";");
			}
			line("}");
		}
		
		if (hasFields()) {
			nl();
			line("@Override");
			line("protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {");
			{
				line("super.writeFields(out);");
				for (Field field : getFields()) {
					if (field.isTransient()) {
						continue;
					}
					boolean nullable = isNullable(field);
					if (nullable) {
						line("if (" + hasName(field) + "()" + ") {");
					}
					line("out.name(" + constant(field) + ");");
					if (field.isRepeated()) {
						line("out.beginArray();");
						line("for (" + mkType(field.getType()) +" x : " + getterName(field) + "()" + ") {");
						{
							jsonOutValue(field.getType(), "x");
						}
						line("}");
						line("out.endArray();");
					} else {
						jsonOutValue(field.getType(), getterCall(field));
					}
					if (nullable) {
						line("}");
					}
				}
			}
			line("}");
		
			nl();
			line("@Override");
			line("protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {");
			{
				line("switch (field) {");
				for (Field field : getFields()) {
					if (field.isTransient()) {
						continue;
					}
					jsonReadField(field);
				}
				line("default: super.readField(in, field);");
				line("}");
			}
			line("}");
		}
	}

	private void jsonReadField(Field field) {
		Type type = field.getType();
		if (field.isRepeated()) {
			line("case " + constant(field) + ": {");
			{
				line("in.beginArray();");
				line("while (in.hasNext()) {");
				{
					line(adderName(field) + "(" + jsonReadEntry(type) + ");");
				}
				line("}");
				line("in.endArray();");
			}
			line("}");
			line("break;");
		} else if (type instanceof MapType) {
			MapType mapType = (MapType) type;
			line("case " + constant(field) + ": {");
			{
				Type keyType = mapType.getKeyType();
				Type valueType = mapType.getValueType();
				if (keyType instanceof PrimitiveType && ((PrimitiveType) keyType).getKind() == Kind.STRING) {
					line("in.beginObject();");
					line("while (in.hasNext()) {");
					{
						line(adderName(field) + "(in.nextName(), " + jsonReadEntry(valueType) + ");");
					}
					line("}");
					line("in.endObject();");
				} else {
					line("in.beginArray();");
					line("while (in.hasNext()) {");
					{
						line("in.beginObject();");
						line(mkType(keyType) + " key = " + mkDefaultValue(keyType) + ";");
						line(mkType(valueType) + " value = " + mkDefaultValue(valueType) + ";");
						line("while (in.hasNext()) {");
						{
							line("switch (in.nextName()) {");
							line("case \"key\": key = " + jsonReadEntry(keyType) + "; break;");
							line("case \"value\": value = " + jsonReadEntry(valueType) + "; break;");
							line("default: in.skipValue(); break;");
							line("}");
						}
						line("}");
						line(adderName(field) + "(key, value);");
						line("in.endObject();");
					}
					line("}");
					line("in.endArray();");
				}
			}
			line("break;");
			line("}");
		} else {
			line("case " + constant(field) + ": " + setterName(field) + "(" + jsonReadEntry(type) + "); break;");
		}
	}

	private String jsonReadEntry(Type type) {
		if (type instanceof PrimitiveType) {
			return jsonType(((PrimitiveType) type).getKind());
		}
		else if (type instanceof CustomType) {
			CustomType messageType = (CustomType) type;
			QName name = messageType.getName();
			return qTypeName(name) + "." + readerName(Util.last(name)) +  "(in)";
		}
		throw new RuntimeException("Unsupported: " + type);
	}

	private String jsonTypeID(MessageDef def) {
		Option nameOption = def.getOptions().get("Name");
		String typeId = nameOption == null ? def.getName() : ((StringOption) nameOption).getValue();
		return "\"" + typeId + "\"";
	}

	private String jsonType(PrimitiveType.Kind primitive) {
		switch (primitive) {
		case BOOL: 
			return "in.nextBoolean()";
	
		case FLOAT:
			return "(float) in.nextDouble()";
		case DOUBLE: 
			return "in.nextDouble()";
		
		case INT32:
		case SINT32:
		case UINT32:
		case FIXED32: 
		case SFIXED32:
			return "in.nextInt()";
		
		case INT64:
		case SINT64:
		case UINT64:
		case FIXED64: 
		case SFIXED64: 
			return "in.nextLong()";
		
		case STRING:
			return "de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)";
			
		case BYTES:
			return "de.haumacher.msgbuf.json.JsonUtil.nextBinaryOptional(in)";
		}			
		
		throw new RuntimeException("No such type: " + primitive);
	}

	private void jsonOutValue(Type type, String x) {
		jsonOutValue(type, x, 0);
	}

	private void jsonOutValue(Type type, String x, int depth) {
		if (type instanceof PrimitiveType) {
			switch (((PrimitiveType) type).getKind()) {
			case BYTES: {
				line("de.haumacher.msgbuf.json.JsonUtil.writeBinaryOptional(out, " + x + ");");
				break;
			}
			default: {
				line("out.value(" + x + ");");
				break;
			}
			}
		} else if (type instanceof CustomType) {
			CustomType customType = (CustomType) type;
			if (isMonomorphicReferenceToTypeInPolymorphicHierarchy(customType)) { 
				line(x + ".writeContent(out);");
			} else {
				line(x + ".writeTo(out);");
			}
		} else if (type instanceof MapType) {
			MapType mapType = (MapType) type;
			
			Type keyType = mapType.getKeyType();
			Type valueType = mapType.getValueType();
			if (keyType instanceof PrimitiveType && ((PrimitiveType) keyType).getKind() == Kind.STRING) {
				line("out.beginObject();");
				String entry = "entry";
				if (depth > 0) {
					entry += depth;
				}
				line("for (" + mkEntryType(mapType) + " " + entry + " : " + x + ".entrySet()) {");
				{
					line("out.name(" + entry + ".getKey()" + ");");
					jsonOutValue(valueType, entry + ".getValue()", depth + 1);
				}
				line("}");
				line("out.endObject();");
			} else {
				line("out.beginArray();");
				String entry = "entry";
				if (depth > 0) {
					entry += depth;
				}
				line("for (" + mkEntryType(mapType) + " " + entry + " : " + x + ".entrySet()) {");
				{
					line("out.beginObject();");
					line("out.name(\"key\");");
					jsonOutValue(keyType, entry + ".getKey()", depth + 1);
					line("out.name(\"value\");");
					jsonOutValue(valueType, entry + ".getValue()", depth + 1);
					line("out.endObject();");
				}
				line("}");
				line("out.endArray();");
			}
		} else {
			throw new RuntimeException("Unsupported: " + type);
		}
	}

	private String mkEntryType(MapType mapType) {
		return "java.util.Map.Entry" + "<" + mkTypeWrapped(mapType.getKeyType()) + "," + mkTypeWrapped(mapType.getValueType()) + ">";
	}

	private void generateBinaryIO() {
		binaryTypeId();
		binaryWrite();
		binaryRead();
	}

	private void binaryTypeId() {
		if (isBaseClass()) {
			if (_def.isAbstract()) {
				nl();
				line("/** The binary identifier for this concrete type in the polymorphic {@link " + typeName(_def) + "} hierarchy. */");
				line("public abstract int typeId();");
			}
		} else {
			if (!_def.isAbstract() && getRoot(_def).isAbstract()) {
				nl();
				line("@Override");
				line("public int typeId() {");
				{
					line("return " + mkBinaryTypeConstant(_def) + ";");				
				}
				line("}");
			}
		}
	}

	private void binaryWrite() {
		if (isBaseClass()) {
			nl();
			line("@Override");
			line("public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {");
			{
				line("out.beginObject();");
				if (_def.isAbstract()) {
					line("out.name(0);");
					line("out.value(typeId());");
				}
				line("writeFields(out);");
				line("out.endObject();");
			}
			line("}");
		}

		if (isBaseClass() || hasFields()) {
			nl();
			if (isBaseClass()) {
				line("/**");
				line(" * Serializes all fields of this instance to the given binary output.");
				line(" *");
				line(" * @param out");
				line(" *        The binary output to write to.");
				line(" * @throws java.io.IOException If writing fails.");
				line(" */");
			} else {
				line("@Override");
			}
			line("protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {");
			{
				if (!isBaseClass()) {
					line("super.writeFields(out);");
				}
				if (getFields().isEmpty()) {
					line("// No fields to write, hook for subclasses.");
				} else {
					for (Field field : getFields()) {
						if (field.isTransient()) {
							continue;
						}
						boolean nullable = isNullable(field);
						if (nullable) {
							line("if (" + hasName(field) + "()" + ") {");
						}
						{
							line("out.name(" + binaryConstant(field) + ");");
							if (field.isRepeated()) {
								line("{");
								{
									line(mkType(field) + " values = " + getterName(field) + "();");
									line("out.beginArray(" + "de.haumacher.msgbuf.binary.DataType." + mkBinaryType(field.getType()) + ", values.size());");
									line("for (" + mkType(field.getType()) +" x : values) {");
									{
										binaryWriteValue(field.getType(), "x");
									}
									line("}");
									line("out.endArray();");
								}
								line("}");
							} else {
								binaryWriteValue(field.getType(), getterCall(field));
							}
						}
						if (nullable) {
							line("}");
						}
					}
				}
			}
			line("}");
		}
	}

	private void binaryWriteValue(Type type, String x) {
		if (type instanceof PrimitiveType) {
			line("out.value(" + x + ");");
		} else if (type instanceof CustomType) {
			line(x + ".writeTo(out);");
		} else {
			// TODO
		}
	}

	private void binaryRead() {
		nl();
		line("/** Reads a new instance from the given reader. */");
		line("public static " + typeName(_def) + " " + readerName(_def) + "(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {");
		{
			line("in.beginObject();");
			if (_def.isAbstract()) {
				line(typeName(_def) + " result;");
				line("int typeField = in.nextName();");
				line("assert typeField == 0;");
				line("int type = in.nextInt();");
				line("switch (type) {");
				for (MessageDef specialization : getTransitiveSpecializations(_def)) {
					if (specialization.isAbstract()) {
						continue;
					}
					line("case " + mkBinaryTypeConstantRef(specialization) + ": result = " + typeName(specialization) + "." + factoryName(specialization) + "(); break;");
				}
				line("default: while (in.hasNext()) {in.skipValue(); } in.endObject(); return null;");
				line("}");
			} else {
				line(typeName(_def) + " result = new " + typeName(_def) + "();");
			}
			
			line("while (in.hasNext()) {");
			{
				line("int field = in.nextName();");
				line("result.readField(in, field);");
			}
			line("}");
	
			line("in.endObject();");
			line("return result;");
		}
		line("}");
		
		if (isBaseClass() || hasFields()) {
			nl();
			if (isBaseClass()) {
				line("/** Consumes the value for the field with the given ID and assigns its value. */");
			} else {
				line("@Override");
			}
			line("protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {");
			{
				line("switch (field) {");
				for (Field field : getFields()) {
					if (field.isTransient()) {
						continue;
					}
					binaryReadField(field);
				}
				if (isBaseClass()) {
					line("default: in.skipValue(); ");
				} else {
					line("default: super.readField(in, field);");
				}
				line("}");
			}
			line("}");
		}
	}

	private void binaryReadField(Field field) {
		Type type = field.getType();
		if (field.isRepeated()) {
			line("case " + binaryConstant(field) + ": {");
			{
				line("in.beginArray();");
				line("while (in.hasNext()) {");
				{
					line(adderName(field) + "(" + binaryReadEntry(type) + ");");
				}
				line("}");
				line("in.endArray();");
			}
			line("}");
			line("break;");
		} else if (type instanceof MapType) {
			MapType mapType = (MapType) type;
			line("case " + binaryConstant(field) + ": {");
			{
				Type keyType = mapType.getKeyType();
				Type valueType = mapType.getValueType();
				
				line("in.beginArray();");
				line("while (in.hasNext()) {");
				{
					line("in.beginObject();");
					line(mkType(keyType) + " key = " + mkDefaultValue(keyType) + ";");
					line(mkType(valueType) + " value = " + mkDefaultValue(valueType) + ";");
					line("while (in.hasNext()) {");
					{
						line("switch (in.nextName()) {");
						line("case 1: key = " + binaryReadEntry(keyType) + "; break;");
						line("case 2: value = " + binaryReadEntry(valueType) + "; break;");
						line("default: in.skipValue(); break;");
						line("}");
					}
					line("}");
					line(adderName(field) + "(key, value);");
					line("in.endObject();");
				}
				line("}");
				line("in.endArray();");
			}
			line("break;");
			line("}");
		} else {
			line("case " + binaryConstant(field) + ": " + setterName(field) + "(" + binaryReadEntry(type) + "); break;");
		}
	}

	private String binaryReadEntry(Type type) {
		if (type instanceof PrimitiveType) {
			return mkBinaryReadValue(((PrimitiveType) type).getKind());
		}
		else if (type instanceof CustomType) {
			CustomType messageType = (CustomType) type;
			QName name = messageType.getName();
			return qTypeName(name) + "." + readerName(Util.last(name)) +  "(in)";
		}
		throw new RuntimeException("Unsupported: " + type);
	}

	private String mkBinaryTypeConstantRef(MessageDef def) {
		return typeName(def) + "." + mkBinaryTypeConstant(def);
	}

	private String mkBinaryReadValue(Kind kind) {
		switch (kind) {
		case BOOL: 
			return "in.nextBoolean()";
		case FLOAT:
			return "in.nextFloat()";
		case DOUBLE: 
			return "in.nextDouble()";
		case INT32:
		case UINT32:
			return "in.nextInt()";
		case SINT32:
			return "in.nextIntSigned()";
		case FIXED32: 
		case SFIXED32:
			return "in.nextIntFixed()";
		
		case INT64:
		case UINT64:
			return "in.nextLong()";
			
		case SINT64:
			return "in.nextLongSigned()";
	
		case FIXED64: 
		case SFIXED64: 
			return "in.nextLongFixed()";
		
		case STRING:
			return "in.nextString()";
			
		case BYTES:
			return "in.nextBinary()";
		}			
		
		throw new RuntimeException("No such type: " + kind);
	}

	private DataType mkBinaryType(Type type) {
		if (type instanceof PrimitiveType) {
			return mkBinaryType(((PrimitiveType) type).getKind());
		} else if (type instanceof CustomType) {
			Definition definition = ((CustomType) type).getDefinition();
			if (definition instanceof EnumDef) {
				return DataType.INT; 
			} else {
				return DataType.OBJECT;
			}
		} else {
			return DataType.OBJECT;
		}
	}

	private DataType mkBinaryType(PrimitiveType.Kind primitive) {
		switch (primitive) {
		case BOOL: 
			return DataType.INT;
	
		case FLOAT:
			return DataType.FLOAT;
		case DOUBLE: 
			return DataType.DOUBLE;
		
		case INT32:
		case UINT32:
			return DataType.INT;
		case SINT32:
			return DataType.SINT;
		case FIXED32: 
		case SFIXED32:
			return DataType.FINT;
		
		case INT64:
		case UINT64:
			return DataType.LONG;
		case SINT64:
			return DataType.SLONG;
		case FIXED64: 
		case SFIXED64: 
			return DataType.FLONG;
		
		case STRING:
			return DataType.STRING;
			
		case BYTES:
			return DataType.BINARY;
		}			
		
		throw new RuntimeException("No such type: " + primitive);
	}

	private void generateVisitMethods() {
		if (_def.isAbstract()) {
			nl();
			line("/** Accepts the given visitor. */");
			line("public abstract <R,A> R visit(Visitor<R,A> v, A arg);");
			nl();
		}

		MessageDef gen = getAbstractGeneralization();
		if (gen != null) {
			nl();
			line("@Override");
			line("public" + (_def.isAbstract() ? " final" : "") + " <R,A> R visit(" + typeName(gen) + ".Visitor<R,A> v, A arg) {");
			{
				if (_def.isAbstract()) {
					line("return visit((Visitor<R,A>) v, arg);");
				} else {
					line("return v.visit(this, arg);");
				}
			}
			line("}");
		}
	}

	private MessageDef getAbstractGeneralization() {
		MessageDef current = _def;
		while (true) {
			MessageDef extendsDef = current.getExtendedDef();
			if (extendsDef == null) {
				return null;
			}
			if (extendsDef.isAbstract()) {
				return extendsDef;
			}
			current = extendsDef;
		}
	}

	private boolean hasFields() {
		return !getFields().isEmpty();
	}

	private List<Field> getFields() {
		return _def.getFields();
	}

	private boolean isBaseClass() {
		boolean baseClass = _def.getExtends() == null;
		return baseClass;
	}

	private static MessageDef getRoot(MessageDef def) {
		MessageDef extendedDef = def.getExtendedDef();
		if (extendedDef == null) {
			return def;
		} else {
			return getRoot(extendedDef);
		}
	}

	private List<MessageDef> getTransitiveSpecializations(MessageDef def) {
		ArrayList<MessageDef> result = new ArrayList<MessageDef>(def.getSpecializations());
		int n = 0;
		while (n < result.size()) {
			result.addAll(result.get(n++).getSpecializations());
		}
		return result;
	}

	private boolean isMonomorphicReferenceToTypeInPolymorphicHierarchy(CustomType customType) {
		Definition definition = customType.getDefinition();
		if (definition instanceof MessageDef) {
			MessageDef messageDef = (MessageDef) definition;
			
			if (messageDef.getExtendedDef() != null && !messageDef.isAbstract()) {
				return true;
			}
		}
		return false;
	}

	private String mkCast(Field field, String var) {
		return "(" + mkType(field) + ") " + var;
	}

	@Override
	public Void visit(EnumDef def, Void arg) {
		new EnumGenerator(def).generateInner(this);
		return null;
	}
	
	@Override
	public Void visit(MessageDef def, Void arg) {
		new MessageGenerator(_table, _options, def).generateInner(this);
		return null;
	}

	private String mkTransient(Field field) {
		return field.isTransient() ? " transient" : "";
	}

	private String mkFinal(Field field) {
		return field.isRepeated() ? " final" : "";
	}
	
	private String mkInitializer(Field field) {
		return " = " + mkDefaultValue(field);
	}
	
	private String mkDefaultValue(Field field) {
		if (field.isRepeated()) {
			return "new java.util.ArrayList<>()";
		} else {
			return mkDefaultValue(field.getType());
		}
	}
	
	static final Type.Visitor<String, Void> DEFAULT_VALUE = new Type.Visitor<String, Void>() {

		@Override
		public String visit(CustomType self, Void arg) {
			Definition definition = self.getDefinition();
			if (definition instanceof EnumDef) {
				return typeName(definition) + "." + ((EnumDef) definition).getConstants().get(0).getName();
			} else {
				return "null";
			}
		}

		@Override
		public String visit(PrimitiveType self, Void arg) {
			switch (self.getKind()) {
			case BOOL: return "false";
			case BYTES: return "null";
			case DOUBLE: return "0.0d";
			case FLOAT: return "0.0f";
			case STRING: return "\"\"";
			case INT32:
			case SINT32:
			case UINT32:
			case FIXED32: 
			case SFIXED32:
				return "0";
			case INT64:
			case SINT64:
			case UINT64:
			case FIXED64:
			case SFIXED64:
				return "0L";
			}
			throw new UnsupportedOperationException("Unsupported type: " + self.getKind());
		}

		@Override
		public String visit(MapType self, Void arg) {
			return "new java.util.HashMap<>()";
		}
	};
	
	private String mkDefaultValue(Type type) {
		return type.visit(DEFAULT_VALUE, null);
	}

	private String mkType(Field field) {
		return field.isRepeated() ? "java.util.List<" + mkTypeWrapped(field.getType()) + ">" : mkType(field.getType());
	}

	private String mkType(Type type) {
		return type.visit(this, Boolean.FALSE);
	}
	
	private String mkTypeWrapped(Type type) {
		return type.visit(this, Boolean.TRUE);
	}
	
	@Override
	public String visit(MapType type, Boolean wrapped) {
		return "java.util.Map<" + mkTypeWrapped(type.getKeyType()) + ", " + mkTypeWrapped(type.getValueType()) + ">";
	}
	
	@Override
	public String visit(CustomType type, Boolean wrapped) {
		return qTypeName(type.getName());
	}
	
	@Override
	public String visit(PrimitiveType type, Boolean wrapped) {
		switch (type.getKind()) {
		case BOOL:
			return wrapped ? "Boolean" : "boolean";
		case BYTES:
			return "byte[]";
		case FLOAT:
			return wrapped ? "Float" : "float";
		case DOUBLE:
			return wrapped ? "Double" : "double";
		case INT32:
		case UINT32:
		case FIXED32:
		case SINT32:
		case SFIXED32:
			return wrapped ? "Integer" : "int";
		case INT64:
		case UINT64:
		case FIXED64:
		case SINT64:
		case SFIXED64:
			return wrapped ? "Long" : "long";
		case STRING:
			return "String";
		}
		throw new RuntimeException("No such type: " + type.getKind());
	}

	static QName qName(String name) {
		QName result = QName.create();
		for (String part : name.split("\\.")) {
			result.addName(part);
		}
		return result;
	}

	static String getterCall(Field field) {
		return getterName(field) + "()";
	}

	static Field getLocalField(MessageDef def, String name) {
		return def.getFields().stream().filter(f -> name.equals(f.getName())).findFirst().orElse(null);
	}

	private static boolean isNullable(Field field) {
		return field.getType() instanceof CustomType && !field.isRepeated();
	}
	

}
