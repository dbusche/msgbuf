/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.msgbuf.generator.plugins;

import static de.haumacher.msgbuf.generator.CodeConvention.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.haumacher.msgbuf.generator.AbstractMessageGenerator;
import de.haumacher.msgbuf.generator.GeneratorPlugin;
import de.haumacher.msgbuf.generator.ast.CustomType;
import de.haumacher.msgbuf.generator.ast.Definition;
import de.haumacher.msgbuf.generator.ast.Field;
import de.haumacher.msgbuf.generator.ast.MessageDef;
import de.haumacher.msgbuf.generator.ast.Option;
import de.haumacher.msgbuf.generator.ast.PrimitiveType;
import de.haumacher.msgbuf.generator.ast.StringOption;
import de.haumacher.msgbuf.generator.ast.Type;
import de.haumacher.msgbuf.generator.common.Util;
import de.haumacher.msgbuf.generator.util.CodeUtil;
import de.haumacher.msgbuf.generator.util.FileGenerator;

/**
 * {@link GeneratorPlugin} generating XML reading code for the {@link javax.xml.stream.XMLStreamReader} interface.
 */
public class XmlStreamingPlugin implements GeneratorPlugin {
	
	@Override
	public FileGenerator messageInterfaceContents(Map<String, Option> options, MessageDef def) {
		if (noXml(options)) {
			return GeneratorPlugin.super.messageInterfaceContents(options, def);
		}

		return new AbstractMessageGenerator(options) {
			@Override
			protected void generate() {
				nl();
				line("/** Creates a new {@link " + typeName(def) + "} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */");
				line("public static " + typeName(def) + " " + readerMethod(def) + "(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {");
				{
					line("in.nextTag();");
					line("return " + qImplName(def) + "." + readXmlContent(def) + "(in);");
				}
				line("}");
			}
		};
	}

