package com.bumptech.glide.signature;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.util.Preconditions;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * A unique Signature that wraps a String.
 */
public class StringSignature implements Key {

    private final String signature;

    public StringSignature(String signature) {
        this.signature = Preconditions.checkNotNull(signature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StringSignature that = (StringSignature) o;
        return signature.equals(that.signature);
    }

    @Override
    public int hashCode() {
        return signature.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(signature.getBytes(CHARSET));
    }

    @Override
    public String toString() {
        return "StringSignature{" + "signature='" + signature + '\'' + '}';
    }
}

