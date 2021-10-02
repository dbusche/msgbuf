package de.haumacher.msgbuf.generator.ast;

/**
 * Base class for an option annotation.
 *
 * @see StringOption 
 * @see NumberOption 
 * @see Flag
 */
public abstract class Option extends de.haumacher.msgbuf.data.AbstractDataObject {

	/** Visitor interface for the {@link Option} hierarchy.*/
	public interface Visitor<R,A> {

		/** Visit case for {@link StringOption}.*/
		R visit(StringOption self, A arg);

		/** Visit case for {@link NumberOption}.*/
		R visit(NumberOption self, A arg);

		/** Visit case for {@link Flag}.*/
		R visit(Flag self, A arg);

	}

	/**
	 * Creates a {@link Option} instance.
	 */
	protected Option() {
		super();
	}

	/** Reads a new instance from the given reader. */
	public static Option readOption(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		Option result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case "StringOption": result = StringOption.readStringOption(in); break;
			case "NumberOption": result = NumberOption.readNumberOption(in); break;
			case "Flag": result = Flag.readFlag(in); break;
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

	/** The type identifier for this concrete subtype. */
	public abstract String jsonType();

	/** Accepts the given visitor. */
	public abstract <R,A> R visit(Visitor<R,A> v, A arg);


}
