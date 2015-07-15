package se.wfh.libs.beencode.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class to represent a dictionary (key / value pairs) for beencoded data.<br>
 * A dictionary consists of key / value pairs. The keys are allways
 * {@link BString}, the values may be any {@link BNode}.<br>
 * <br>
 * This class provides methods to create and fill new dictionaries, as also
 * parse beencoded data from an {@link InputStream}.<br>
 * <br>
 * <code>
 * Dictionaries are encoded as a 'd' followed by a list of alternating keys and
 * their corresponding values followed by an 'e'. For example,
 * d3:cow3:moo4:spam4:eggse corresponds to {'cow': 'moo', 'spam': 'eggs'} and
 * d4:spaml1:a1:bee corresponds to {'spam': ['a', 'b']}. Keys must be strings
 * and appear in sorted order (sorted as raw strings, not alphanumerics).
 * </code>
 *
 * @since 0.1
 */
public final class BDict extends BNode<Map<BString, BNode<?>>> implements
		Serializable, Cloneable, Map<BString, BNode<?>> {
	private static final long serialVersionUID = 1L;

	/** Prefix declaring the start of a dictionary */
	public static final byte PREFIX = 'd';

	/** Suffix marking the end of a dictionary */
	public static final byte SUFFIX = 'e';

	/**
	 * Create a new empty dictionary.
	 */
	public BDict() {
		super(new HashMap<BString, BNode<?>>());
	}

	/**
	 * Create a new dictionary according to the data in the given stream.
	 *
	 * @param inp
	 *          The stream to read from
	 * @param prefix
	 *          The first read byte from the stream, has to be the {@link #PREFIX}
	 *
	 * @throws IOException
	 *           If something goes wrong while reading from the Stream.
	 * @throws IllegalArgumentException
	 *           If the given prefix is not the {@link #PREFIX}
	 *
	 * @see BNode#BNode(InputStream, byte)
	 */
	public BDict(final InputStream inp, final byte prefix) throws IOException,
			IllegalArgumentException {
		super(inp, prefix);
	}

	/**
	 * Create a new dictionary with the given elements.
	 *
	 * @param value
	 *          The nodes for this dictionary
	 * @see BNode#BNode(Object)
	 */
	public BDict(final Map<BString, BNode<?>> value) {
		super(value);
	}

	@Override
	public BDict clone() {
		/* create a new map */
		final Map<BString, BNode<?>> neu = new HashMap<>();

		synchronized (this) {
			/* clone all elements */
			value.entrySet().forEach(
					entry -> neu.put(entry.getKey().clone(), entry.getValue().clone()));
		}

		/* create a new dict with the created map */
		return new BDict(neu);
	}

	/**
	 * @see #get(Object)
	 *
	 * @param key
	 *          The key to fetch
	 * @return The node for the specified key or <code>null</code> if key was
	 *         not found.
	 */
	public BNode<?> get(final byte[] key) {
		return get(new BString(key));
	}

	/**
	 * @see #get(Object)
	 * 
	 * @param key
	 *          The key to fetch
	 * @return The node for the specified key or <code>null</code> if key was
	 *         not found.
	 */
	@Deprecated
	public BNode<?> get(final String key) {
		return get(new BString(key));
	}

	@Override
	protected String getReadableString(final int level) {
		/* initialize buffer */
		final StringBuilder buf = new StringBuilder();

		/* indent level */
		final int i_level = indent(buf, level);

		/* append prefix */
		buf.append("{\n");

		/* iterate over all keys */
		value.keySet().forEach(key -> {
			/* get value for key */
			final BNode<?> val = value.get(key);

			/* write key */
			buf.append(key.getReadableString(i_level + 1));

			/* write seperator between key and value */
			buf.append(" => ");

			/* write value */
			buf.append(val.getReadableString(-(i_level + 1)));

			/* write line break */
			buf.append('\n');
		});

		/* indent */
		indent(buf, i_level);

		/* append suffix */
		buf.append('}');

		/* return result */
		return buf.toString();
	}

	/**
	 * @param key
	 *          The key to set
	 * @param value
	 *          The value to set
	 * @return The previous value at the specified key or <code>null</code> if
	 *         none was present.
	 *
	 * @see Map#put(Object, Object)
	 */
	public BNode<?> put(final BString key, final BNode<?> value) {
		return this.value.put(key, value);
	}

	@Override
	protected Map<BString, BNode<?>> read(final InputStream inp, final byte prefix)
			throws IOException {
		/* abort when wrong prefix is given */
		if (prefix != BDict.PREFIX) {
			throw new IllegalArgumentException("Invalid prefix for an "
					+ this.getClass().getSimpleName() + ". Is '" + prefix
					+ "', expected: '" + PREFIX + "'");
		}

		/* prepare buffer for reading */
		final byte[] buf = new byte[] { prefix };

		/* prepare result map */
		final HashMap<BString, BNode<?>> result = new HashMap<>();

		/* read and parsed data are valid? */
		boolean success = false;

		/* prepare key object for an entry in the dict */
		BString key;

		/* as long as we have more data to read and the dict was not finished */
		while (inp.read(buf) == 1 && !success) {
			/* if the read byte is the suffix, then we are finished */
			if (buf[0] == BDict.SUFFIX) {
				success = true;
				break;
			}

			/* read the key */
			key = new BString(inp, buf[0]);
			if (inp.read(buf) <= 0) {
				throw new IOException("Unexpected end of data.");
			}

			/* parse the value and put the element into the dictionary */
			result.put(key, BNode.of(inp, buf[0]));
		}

		/* if end of stream was reached without completing the dict, abort */
		if (!success) {
			throw new IOException("Unexpected end of data.");
		}

		/* return the parsed data */
		return result;
	}

	@Override
	public String toString() {
		return getReadableString();
	}

	@Override
	public void write(final OutputStream out) throws IOException {
		/* write prefix */
		out.write(BDict.PREFIX);

		/* write each element in the dict */
		for (final BString key : value.keySet()) {
			/* first write the key */
			key.write(out);

			/* then write the value */
			value.get(key).write(out);
		}

		/* write the suffix */
		out.write(BDict.SUFFIX);
	}

	/*
	 * ===============================================================
	 * Implements java.util.Map
	 * ===============================================================
	 */

	@Override
	public int size() {
		return value.size();
	}

	@Override
	public boolean isEmpty() {
		return value.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		if (key == null || !BString.class.isAssignableFrom(key.getClass())) {
			return false;
		}

		return value.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		if (value == null || !BNode.class.isAssignableFrom(value.getClass())) {
			return false;
		}

		return this.value.containsValue(value);
	}

	@Override
	public BNode<?> get(Object key) {
		if (key == null || !BString.class.isAssignableFrom(key.getClass())) {
			return null;
		}

		return value.get(key);
	}

	@Override
	public BNode<?> remove(Object key) {
		if (key == null || !BString.class.isAssignableFrom(key.getClass())) {
			return null;
		}

		return value.remove(key);
	}

	@Override
	public void putAll(Map<? extends BString, ? extends BNode<?>> m) {
		value.putAll(m);
	}

	@Override
	public void clear() {
		value.clear();
	}

	@Override
	public Set<BString> keySet() {
		return value.keySet();
	}

	@Override
	public Collection<BNode<?>> values() {
		return value.values();
	}

	@Override
	public Set<Entry<BString, BNode<?>>> entrySet() {
		return value.entrySet();
	}
}
