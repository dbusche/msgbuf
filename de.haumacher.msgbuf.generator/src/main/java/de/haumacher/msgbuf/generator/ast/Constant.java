package de.haumacher.msgbuf.generator.ast;

/**
 * A constant of an {@link EnumDef}.
 *
 * @see EnumDef#getConstants()
 */
public class Constant extends Part {

	/**
	 * Creates a {@link Constant} instance.
	 */
	public static Constant create() {
		return new Constant();
	}

	/**
	 * Creates a {@link Constant} instance.
	 *
	 * @see #create()
	 */
	protected Constant() {
		super();
	}

	/** Reads a new instance from the given reader. */
	public static Constant readConstant(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		Constant result = new Constant();
		in.beginObject();
		result.readFields(in);
		in.endObject();
		return result;
	}

	@Override
	public String jsonType() {
		return "Constant";
	}

	@Override
	public <R,A> R visit(Part.Visitor<R,A> v, A arg) {
		return v.visit(this, arg);
	}

}
