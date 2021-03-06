/*
 * MIT Licence
 * Copyright (c) 2017 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.libs.beencode;

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

@Immutable
public final class BString extends BNode<byte[]> implements Comparable<BString> {
    public static final int DEFAULT_MAX_READ_LEN = 33_554_432; // 32 MiB
    private static final long serialVersionUID = 1L;
    private static final byte SEPARATOR = ':';

    private BString(@NotNull byte[] data) {
        super(new byte[data.length]);
        System.arraycopy(data, 0, getValue(), 0, data.length);
    }

    @NotNull
    public static BString of(@NotNull byte[] data) {
        Objects.requireNonNull(data, "data may not be null");
        return new BString(data);
    }

    @NotNull
    public static BString of(@NotNull CharSequence data) {
        return of(data, Charset.defaultCharset());
    }

    @NotNull
    public static BString of(@NotNull CharSequence data, @NotNull Charset charset) {
        Objects.requireNonNull(data, "data may not be null");
        Objects.requireNonNull(charset, "charset may not be null");
        return of(data.toString().getBytes(charset));
    }

    @NotNull
    public static BString of(@NotNull InputStream is) throws IOException {
        return of(is, (byte) is.read());
    }

    @NotNull
    public static BString of(@NotNull InputStream is, byte prefix) throws IOException {
        return of(is, prefix, DEFAULT_MAX_READ_LEN);
    }

    @NotNull
    public static BString of(@NotNull InputStream is, byte prefix, int maxReadLen) throws IOException {
        long length = prefix - '0';

        byte cur;
        while ((cur = (byte) is.read()) != SEPARATOR) {
            if (!canParsePrefix(cur)) {
                throw new BencodeException("Unexpected data, expected an digit but got a '"
                        + cur + "'");
            }
            length = length * 10 + (cur - '0');
        }

        if (length > 0 && prefix == '0') {
            throw new BencodeException("Leading zeros are not allowed.");
        }

        if (length > maxReadLen) {
            throw new BencodeException("Denied attempt to read " + length + " bytes.");
        }

        int ilength = (int) length;
        byte[] value = new byte[ilength];
        int offset = 0;
        while (offset != ilength) {
            int temp = is.read(value, offset, ilength - offset);
            if (temp >= 0) {
                offset += temp;
            } else {
                throw new BencodeException("Premature end of stream, missing "
                        + (ilength - offset) + " bytes.");
            }
        }
        return of(value);
    }

    public static boolean canParsePrefix(byte prefix) {
        return prefix >= '0' && prefix <= '9';
    }

    @Override
    @NotNull
    public String toString() {
        return toString(Charset.defaultCharset());
    }

    @NotNull
    public String toString(@NotNull Charset encoding) {
        return new String(getValue(), encoding);
    }

    @Override
    public void write(@NotNull OutputStream os) throws IOException {
        os.write(String.valueOf(getValue().length).getBytes(DEFAULT_CHARSET));
        os.write(SEPARATOR);
        os.write(getValue());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + Arrays.hashCode(getValue());
    }

    @Override
    public int compareTo(@NotNull BString o) {
        return toString().compareTo(o.toString());
    }
}
