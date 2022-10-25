/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.msgbuf.generator;

import static de.haumacher.msgbuf.generator.CodeConvention.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.haumacher.msgbuf.generator.ast.Flag;
import de.haumacher.msgbuf.generator.ast.MessageDef;
import de.haumacher.msgbuf.generator.ast.Option;
import de.haumacher.msgbuf.generator.util.AbstractJavaGenerator;

/**
 * Base class for generating message contents.
 */
public abstract class AbstractMessageGenerator extends AbstractJavaGenerator {

	private Map<String, Option> _options;
	
	protected final boolean _noInterfaces;

	/** 
	 * Creates a {@link AbstractMessageGenerator}.
	 */
	public AbstractMessageGenerator(Map<String, Option> options) {
		_options = options;
		_noInterfaces = isTrue(options.get("NoInterfaces"), false);
	}
	
	public Map<String, Option> getOptions() {
		return _options;
	}

	public static boolean isTrue(Option option, boolean defaultValue) {
		return option == null ? defaultValue : ((Flag) option).isValue();
	}
	
	protected final String implName(MessageDef def) {
		if (_noInterfaces) {
			return CodeConvention.typeName(def);
		} else {
			return CodeConvention.implName(def);
		}
	}

	protected String qImplName(MessageDef def) {
		if (_noInterfaces) {
			return qTypeName(def);
		} else {
			return CodeConvention.qImplName(def);
		}
	}

	protected static List<MessageDef> concreteSpecializations(MessageDef def) {
		ArrayList<MessageDef> result = new ArrayList<>();
		addConcreteSpecializations(result, def);
		return result;
	}

	private static void addConcreteSpecializations(ArrayList<MessageDef> result, MessageDef def) {
		for (MessageDef specialization : def.getSpecializations()) {
			if (!specialization.isAbstract()) {
				result.add(specialization);
			}
			
			addConcreteSpecializations(result, specialization);
		}
	}
	
}
