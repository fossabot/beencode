package se.wfh.libs.beencode.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import se.wfh.libs.beencode.data.BInteger;
import se.wfh.libs.beencode.data.BList;
import se.wfh.libs.beencode.data.BString;
import se.wfh.libs.common.utils.Config;

public class BListTest {
	public BListTest() throws IOException {
		Config.load("src/test/resources/junit.conf");
	}

	@Test
	public void testClone() {
		BList list1 = new BList();
		BList list2 = new BList();

		list1.getList().add(new BInteger(13));
		list2.getList().add(new BInteger(13));

		Assert.assertEquals(list1, list2);

		list1.getList().add(new BString("asd"));
		Assert.assertNotEquals(list1, list2);

		list2.getList().add(new BString("asd"));
		Assert.assertEquals(list1, list2);
	}

	@Test(expected = IOException.class)
	public void testInvalidStreamEmpty() throws IOException {
		try (FileInputStream fstream = new FileInputStream(new File(
				Config.getString("junit.tests") + "blist_invalid_empty.dat"))) {
			new BList(fstream, (byte) 'l');

			Assert.fail("This method should not complete!");
		}
	}

	@Test(expected = IOException.class)
	public void testInvalidStreamEnd() throws IOException {
		try (FileInputStream fstream = new FileInputStream(new File(
				Config.getString("junit.tests") + "blist_invalid_end.dat"))) {
			fstream.skip(1);
			new BList(fstream, (byte) 'l');

			Assert.fail("This method should not complete!");
		}
	}

	@Test
	public void testNewByStream() throws IOException {
		try (FileInputStream fstream = new FileInputStream(new File(
				Config.getString("junit.tests") + "blist_simple.dat"))) {
			fstream.skip(1);
			BList bi = new BList(fstream, (byte) 'l');

			Assert.assertEquals(Long.valueOf(13), bi.getList().get(0).getValue());
			Assert.assertArrayEquals("test".getBytes(), (byte[]) bi.getList().get(1)
					.getValue());
		}
	}

	@Test
	public void testNewByStreamExtraData() throws IOException {
		try (FileInputStream fstream = new FileInputStream(new File(
				Config.getString("junit.tests") + "blist_extra_data.dat"))) {
			fstream.skip(1);
			BList bi = new BList(fstream, (byte) 'l');

			Assert.assertEquals(Long.valueOf(13), bi.getList().get(0).getValue());
			Assert.assertArrayEquals("test".getBytes(), (byte[]) bi.getList().get(1)
					.getValue());
		}
	}

	@Test
	public void testNewByString() {
		BList bi = new BList();

		bi.getList().add(new BInteger(13));
		bi.getList().add(new BString("test"));

		Assert.assertEquals("[\n  13\n  \"test\"\n]", bi.toString());
	}

	@Test
	public void testNewEmptyString() {
		BList bi = new BList();

		Assert.assertEquals("[\n]", bi.toString());
	}
}
