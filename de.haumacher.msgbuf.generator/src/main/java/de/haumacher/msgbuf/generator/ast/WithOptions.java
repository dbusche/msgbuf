package de.haumacher.msgbuf.generator.ast;

/**
 * Base class for object that can be annotated.
 */
public abstract class WithOptions extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.observer.Observable {

	/** Type codes for the {@link de.haumacher.msgbuf.generator.ast.WithOptions} hierarchy. */
	public enum TypeKind {

		/** Type literal for {@link de.haumacher.msgbuf.generator.ast.OptionContainer}. */
		OPTION_CONTAINER,

		/** Type literal for {@link de.haumacher.msgbuf.generator.ast.Constant}. */
		CONSTANT,

		/** Type literal for {@link de.haumacher.msgbuf.generator.ast.Field}. */
		FIELD,

		/** Type literal for {@link de.haumacher.msgbuf.generator.ast.EnumDef}. */
		ENUM_DEF,

		/** Type literal for {@link de.haumacher.msgbuf.generator.ast.MessageDef}. */
		MESSAGE_DEF,

		/** Type literal for {@link de.haumacher.msgbuf.generator.ast.DefinitionFile}. */
		DEFINITION_FILE,
		;

	}

	/** Visitor interface for the {@link de.haumacher.msgbuf.generator.ast.WithOptions} hierarchy.*/
	public interface Visitor<R,A> extends de.haumacher.msgbuf.generator.ast.DefinitionBase.Visitor<R,A> {

		/** Visit case for {@link de.haumacher.msgbuf.generator.ast.OptionContainer}.*/
		R visit(de.haumacher.msgbuf.generator.ast.OptionContainer self, A arg);

		/** Visit case for {@link de.haumacher.msgbuf.generator.ast.DefinitionFile}.*/
		R visit(de.haumacher.msgbuf.generator.ast.DefinitionFile self, A arg);

	}

	/** @see #getOptions() */
	public static final String OPTIONS__PROP = "options";

	private final java.util.Map<String, de.haumacher.msgbuf.generator.ast.Option> _options = new de.haumacher.msgbuf.util.ReferenceMap<>() {
		@Override
		protected void beforeAdd(String index, de.haumacher.msgbuf.generator.ast.Option element) {
			_listener.beforeAdd(WithOptions.this, OPTIONS__PROP, index, element);
		}

		@Override
		protected void afterRemove(String index, de.haumacher.msgbuf.generator.ast.Option element) {
			_listener.afterRemove(WithOptions.this, OPTIONS__PROP, index, element);
		}
	};

	/**
	 * Creates a {@link WithOptions} instance.
	 */
	protected WithOptions() {
		super();
	}

	/** The type code of this instance. */
	public abstract TypeKind kind();

	/**
	 * Annotations to this definition.
	 */
	public final java.util.Map<String, de.haumacher.msgbuf.generator.ast.Option> getOptions() {
		return _options;
	}

	/**
	 * @see #getOptions()
	 */
	public de.haumacher.msgbuf.generator.ast.WithOptions setOptions(java.util.Map<String, de.haumacher.msgbuf.generator.ast.Option> value) {
		internalSetOptions(value);
		return this;
	}

	/** Internal setter for {@link #getOptions()} without chain call utility. */
	protected final void internalSetOptions(java.util.Map<String, de.haumacher.msgbuf.generator.ast.Option> value) {
		if (value == null) throw new IllegalArgumentException("Property 'options' cannot be null.");
		_options.clear();
		_options.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getOptions()} map.
	 */
	public de.haumacher.msgbuf.generator.ast.WithOptions putOption(String key, de.haumacher.msgbuf.generator.ast.Option value) {
		internalPutOption(key, value);
		return this;
	}

	/** Implementation of {@link #putOption(String, de.haumacher.msgbuf.generator.ast.Option)} without chain call utility. */
	protected final void  internalPutOption(String key, de.haumacher.msgbuf.generator.ast.Option value) {
		if (_options.containsKey(key)) {
			throw new IllegalArgumentException("Property 'options' already contains a value for key '" + key + "'.");
		}
		_options.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getOptions()} map.
	 */
	public final void removeOption(String key) {
		_options.remove(key);
	}

	protected de.haumacher.msgbuf.observer.Listener _listener = de.haumacher.msgbuf.observer.Listener.NONE;

	@Override
	public de.haumacher.msgbuf.generator.ast.WithOptions registerListener(de.haumacher.msgbuf.observer.Listener l) {
		internalRegisterListener(l);
		return this;
	}

	protected final void internalRegisterListener(de.haumacher.msgbuf.observer.Listener l) {
		_listener = de.haumacher.msgbuf.observer.Listener.register(_listener, l);
	}

	@Override
	public de.haumacher.msgbuf.generator.ast.WithOptions unregisterListener(de.haumacher.msgbuf.observer.Listener l) {
		internalUnregisterListener(l);
		return this;
	}

	protected final void internalUnregisterListener(de.haumacher.msgbuf.observer.Listener l) {
		_listener = de.haumacher.msgbuf.observer.Listener.unregister(_listener, l);
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			OPTIONS__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case OPTIONS__PROP: return getOptions();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case OPTIONS__PROP: internalSetOptions((java.util.Map<String, de.haumacher.msgbuf.generator.ast.Option>) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static de.haumacher.msgbuf.generator.ast.WithOptions readWithOptions(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		de.haumacher.msgbuf.generator.ast.WithOptions result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case OptionContainer.OPTION_CONTAINER__TYPE: result = de.haumacher.msgbuf.generator.ast.OptionContainer.readOptionContainer(in); break;
			case DefinitionFile.DEFINITION_FILE__TYPE: result = de.haumacher.msgbuf.generator.ast.DefinitionFile.readDefinitionFile(in); break;
			case Constant.CONSTANT__TYPE: result = de.haumacher.msgbuf.generator.ast.Constant.readConstant(in); break;
			case Field.FIELD__TYPE: result = de.haumacher.msgbuf.generator.ast.Field.readField(in); break;
			case EnumDef.ENUM_DEF__TYPE: result = de.haumacher.msgbuf.generator.ast.EnumDef.readEnumDef(in); break;
			case MessageDef.MESSAGE_DEF__TYPE: result = de.haumacher.msgbuf.generator.ast.MessageDef.readMessageDef(in); break;
			default: in.skipValue(); result = null; break;
		}
		in.endArray();
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.beginArray();
		out.value(jsonType());
		writeContent(out);
		out.endArray();
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(OPTIONS__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,de.haumacher.msgbuf.generator.ast.Option> entry : getOptions().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OPTIONS__PROP: {
				java.util.Map<String, de.haumacher.msgbuf.generator.ast.Option> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), de.haumacher.msgbuf.generator.ast.Option.readOption(in));
				}
				in.endObject();
				setOptions(newValue);
				break;
			}
			default: super.readField(in, field);
		}
	}

	/** Accepts the given visitor. */
	public abstract <R,A> R visit(Visitor<R,A> v, A arg);

}
