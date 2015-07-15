package se.wfh.libs.beencode.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Class to represent a list of nodes for beencoded data.<br>
 * This class provides methods to create and fill a new list, as also parse
 * beencoded data from an {@link InputStream}.<br>
 * <br>
 * <code>
 * Lists are encoded as an 'l' followed by their elements (also bencoded)
 * followed by an 'e'. For example l4:spam4:eggse corresponds to ['spam', 'eggs'].
 * </code>
 *
 * @since 0.1
 */
public final class BList extends BNode<List<BNode<?>>> implements Serializable,
		Cloneable, List<BNode<?>> {
	private static final long serialVersionUID = 1L;

	/** Prefix declaring the start of a list */
	public static final byte PREFIX = 'l';

	/** Suffix marking the end of a dictionary */
	public static final byte SUFFIX = 'e';

	/**
	 * Create a new empty list.
	 */
	public BList() {
		super(new ArrayList<BNode<?>>());
	}

	/**
	 * Create a new list according to the data in the given stream.
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
	 */
	public BList(final InputStream inp, final byte prefix) throws IOException {
		super(inp, prefix);
	}

	/**
	 * Create a new list with the given elements.
	 *
	 * @param value
	 *          The nodes for this list
	 *
	 * @see BNode#BNode(Object)
	 */
	public BList(final List<BNode<?>> value) {
		super(value);
	}

	@Override
	public synchronized BList clone() {
		/* clone all elements */
		return new BList(value.stream().map(BNode::clone)
				.collect(Collectors.toList()));
	}

	@Override
	protected String getReadableString(final int level) {
		/* initialize buffer */
		final StringBuilder buf = new StringBuilder();

		/* indent level */
		final int i_level = indent(buf, level);

		/* append prefix */
		buf.append("[\n");

		/* iterate over all elements */
		value.forEach(node -> {
			/* write value */
			buf.append(node.getReadableString(i_level + 1));

			/* write line break */
			buf.append('\n');
		});

		/* indent */
		indent(buf, i_level);

		/* append suffix */
		buf.append(']');

		/* return result */
		return buf.toString();
	}

	@Override
	protected List<BNode<?>> read(final InputStream inp, final byte prefix)
			throws IOException {
		/* abort when wrong prefix is given */
		if (prefix != BList.PREFIX) {
			throw new IllegalArgumentException("Invalid prefix for an "
					+ this.getClass().getSimpleName() + ". Is '" + prefix
					+ "', expected: '" + PREFIX + "'");
		}

		/* prepare buffer for reading */
		int buf = -1;

		/* prepare result map */
		final List<BNode<?>> result = new ArrayList<>();

		/* read and parsed data are valid? */
		boolean success = false;

		/* as long as we have more data to read and the list was not finished */
		while (!success && (buf = inp.read()) != -1) {
			/* if the read byte is the suffix, then we are finished */
			if (buf == BList.SUFFIX) {
				success = true;
				break;
			}

			/* parse and add next node */
			result.add(BNode.of(inp, buf));
		}

		/* if end of stream was reached without completing the list, abort */
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
		out.write(BList.PREFIX);

		/* write each element in the list */
		for (final BNode<?> data : value) {
			data.write(out);
		}

		/* write the suffix */
		out.write(BList.SUFFIX);
	}

	/*
	 * ===============================================================
	 * Implements java.util.List
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
	public boolean contains(Object o) {
		if (o == null || !BNode.class.isAssignableFrom(o.getClass())) {
			return false;
		}

		return value.contains(o);
	}

	@Override
	public Iterator<BNode<?>> iterator() {
		return value.iterator();
	}

	@Override
	public BNode<?>[] toArray() {
		return value.toArray(new BNode[value.size()]);
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return value.toArray(a);
	}

	@Override
	public boolean add(BNode<?> e) {
		return value.add(e);
	}

	@Override
	public boolean remove(Object o) {
		if (o == null || !BNode.class.isAssignableFrom(o.getClass())) {
			return false;
		}

		return value.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return value.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends BNode<?>> c) {
		return value.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends BNode<?>> c) {
		return value.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return value.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return value.retainAll(c);
	}

	@Override
	public void clear() {
		value.clear();
	}

	@Override
	public BNode<?> get(int index) {
		return value.get(index);
	}

	@Override
	public BNode<?> set(int index, BNode<?> element) {
		return value.set(index, element);
	}

	@Override
	public void add(int index, BNode<?> element) {
		value.add(index, element);
	}

	@Override
	public BNode<?> remove(int index) {
		return value.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		if (o == null || !BNode.class.isAssignableFrom(o.getClass())) {
			return -1;
		}

		return value.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null || !BNode.class.isAssignableFrom(o.getClass())) {
			return -1;
		}

		return value.lastIndexOf(o);
	}

	@Override
	public ListIterator<BNode<?>> listIterator() {
		return value.listIterator();
	}

	@Override
	public ListIterator<BNode<?>> listIterator(int index) {
		return value.listIterator(index);
	}

	@Override
	public List<BNode<?>> subList(int fromIndex, int toIndex) {
		return value.subList(fromIndex, toIndex);
	}
}