	@Override
	public FileGenerator messageImplContents(Map<String, Option> options, MessageDef def) {
		if (noXml(options)) {
			return GeneratorPlugin.super.messageImplContents(options, def);
		}
		
		return new AbstractMessageGenerator(options) {
			@Override
			protected void generate() {
				nl();
				line("/** XML element name representing a {@link " + typeName(def) + "} type. */");
				line("public static final String " + xmlTypeNameConstant(def) + " = \"" + xmlTypeName(def) + "\";");
				
				for (Field field : def.getFields()) {
					nl();
					line("/** XML attribute or element name of a {@link #" + getterName(field) + "} property. */");
					line("private static final String " + xmlFieldNameConstant(field) + " = \"" + xmlFieldName(field) + "\";");
				}

				nl();
				line("/** Creates a new {@link " + typeName(def) + "} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */");
				line("public static " + implName(def) + " " + readXmlContent(def) + "(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {");
				{
					if (def.isAbstract()) {
						line("switch (in.getLocalName()) {");
						{
							for (MessageDef specialization : concreteSpecializations(def)) {
								line("case " + xmlTypeNameRef(specialization) + ": {");
								{
									line("return " + qImplName(specialization) + "." + readXmlContent(specialization) + "(in);");
								}
								line("}");
								nl();
							}
							
							line("default: {");
							{
								line("internalSkipUntilMatchingEndElement(in);");
								line("return null;");
							}
							line("}");
						}
						line("}");
					} else {
						line(implName(def) + " result = new " + implName(def) + "();");
						line("result.readContentXml(in);");
						line("return result;");
					}
				}
				line("}");
				
				if (!def.hasExtends()) {
					nl();
					line("/** Reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */");
					line("protected final void readContentXml(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {");
					{
						line("for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {");
						{
							line("String name = in.getAttributeLocalName(n);");
							line("String value = in.getAttributeValue(n);");
							nl();
							line("readFieldXmlAttribute(name, value);");
						}
						line("}");
						
						line("while (true) {");
						{
							line("int event = in.nextTag();");
							line("if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {");
							{
								line("break;");
							}
							line("}");
							line("assert event == javax.xml.stream.XMLStreamConstants.START_ELEMENT;");
							nl();
							
							line("String localName = in.getLocalName();");
							line("readFieldXmlElement(in, localName);");
						}
						line("}");
					}
					line("}");
				}
				
				nl();
				if (def.hasExtends()) {
					line("@Override");
				} else {
					line("/** Parses the given attribute value and assigns it to the field with the given name. */");
				}
				line("protected void readFieldXmlAttribute(String name, String value) {");
				{
					line("switch (name) {");
					{
						for (Field field : def.getFields()) {
							if (field.getType().kind() == Type.TypeKind.PRIMITIVE_TYPE) {
								line("case " + xmlFieldNameConstant(field) + ": {");
								{
									line(setterName(field) + "(" + fromString(field, "value") + ");");
									line("break;");
								}
								line("}");
							}
						}

						line("default: {");
						{
							if (def.hasExtends()) {
								line("super.readFieldXmlAttribute(name, value);");
							} else {
								line("// Skip unknown attribute.");
							}
						}
						line("}");
					}
					line("}");
				}
				line("}");

				List<Field> repeatedFields = new ArrayList<>();
				
				nl();
				if (def.hasExtends()) {
					line("@Override");
				} else {
					line("/** Reads the element under the cursor and assigns its contents to the field with the given name. */");
				}
				line("protected void readFieldXmlElement(javax.xml.stream.XMLStreamReader in, String localName) throws javax.xml.stream.XMLStreamException {");
				{
					line("switch (localName) {");
					{
						Set<String> names = new HashSet<>();
						for (Field field : def.getFields()) {
							if (!names.add(xmlFieldName(field))) {
								System.err.println("ERROR: Ambiguous element name '" + xmlFieldName(field) + "' in type '" + def.getName() + "'.");
								continue;
							}
							
							Type type = field.getType();
							
							switch (type.kind()) {
								case PRIMITIVE_TYPE: {
									line("case " + xmlFieldNameConstant(field) + ": {");
									{
										line(setterName(field) + "(" + fromString(field, "in.getElementText()") + ");");
										line("break;");
									}
									line("}");
									break;
								}
								
								case CUSTOM_TYPE: {
									Definition typeDefinition = ((CustomType) type).getDefinition();
									if (field.getOptions().get("Embedded") != null) {
										if (!(typeDefinition instanceof MessageDef)) {
											System.err.println("ERROR: Only other messages can be embedded from '" + field.getName() + "'.");
											continue;
										}
										
										for (MessageDef targetDef : concreteSpecializations((MessageDef) typeDefinition)) {
											if (!names.add(xmlTypeName(targetDef))) {
												System.err.println("ERROR: Ambiguous element name '" + xmlTypeName(targetDef) + "' in type '" + def.getName() + "'.");
												continue;
											}
											line("case " + qImplName(targetDef) + "." + xmlTypeNameConstant(targetDef) + ": {");
											{
												line((field.isRepeated() ? adderName(field) : setterName(field)) + "(" + qImplName(targetDef) + "." + readXmlContent(targetDef) + "(in));");
												line("break;");
											}
											line("}");
										}
									}
									
									if (typeDefinition instanceof MessageDef) {
										line("case " + xmlFieldNameConstant(field) + ": {");
										if (field.isRepeated()) {
											repeatedFields.add(field);
											line(readListFieldMethod(field) + "(in);");
										} else {
											MessageDef targetDef = (MessageDef) typeDefinition;
											if (targetDef.isAbstract()) {
												// Polymorphic field requires wrapper element.
												line("in.nextTag();");
												line(setterName(field) + "(" + qImplName(targetDef) + "." + readXmlContent(targetDef) + "(in)" + ");"); 
												line("internalSkipUntilMatchingEndElement(in);");
											} else {
												line(setterName(field) + "(" + qImplName(targetDef) + "." + readXmlContent(targetDef) + "(in)" + ");"); 
											}
										}
										line("break;");
										line("}");
									}
									break;
								}
								
								case MAP_TYPE: {
									System.err.println("ERROR: Map types not supported in XML: " + field.getName());
									break;
								}
							}
						}
						
						line("default: {");
						{
							if (def.hasExtends()) {
								line("super.readFieldXmlElement(in, localName);");
							} else {
								line("internalSkipUntilMatchingEndElement(in);");
							}
						}
						line("}");
					}
					line("}");
				}
				line("}");
				
				if (!def.hasExtends()) {
					nl();
					line("protected static final void internalSkipUntilMatchingEndElement(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {");
					{
						line("int level = 0;");
						line("while (true) {");
						{
							line("switch (in.next()) {");
							{
								line("case javax.xml.stream.XMLStreamConstants.START_ELEMENT: level++; break;");
								line("case javax.xml.stream.XMLStreamConstants.END_ELEMENT: if (level == 0) { return; } else { level--; break; }");
							}
							line("}");
						}
						line("}");
					}
					line("}");
				}				

				for (Field field : repeatedFields) {
					nl();
					line("private void " + readListFieldMethod(field) +"(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {");
					{
						line("while (true) {");
						{
							line("int event = in.nextTag();");
							line("if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {");
							{
								line("break;");
							}
							line("}");
							nl();
							
							MessageDef targetMessage = (MessageDef) ((CustomType) field.getType()).getDefinition();
							
							line(adderName(field) + "(" + qImplName(targetMessage) + "." + readXmlContent(targetMessage) + "(in));");
						}
						line("}");
					}
					line("}");
				}
			}

			String xmlTypeNameRef(MessageDef def) {
				return implName(def) + "." + xmlTypeNameConstant(def);
			}
		};
	}

