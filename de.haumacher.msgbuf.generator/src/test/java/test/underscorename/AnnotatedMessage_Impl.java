package test.underscorename;

class AnnotatedMessage_Impl extends BaseMsg_Impl implements AnnotatedMessage {

	private String _annotatedField = "";

	/**
	 * Creates a {@link AnnotatedMessage_Impl} instance.
	 *
	 * @see AnnotatedMessage#create()
	 */
	protected AnnotatedMessage_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ANNOTATED_MESSAGE;
	}

	@Override
	public final String getAnnotatedField() {
		return _annotatedField;
	}

	@Override
	public AnnotatedMessage setAnnotatedField(String value) {
		internalSetAnnotatedField(value);
		return this;
	}

	/** Internal setter for {@link #getAnnotatedField()} without chain call utility. */
	protected final void internalSetAnnotatedField(String value) {
		_listener.beforeSet(this, ANNOTATED_FIELD__PROP, value);
		_annotatedField = value;
	}

	@Override
	public String jsonType() {
		return ANNOTATED_MESSAGE__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			ANNOTATED_FIELD__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case ANNOTATED_FIELD__PROP: return getAnnotatedField();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ANNOTATED_FIELD__PROP: internalSetAnnotatedField((String) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ANNOTATED_FIELD__PROP);
		out.value(getAnnotatedField());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ANNOTATED_FIELD__PROP: setAnnotatedField(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public int typeId() {
		return ANNOTATED_MESSAGE__TYPE_ID;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ANNOTATED_FIELD__ID);
		out.value(getAnnotatedField());
	}

	/** Helper for creating an object of type {@link AnnotatedMessage} from a polymorphic composition. */
	public static AnnotatedMessage readannotated_message_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		test.underscorename.AnnotatedMessage_Impl result = new AnnotatedMessage_Impl();
		result.readContent(in);
		return result;
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case ANNOTATED_FIELD__ID: setAnnotatedField(in.nextString()); break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link AnnotatedMessage} type. */
	public static final String ANNOTATED_MESSAGE__XML_ELEMENT = "m1";

	/** XML attribute or element name of a {@link #getAnnotatedField} property. */
	private static final String ANNOTATED_FIELD__XML_ATTR = "f1";

	/** Creates a new {@link AnnotatedMessage} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static AnnotatedMessage_Impl readAnnotated_message_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		AnnotatedMessage_Impl result = new AnnotatedMessage_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case ANNOTATED_FIELD__XML_ATTR: {
				setAnnotatedField(value);
				break;
			}
			default: {
				super.readFieldXmlAttribute(name, value);
			}
		}
	}

	@Override
	protected void readFieldXmlElement(javax.xml.stream.XMLStreamReader in, String localName) throws javax.xml.stream.XMLStreamException {
		switch (localName) {
			case ANNOTATED_FIELD__XML_ATTR: {
				setAnnotatedField(in.getElementText());
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(BaseMsg.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
