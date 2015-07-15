package se.wfh.libs.beencode.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Class to represent an integer for beencoded data.<br>
 * A beencoded integer is a 64 bit number, represented by a {@link Long}.<br>
 * <br>
 * <code>
 * Integers are represented by an 'i' followed by the number in base 10
 * followed by an 'e'. For example i3e corresponds to 3 and i-3e corresponds
 * to -3. Integers have no size limitation. i-0e is invalid. All encodings with
 * a leading zero, such as i03e, are invalid, other than i0e, which of course
 * corresponds to 0.
 * </code>
 *
 * @since 0.1
 */
public final class BInteger extends BNode<Long> implements Serializable,
		Cloneable {
	private static final long serialVersionUID = 1L;

	/** Prefix declaring the start of an integer */
	public static final byte PREFIX = 'i';

	/** Suffix marking the end of an integer */
	public static final byte SUFFIX = 'e';

	/**
	 * Create a new Integer from the given data
	 *
	 * @param inp
	 *          The stream to parse
	 * @param prefix
	 *          The first byte of the stream
	 *
	 * @throws IOException
	 *           If something goes wrong while reading from the Stream.
	 * @throws IllegalArgumentException
	 *           If the given prefix is not the {@link #PREFIX}
	 *
	 * @see BNode#BNode(InputStream, byte)
	 */
	public BInteger(final InputStream inp, final byte prefix) throws IOException {
		super(inp, prefix);
	}

	/**
	 * Create a new beencoded integer.
	 *
	 * @param value
	 *          The value
	 */
	public BInteger(final long value) {
		super(value);
	}

	private long checkResult(final boolean finished, final boolean negative,
			final long number) throws IOException {
		/* if end of stream was reached without completing the integer, abort */
		if (!finished) {
			throw new IOException("Unexpected end of data.");
		}

		/* negative zero is not allowed per definition */
		if (negative && number == 0) {
			throw new IOException("Invalid data in stream: -0 is not permitted.");
		}

		/* if the negative flag is set, turn the result */
		if (negative) {
			return -number;
		} else {
			return number;
		}
	}

	@Override
	public BInteger clone() {
		/* create a new BInteger with the same value */
		return new BInteger(value);
	}

	@Override
	protected String getReadableString(final int level) {
		/* initialize buffer */
		final StringBuilder buf = new StringBuilder();

		/* indent */
		indent(buf, level);

		/* write number */
		buf.append(value.toString());

		/* return result */
		return buf.toString();
	}

	@Override
	protected Long read(final InputStream inp, final byte prefix)
			throws IOException {
		/* abort when wrong prefix is given */
		if (prefix != BInteger.PREFIX) {
			throw new IllegalArgumentException("Invalid prefix for an "
					+ this.getClass().getSimpleName() + ". Is '" + prefix
					+ "', expected: '" + PREFIX + "'");
		}

		/* prepare buffer for reading */
		int buf = -1;

		/* prepare result */
		long number = 0;

		/* started yet? */
		boolean started = false;

		/* read and parsed data are valid? */
		boolean finished = false;

		/* read number is negative? */
		boolean negative = false;

		/* only zero value? */
		boolean only_zero = false;

		/* as long as we have more data to read and the integer was not finished */
		while (!finished && (buf = inp.read()) != -1) {
			if (buf == 'e') {
				/* switch flag, number successfully read */
				finished = started || only_zero;
				break;
			} else if (Character.isDigit(buf)) {
				if (buf == '0' && !started) {
					only_zero = true;
					started = true;
					continue;
				}

				/* check for preceeding 0s */
				if (only_zero) {
					throw new IOException("Leading zeros are not permitted.");
				}

				/* append the read digit to the result */
				number = number * 10 + buf - 0x30;
				only_zero = false;
				started = true;
				continue;
			} else if (buf == '-' && !started) {
				/* if nothing else was read so far, we set the negative bit */
				negative = true;
				continue;
			}

			/* invalid character read */
			throw new IOException("Invalid data in stream. Read: '" + buf
					+ "', EOF: '" + (inp.available() == 0) + "'.");
		}

		/* return the parsed data */
		return checkResult(finished, negative, number);
	}

	@Override
	public String toString() {
		return getReadableString();
	}

	@Override
	public void write(final OutputStream out) throws IOException {
		/* write the prefix */
		out.write(BInteger.PREFIX);

		/* write the number */
		out.write(String.valueOf(value).getBytes(StandardCharsets.US_ASCII));

		/* write the suffix */
		out.write(BInteger.SUFFIX);
	}
}
