package test.graph.data.impl;

/**
 * Implementation of {@link test.graph.data.Rectangle}.
 */
public class Rectangle_Impl extends test.graph.data.impl.AtomicShape_Impl implements test.graph.data.Rectangle {

	private int _width = 0;

	private int _height = 0;

	/**
	 * Creates a {@link Rectangle_Impl} instance.
	 *
	 * @see test.graph.data.Rectangle#create()
	 */
	public Rectangle_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.RECTANGLE;
	}

	@Override
	public final int getWidth() {
		return _width;
	}

	@Override
	public test.graph.data.Rectangle setWidth(int value) {
		internalSetWidth(value);
		return this;
	}

	/** Internal setter for {@link #getWidth()} without chain call utility. */
	protected final void internalSetWidth(int value) {
		_listener.beforeSet(this, WIDTH__PROP, value);
		_width = value;
		_listener.afterChanged(this, WIDTH__PROP);
	}

	@Override
	public final int getHeight() {
		return _height;
	}

	@Override
	public test.graph.data.Rectangle setHeight(int value) {
		internalSetHeight(value);
		return this;
	}

	/** Internal setter for {@link #getHeight()} without chain call utility. */
	protected final void internalSetHeight(int value) {
		_listener.beforeSet(this, HEIGHT__PROP, value);
		_height = value;
		_listener.afterChanged(this, HEIGHT__PROP);
	}

	@Override
	public test.graph.data.Rectangle setXCoordinate(int value) {
		internalSetXCoordinate(value);
		return this;
	}

	@Override
	public test.graph.data.Rectangle setYCoordinate(int value) {
		internalSetYCoordinate(value);
		return this;
	}

	@Override
	public String jsonType() {
		return RECTANGLE__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			WIDTH__PROP, 
			HEIGHT__PROP));

	private static java.util.Set<String> TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(
			java.util.Arrays.asList(
				)));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case WIDTH__PROP: return getWidth();
			case HEIGHT__PROP: return getHeight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case WIDTH__PROP: internalSetWidth((int) value); break;
			case HEIGHT__PROP: internalSetHeight((int) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(WIDTH__PROP);
		out.value(getWidth());
		out.name(HEIGHT__PROP);
		out.value(getHeight());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case WIDTH__PROP: {
				out.value(getWidth());
				break;
			}
			case HEIGHT__PROP: {
				out.value(getHeight());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case WIDTH__PROP: setWidth(in.nextInt()); break;
			case HEIGHT__PROP: setHeight(in.nextInt()); break;
			default: super.readField(scope, in, field);
		}
	}

	/** XML element name representing a {@link test.graph.data.Rectangle} type. */
	public static final String RECTANGLE__XML_ELEMENT = "rectangle";

	/** XML attribute or element name of a {@link #getWidth} property. */
	private static final String WIDTH__XML_ATTR = "w";

	/** XML attribute or element name of a {@link #getHeight} property. */
	private static final String HEIGHT__XML_ATTR = "h";

	@Override
	public String getXmlTagName() {
		return RECTANGLE__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(WIDTH__XML_ATTR, Integer.toString(getWidth()));
		out.writeAttribute(HEIGHT__XML_ATTR, Integer.toString(getHeight()));
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link test.graph.data.Rectangle} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Rectangle_Impl readRectangle_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Rectangle_Impl result = new Rectangle_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case WIDTH__XML_ATTR: {
				setWidth(Integer.parseInt(value));
				break;
			}
			case HEIGHT__XML_ATTR: {
				setHeight(Integer.parseInt(value));
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
			case WIDTH__XML_ATTR: {
				setWidth(Integer.parseInt(in.getElementText()));
				break;
			}
			case HEIGHT__XML_ATTR: {
				setHeight(Integer.parseInt(in.getElementText()));
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(test.graph.data.AtomicShape.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
