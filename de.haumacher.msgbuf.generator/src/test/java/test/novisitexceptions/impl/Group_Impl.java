package test.novisitexceptions.impl;

/**
 * Implementation of {@link test.novisitexceptions.Group}.
 */
public class Group_Impl extends test.novisitexceptions.impl.Shape_Impl implements test.novisitexceptions.Group {

	private final java.util.List<test.novisitexceptions.Shape> _shapes = new de.haumacher.msgbuf.util.ReferenceList<test.novisitexceptions.Shape>() {
		@Override
		protected void beforeAdd(int index, test.novisitexceptions.Shape element) {
			_listener.beforeAdd(Group_Impl.this, SHAPES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, test.novisitexceptions.Shape element) {
			_listener.afterRemove(Group_Impl.this, SHAPES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(Group_Impl.this, SHAPES__PROP);
		}
	};

	/**
	 * Creates a {@link Group_Impl} instance.
	 *
	 * @see test.novisitexceptions.Group#create()
	 */
	public Group_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GROUP;
	}

	@Override
	public final java.util.List<test.novisitexceptions.Shape> getShapes() {
		return _shapes;
	}

	@Override
	public test.novisitexceptions.Group setShapes(java.util.List<? extends test.novisitexceptions.Shape> value) {
		internalSetShapes(value);
		return this;
	}

	/** Internal setter for {@link #getShapes()} without chain call utility. */
	protected final void internalSetShapes(java.util.List<? extends test.novisitexceptions.Shape> value) {
		if (value == null) throw new IllegalArgumentException("Property 'shapes' cannot be null.");
		_shapes.clear();
		_shapes.addAll(value);
	}

	@Override
	public test.novisitexceptions.Group addShape(test.novisitexceptions.Shape value) {
		internalAddShape(value);
		return this;
	}

	/** Implementation of {@link #addShape(test.novisitexceptions.Shape)} without chain call utility. */
	protected final void internalAddShape(test.novisitexceptions.Shape value) {
		_shapes.add(value);
	}

	@Override
	public final void removeShape(test.novisitexceptions.Shape value) {
		_shapes.remove(value);
	}

	@Override
	public test.novisitexceptions.Group setXCoordinate(int value) {
		internalSetXCoordinate(value);
		return this;
	}

	@Override
	public test.novisitexceptions.Group setYCoordinate(int value) {
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
			case SHAPES__PROP: return getShapes();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SHAPES__PROP: internalSetShapes(de.haumacher.msgbuf.util.Conversions.asList(test.novisitexceptions.Shape.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(SHAPES__PROP);
		out.beginArray();
		for (test.novisitexceptions.Shape x : getShapes()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SHAPES__PROP: {
				java.util.List<test.novisitexceptions.Shape> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(test.novisitexceptions.Shape.readShape(in));
				}
				in.endArray();
				setShapes(newValue);
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
			java.util.List<test.novisitexceptions.Shape> values = getShapes();
			out.beginArray(de.haumacher.msgbuf.binary.DataType.OBJECT, values.size());
			for (test.novisitexceptions.Shape x : values) {
				x.writeTo(out);
			}
			out.endArray();
		}
	}

	/** Helper for creating an object of type {@link test.novisitexceptions.Group} from a polymorphic composition. */
	public static test.novisitexceptions.Group readGroup_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		test.novisitexceptions.impl.Group_Impl result = new Group_Impl();
		result.readContent(in);
		return result;
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case SHAPES__ID: {
				in.beginArray();
				while (in.hasNext()) {
					addShape(test.novisitexceptions.Shape.readShape(in));
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link test.novisitexceptions.Group} type. */
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
		for (test.novisitexceptions.Shape element : getShapes()) {
			element.writeTo(out);
		}
		out.writeEndElement();
	}

	/** Creates a new {@link test.novisitexceptions.Group} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
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

			addShape(test.novisitexceptions.impl.Shape_Impl.readShape_XmlContent(in));
		}
	}

	@Override
	public <R,A> R visit(test.novisitexceptions.Shape.Visitor<R,A> v, A arg) {
		return v.visit(this, arg);
	}

}
