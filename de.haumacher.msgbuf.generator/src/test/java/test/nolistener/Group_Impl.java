package test.nolistener;

/**
 * A group of shapes.
 */
class Group_Impl extends Shape_Impl implements Group {

	private final java.util.List<Shape> _shapes = new java.util.ArrayList<>();

	/**
	 * Creates a {@link Group_Impl} instance.
	 *
	 * @see Group#create()
	 */
	protected Group_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GROUP;
	}

	@Override
	public final java.util.List<Shape> getShapes() {
		return _shapes;
	}

	@Override
	public Group setShapes(java.util.List<? extends Shape> value) {
		internalSetShapes(value);
		return this;
	}

	/** Internal setter for {@link #getShapes()} without chain call utility. */
	protected final void internalSetShapes(java.util.List<? extends Shape> value) {
		if (value == null) throw new IllegalArgumentException("Property 'shapes' cannot be null.");
		_shapes.clear();
		_shapes.addAll(value);
	}

	@Override
	public Group addShape(Shape value) {
		internalAddShape(value);
		return this;
	}

	/** Implementation of {@link #addShape(Shape)} without chain call utility. */
	protected final void internalAddShape(Shape value) {
		_shapes.add(value);
	}

	@Override
	public final void removeShape(Shape value) {
		_shapes.remove(value);
	}

	@Override
	public Group setXCoordinate(int value) {
		internalSetXCoordinate(value);
		return this;
	}

	@Override
	public Group setYCoordinate(int value) {
		internalSetYCoordinate(value);
		return this;
	}

	@Override
	public String jsonType() {
		return GROUP__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			SHAPES__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case SHAPES__PROP: return getShapes();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SHAPES__PROP: internalSetShapes(de.haumacher.msgbuf.util.Conversions.asList(Shape.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(SHAPES__PROP);
		out.beginArray();
		for (Shape x : getShapes()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SHAPES__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addShape(test.nolistener.Shape.readShape(in));
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

	@Override
	public int typeId() {
		return GROUP__TYPE_ID;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(SHAPES__ID);
		{
			java.util.List<Shape> values = getShapes();
			out.beginArray(de.haumacher.msgbuf.binary.DataType.OBJECT, values.size());
			for (Shape x : values) {
				x.writeTo(out);
			}
			out.endArray();
		}
	}

	/** Helper for creating an object of type {@link Group} from a polymorphic composition. */
	public static Group readGroup_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		test.nolistener.Group_Impl result = new Group_Impl();
		result.readContent(in);
		return result;
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case SHAPES__ID: {
				in.beginArray();
				while (in.hasNext()) {
					addShape(test.nolistener.Shape.readShape(in));
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link Group} type. */
	public static final String GROUP__XML_ELEMENT = "group";

	/** XML attribute or element name of a {@link #getShapes} property. */
	private static final String SHAPES__XML_ATTR = "shapes";

	@Override
	public String getXmlTagName() {
		return GROUP__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		out.writeStartElement(SHAPES__XML_ATTR);
		for (test.nolistener.Shape element : getShapes()) {
			element.writeTo(out);
		}
		out.writeEndElement();
	}

	/** Creates a new {@link Group} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Group_Impl readGroup_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Group_Impl result = new Group_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			default: {
				super.readFieldXmlAttribute(name, value);
			}
		}
	}

	@Override
	protected void readFieldXmlElement(javax.xml.stream.XMLStreamReader in, String localName) throws javax.xml.stream.XMLStreamException {
		switch (localName) {
			case SHAPES__XML_ATTR: {
				internalReadShapesListXml(in);
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	private void internalReadShapesListXml(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		while (true) {
			int event = in.nextTag();
			if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
				break;
			}

			addShape(test.nolistener.Shape_Impl.readShape_XmlContent(in));
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(Shape.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
