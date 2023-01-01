package test.hierarchy.data;

/**
 * A rectangle.
 */
public interface Rectangle extends AtomicShape {

	/**
	 * Creates a {@link test.hierarchy.data.Rectangle} instance.
	 */
	static test.hierarchy.data.Rectangle create() {
		return new test.hierarchy.data.impl.Rectangle_Impl();
	}

	/** Identifier for the {@link test.hierarchy.data.Rectangle} type in JSON format. */
	String RECTANGLE__TYPE = "Rectangle";

	/** @see #getWidth() */
	String WIDTH__PROP = "w";

	/** @see #getHeight() */
	String HEIGHT__PROP = "h";

	/** Identifier for the {@link test.hierarchy.data.Rectangle} type in binary format. */
	static final int RECTANGLE__TYPE_ID = 2;

	/** Identifier for the property {@link #getWidth()} in binary format. */
	static final int WIDTH__ID = 4;

	/** Identifier for the property {@link #getHeight()} in binary format. */
	static final int HEIGHT__ID = 5;

	/**
	 * The width of this {@link Rectangle}.
	 *
	 * <p>
	 * The top left corner of this {@Rectangle} is at ({@link #getXCoordinate()}, {@link #getYCoordinate()}).
	 * </p>
	 *
	 * @see #getHeight()
	 */
	int getWidth();

	/**
	 * @see #getWidth()
	 */
	test.hierarchy.data.Rectangle setWidth(int value);

	/**
	 * The width of this {@link Rectangle}.
	 *
	 * @see #getWidth()
	 */
	int getHeight();

	/**
	 * @see #getHeight()
	 */
	test.hierarchy.data.Rectangle setHeight(int value);

	@Override
	test.hierarchy.data.Rectangle setXCoordinate(int value);

	@Override
	test.hierarchy.data.Rectangle setYCoordinate(int value);

	@Override
	test.hierarchy.data.Rectangle setColor(test.hierarchy.data.Color value);

	/** Reads a new instance from the given reader. */
	static test.hierarchy.data.Rectangle readRectangle(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		test.hierarchy.data.impl.Rectangle_Impl result = new test.hierarchy.data.impl.Rectangle_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static test.hierarchy.data.Rectangle readRectangle(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		test.hierarchy.data.Rectangle result = test.hierarchy.data.impl.Rectangle_Impl.readRectangle_Content(in);
		in.endObject();
		return result;
	}

	/** Creates a new {@link Rectangle} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Rectangle readRectangle(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return test.hierarchy.data.impl.Rectangle_Impl.readRectangle_XmlContent(in);
	}

}