	private boolean noXml(Map<String, Option> options) {
		return options.get("NoXml") != null;
	}

	String readerMethod(MessageDef def) {
		return "read" + CodeUtil.firstUpperCase(def.getName());
	}

	String xmlTypeName(MessageDef def) {
		Optional<Option> xmlName = Util.getOption(def, "XmlName");
		if (xmlName.isPresent()) {
			return ((StringOption) xmlName.get()).getValue();
		}
		Optional<Option> fieldName = Util.getOption(def, "Name");
		if (fieldName.isPresent()) {
			return ((StringOption) fieldName.get()).getValue();
		}
		return CodeUtil.xmlName(def.getName());
	}

	String xmlFieldName(Field def) {
		Optional<Option> xmlName = Util.getOption(def, "XmlName");
		if (xmlName.isPresent()) {
			return ((StringOption) xmlName.get()).getValue();
		}
		Optional<Option> fieldName = Util.getOption(def, "Name");
		if (fieldName.isPresent()) {
			return ((StringOption) fieldName.get()).getValue();
		}
		return CodeUtil.xmlName(def.getName());
	}

	String fromString(Field field, String value) {
		PrimitiveType type = (PrimitiveType) field.getType();
		if (field.isRepeated()) {
			return fromStringList(type, value);
		} else {
			return fromStringSingle(type, value);
		}
	}

	String fromStringList(de.haumacher.msgbuf.generator.ast.PrimitiveType type, String value) {
		return "java.util.Arrays.stream(" + value + ".split(\"\\\\s*,\\\\s*\")).map(x -> " + fromStringSingle(type, "x")
				+ ").collect(java.util.stream.Collectors.toList())";
	}

	String fromStringSingle(de.haumacher.msgbuf.generator.ast.PrimitiveType type, String value) {
		switch (type.getKind()) {
		case BOOL: 
			return "Boolean.parseBoolean(" + value + ")";
		
		case SFIXED_32: 
		case SINT_32:
		case INT_32: 
			return "Integer.parseInt(" + value + ")";
		
		case FIXED_32:
		case UINT_32: 
			return "(int) Long.parseLong(" + value + ")";
			
		case FIXED_64:
		case INT_64: 
		case UINT_64:
		case SFIXED_64:
		case SINT_64:
			return "Long.parseLong(" + value + ")";
			
		case DOUBLE:
			return "Double.parseDouble(" + value + ")";
			
		case FLOAT:
			return "Float.parseFloat(" + value + ")";
			
		case STRING:
			return value;
			
		case BYTES:
			return "java.util.Base64.getDecoder().decode(" + value + ");";
		}
		
		throw new UnsupportedOperationException("Cannot read values of type: " + type);
	}

	String xmlFieldNameConstant(Field field) {
		return CodeUtil.allUpperCase(field.getName()) + "__XML_ATTR";
	}

	String xmlTypeNameConstant(MessageDef def) {
		return CodeUtil.allUpperCase(def.getName()) + "__XML_ELEMENT";
	}

	String readListFieldMethod(Field field) {
		return "internalRead" + CodeUtil.firstUpperCase(field.getName()) + "ListXml";
	}

	String readXmlContent(MessageDef def) {
		return "read" + CodeUtil.firstUpperCase(def.getName()) + "_XmlContent";
	}

}
