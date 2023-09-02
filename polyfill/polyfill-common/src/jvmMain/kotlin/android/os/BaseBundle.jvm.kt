/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os

import android.util.Log
import androidx.collection.ArrayMap
import java.io.Serializable
import java.util.Objects.requireNonNull
import java.util.function.BiFunction

/**
 * A mapping from String keys to values of various types. In most cases, you should work directly
 * with either the [Bundle] or [PersistableBundle] subclass.
 */
actual open class BaseBundle {
    companion object {
        /** @hide */
        protected const val TAG = "Bundle"
        private const val DEBUG = false

        /**
         * Does a loose equality check between two given [BaseBundle] objects. Returns `true` if
         * both are `null`, or if both are equal as per {@link #kindofEquals(BaseBundle)}.
         *
         * @param a A [BaseBundle] object
         * @param b Another [BaseBundle] to compare with a
         * @return `true` if both are the same, `false` otherwise
         * @see #kindofEquals(BaseBundle)
         * @hide
         */
        fun kindofEquals(a: BaseBundle?, b: BaseBundle?): Boolean {
            return (a == b) || (a != null && a.kindofEquals(b))
        }
    }

    internal var mMap: ArrayMap<String, Any?>

    /** The ClassLoader used unparcelling data from mParcelledData. */
    private var mClassLoader: ClassLoader? = null

    /**
     * Constructs a new, empty Bundle that uses a specific [ClassLoader] for instantiating
     * [Parcelable] and [Serializable] objects.
     *
     * @param loader An explicit [ClassLoader] to use when instantiating objects inside of the
     *   [Bundle].
     * @param capacity Initial size of the ArrayMap.
     */
    constructor(loader: ClassLoader?, capacity: Int) {
        mMap = if (capacity > 0) ArrayMap<String, Any?>(capacity) else ArrayMap<String, Any?>()
        mClassLoader = if (loader == null) this::class.java.classLoader else loader
    }

    /** Constructs a new, empty Bundle. */
    constructor() : this(null as ClassLoader?, 0) {}

    /**
     * Constructs a new, empty Bundle that uses a specific ClassLoader for instantiating Parcelable
     * and Serializable objects.
     *
     * @param loader An explicit ClassLoader to use when instantiating objects inside of the Bundle.
     */
    constructor(loader: ClassLoader?) : this(loader, 0) {}

    /**
     * Constructs a new, empty Bundle sized to hold the given number of elements. The Bundle will
     * grow as needed.
     *
     * @param capacity the initial capacity of the Bundle
     */
    constructor(capacity: Int) : this(null as ClassLoader?, capacity) {}

    /**
     * Constructs a Bundle containing a copy of the mappings from the given Bundle.
     *
     * @param b a Bundle to be copied.
     */
    constructor(b: BaseBundle) : this(b, deep = false) {}

    /**
     * Constructs a [BaseBundle] containing a copy of {@code from}.
     *
     * @param from The bundle to be copied.
     * @param deep Whether is a deep or shallow copy.
     * @hide
     */
    constructor(from: BaseBundle, deep: Boolean) {
        synchronized(from) {
            mClassLoader = from.mClassLoader

            if (!deep) {
                mMap = ArrayMap(from.mMap)
            } else {
                val fromMap = from.mMap
                val n = fromMap.size
                mMap = ArrayMap(n)
                for (i in 0 until n) {
                    mMap.put(fromMap.keyAt(i), deepCopyValue(fromMap.valueAt(i)))
                }
            }
        }
    }

    /**
     * Note: value in single-pair Bundle may be null.
     *
     * @hide
     *
     * TODO: optimize this later (getting just the value part of a Bundle with a single pair) once
     *   Bundle.forPair() above is implemented with a special single-value Map
     *   implementation/serialization.
     */
    open fun getPairValue(): String? {
        val size = mMap.size
        if (size > 1) {
            Log.w(TAG, "getPairValue() used on Bundle with multiple pairs.")
        }
        if (size == 0) {
            return null
        }
        try {
            return getValueAt(0, String::class.java)
        } catch (e: ClassCastException) {
            typeWarning("getPairValue()", "String", e)
            return null
        }
    }

    /**
     * Changes the ClassLoader this Bundle uses when instantiating objects.
     *
     * @param loader An explicit ClassLoader to use when instantiating objects inside of the Bundle.
     */
    internal open fun setClassLoader(loader: ClassLoader?) {
        mClassLoader = loader
    }

    /** Return the ClassLoader currently associated with this Bundle. */
    internal open fun getClassLoader(): ClassLoader? {
        return mClassLoader
    }

    /**
     * Returns the value for key [key].
     *
     * This call should always be made after {@link #unparcel()} or inside a lock after making sure
     * [mMap] is not null.
     *
     * @deprecated Use {@link #getValue(String, Class, Class[])}. This method should only be used in
     *   other deprecated APIs.
     * @hide
     */
    @Suppress("DEPRECATION")
    @Deprecated(
        "Use getValue(String, Class, Class...). This method should only be used in " +
            "other deprecated APIs."
    )
    fun getValue(key: String?): Any? {
        return getValue(key, /* clazz */ null)
    }

    /** Same as {@link #getValue(String, Class, Class[])} with no item types. */
    fun <T> getValue(key: String?, clazz: Class<T>?): T? {
        // Avoids allocating Class[0] array
        return getValue(key, clazz)
    }

    /**
     * Returns the value for key [key] for expected return type [clazz] (or pass `null` for no type
     * check).
     *
     * For [itemTypes], see {@link Parcel#readValue(int, ClassLoader, Class, Class[])}.
     *
     * This call should always be made after {@link #unparcel()} or inside a lock after making sure
     * [mMap] is not null.
     *
     * @hide
     */
    fun <T> getValue(key: String?, clazz: Class<T>?, vararg itemTypes: Class<*>?): T? {
        val i = mMap.indexOfKey(key)
        return if (i >= 0) getValueAt(i, clazz, *itemTypes) else null
    }

    /**
     * Returns the value for a certain position in the array map for expected return type [clazz]
     * (or pass `null` for no type check).
     *
     * For [itemTypes], see {@link Parcel#readValue(int, ClassLoader, Class, Class[])}.
     *
     * This call should always be made after {@link #unparcel()} or inside a lock after making sure
     * [mMap] is not null.
     *
     * @hide
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getValueAt(i: Int, clazz: Class<T>?, vararg itemTypes: Class<*>?): T? {
        var o = mMap.valueAt(i)
        if (o is BiFunction<*, *, *>) {
            o = (o as BiFunction<in Class<T>?, in Array<out Class<*>?>, *>).apply(clazz, itemTypes)
            mMap.setValueAt(i, o)
        }
        return if (clazz != null) clazz.cast(o) else o as T
    }

    /**
     * Returns the backing map of this bundle after deserializing every item.
     *
     * **Warning:** This method will deserialize every item on the bundle, including custom types
     * such as [Parcelable] and [Serializable], so only use this when you trust the source.
     * Specifically don't use this method on app-provided bundles.
     *
     * @hide
     */
    internal fun getItemwiseMap(): ArrayMap<String, Any?> {
        return mMap
    }

    /**
     * Returns the number of mappings contained in this Bundle.
     *
     * @return the number of mappings as an int.
     */
    fun size(): Int {
        return mMap.size
    }

    /** Returns true if the mapping of this Bundle is empty, false otherwise. */
    fun isEmpty(): Boolean {
        return mMap.isEmpty()
    }

    /**
     * This method returns true when the parcel is 'definitely' empty. That is, it may return false
     * for an empty parcel. But will never return true for a non-empty one.
     *
     * @hide this should probably be the implementation of isEmpty(). To do that we need to ensure
     *   we always use the special empty parcel form when the bundle is empty. (This may already be
     *   the case, but to be safe we'll do this later when we aren't trying to stabilize.)
     */
    fun isDefinitelyEmpty(): Boolean {
        return isEmpty()
    }

    /**
     * Performs a loose equality check, which means there can be false negatives but if the method
     * returns true than both objects are guaranteed to be equal.
     *
     * The point is that this method is a light-weight check in performance terms.
     *
     * @hide
     */
    fun kindofEquals(other: BaseBundle?): Boolean {
        if (other == null) {
            return false
        }
        if (isDefinitelyEmpty() && other.isDefinitelyEmpty()) {
            return true
        }
        return mMap.equals(other.mMap)
    }

    /** Removes all elements from the mapping of this Bundle. */
    open fun clear() {
        mMap.clear()
    }

    fun deepCopyValue(value: Any?): Any? {
        return when (val v = value) {
            null -> null
            is Bundle -> v.deepCopy()
            is ArrayList<*> -> deepcopyArrayList(v)
            else -> {
                if (v::class.java.isArray()) {
                    when (val v2 = v) {
                        is IntArray -> v2.clone()
                        is LongArray -> v2.clone()
                        is FloatArray -> v2.clone()
                        is DoubleArray -> v2.clone()
                        is ByteArray -> v2.clone()
                        is ShortArray -> v2.clone()
                        is CharArray -> v2.clone()
                        is Array<*> -> v2.clone()
                        else -> value
                    }
                } else {
                    value
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> deepcopyArrayList(from: ArrayList<T>): ArrayList<T> {
        val n = from.size
        val res = ArrayList<T>(n)
        for (i in 0 until n) {
            res.add(deepCopyValue(from.get(i)) as T)
        }
        return res
    }

    /**
     * Returns true if the given key is contained in the mapping of this Bundle.
     *
     * @param key a key: String
     * @return true if the key is part of the mapping, false otherwise
     */
    open fun containsKey(key: String?): Boolean {
        return mMap.containsKey(key)
    }

    /**
     * Returns the entry with the given key as an object.
     *
     * @param key a key: String
     * @return an Object, or null
     * @deprecated Use the type-safe specific APIs depending on the type of the item to be
     *   retrieved, eg. {@link #getString(String)}.
     */
    @Suppress("DEPRECATION")
    @Deprecated(
        "Use the type-safe specific APIs depending on the type of the item to be " +
            "retrieved, eg. getString(String)."
    )
    open fun get(key: String?): Any? {
        return getValue(key)
    }

    /**
     * Returns the object of type [clazz] for the given [key], or `null` if:
     * <ul>
     * <li>No mapping of the desired type exists for the given key.
     * <li>A `null` value is explicitly associated with the key.
     * <li>The object is not of type [clazz].
     * </ul>
     *
     * Use the more specific APIs where possible, especially in the case of containers such as
     * lists, since those APIs allow you to specify the type of the items.
     *
     * @param key key: String
     * @param clazz The type of the object expected
     * @return an Object, or null
     */
    internal open fun <T> get(key: String?, clazz: Class<T>): T? {
        try {
            return getValue(key, requireNonNull(clazz))
        } catch (e: ClassCastException) {
            typeWarning(key, clazz.getCanonicalName(), e)
            return null
        }
    }

    /**
     * Removes any entry with the given key from the mapping of this Bundle.
     *
     * @param key a key: String
     */
    open fun remove(key: String?) {
        mMap.remove(key)
    }

    /**
     * Inserts all mappings from the given Map into this BaseBundle.
     *
     * @param map a Map
     */
    @Suppress("UNCHECKED_CAST")
    open fun putAll(map: ArrayMap<out String, Any?>) {
        mMap.putAll(map as Map<String, Any?>)
    }

    /**
     * Returns a Set containing the Strings used as keys in this Bundle.
     *
     * @return a Set of String keys
     */
    open fun keySet(): Set<String> {
        throw Throwable("abc")
    }

    /** {@hide} */
    @Suppress("UNCHECKED_CAST")
    open fun putObject(key: String?, value: Any?) {
        when (val v = value) {
            null -> putString(key, null)
            is Boolean -> putBoolean(key, v)
            is Int -> putInt(key, v)
            is Long -> putLong(key, v)
            is Double -> putDouble(key, v)
            is String -> putString(key, v)
            is BooleanArray -> putBooleanArray(key, v)
            is IntArray -> putIntArray(key, v)
            is LongArray -> putLongArray(key, v)
            is DoubleArray -> putDoubleArray(key, v)
            is Array<*> -> {
                when {
                    v.isArrayOf<String>() -> putStringArray(key, v as Array<String>)
                    else -> throw IllegalArgumentException("Unsupported type " + value::class.java)
                }
            }
            else -> throw IllegalArgumentException("Unsupported type " + value::class.java)
        }
    }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing any existing value for the
     * given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a boolean
     */
    open fun putBoolean(key: String?, value: Boolean) {
        mMap.put(key, value)
    }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a byte
     */
    internal open fun putByte(key: String?, value: Byte) {
        mMap.put(key, value)
    }

    /**
     * Inserts a char value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a char
     */
    internal open fun putChar(key: String?, value: Char) {
        mMap.put(key, value)
    }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a short
     */
    internal open fun putShort(key: String?, value: Short) {
        mMap.put(key, value)
    }

    /**
     * Inserts an int value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value an int
     */
    open fun putInt(key: String?, value: Int) {
        mMap.put(key, value)
    }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a long
     */
    open fun putLong(key: String?, value: Long) {
        mMap.put(key, value)
    }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a float
     */
    internal open fun putFloat(key: String?, value: Float) {
        mMap.put(key, value)
    }

    /**
     * Inserts a double value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a double
     */
    open fun putDouble(key: String?, value: Double) {
        mMap.put(key, value)
    }

    /**
     * Inserts a String value into the mapping of this Bundle, replacing any existing value for the
     * given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a String, or null
     */
    open fun putString(key: String?, value: String?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing any existing value
     * for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence, or null
     */
    internal open fun putCharSequence(key: String?, value: CharSequence?) {
        mMap.put(key, value)
    }

    /**
     * Inserts an ArrayList<Integer> value into the mapping of this Bundle, replacing any existing
     * value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<Integer> object, or null
     */
    internal open fun putIntegerArrayList(key: String?, value: ArrayList<Int>?) {
        mMap.put(key, value)
    }

    /**
     * Inserts an ArrayList<String> value into the mapping of this Bundle, replacing any existing
     * value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<String> object, or null
     */
    internal open fun putStringArrayList(key: String?, value: ArrayList<String>?) {
        mMap.put(key, value)
    }

    /**
     * Inserts an ArrayList<CharSequence> value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<CharSequence> object, or null
     */
    internal open fun putCharSequenceArrayList(key: String?, value: ArrayList<CharSequence>?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing any existing value
     * for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Serializable object, or null
     */
    internal open fun putSerializable(key: String?, value: Serializable?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value
     * for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a boolean array object, or null
     */
    open fun putBooleanArray(key: String?, value: BooleanArray?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a byte array object, or null
     */
    internal open fun putByteArray(key: String?, value: ByteArray?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a short array object, or null
     */
    internal open fun putShortArray(key: String?, value: ShortArray?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a char array object, or null
     */
    internal open fun putCharArray(key: String?, value: CharArray?) {
        mMap.put(key, value)
    }

    /**
     * Inserts an int array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an int array object, or null
     */
    open fun putIntArray(key: String?, value: IntArray?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a long array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a long array object, or null
     */
    open fun putLongArray(key: String?, value: LongArray?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a float array object, or null
     */
    internal open fun putFloatArray(key: String?, value: FloatArray?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a double array value into the mapping of this Bundle, replacing any existing value
     * for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a double array object, or null
     */
    open fun putDoubleArray(key: String?, value: DoubleArray?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a String array value into the mapping of this Bundle, replacing any existing value
     * for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a String array object, or null
     */
    open fun putStringArray(key: String?, value: Array<String>?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing any existing
     * value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence array object, or null
     */
    internal open fun putCharSequenceArray(key: String?, value: Array<CharSequence>?) {
        mMap.put(key, value)
    }

    /**
     * Returns the value associated with the given key, or false if no mapping of the desired type
     * exists for the given key.
     *
     * @param key a String
     * @return a boolean value
     */
    open fun getBoolean(key: String?): Boolean {
        if (DEBUG)
            Log.d(TAG, "Getting boolean in " + Integer.toHexString(System.identityHashCode(this)))
        return getBoolean(key, false)
    }

    // Log a message if the value was non-null but not of the expected type
    internal fun typeWarning(
        key: String?,
        value: Any?,
        className: String,
        defaultValue: Any?,
        e: RuntimeException
    ) {
        val sb = StringBuilder()
        sb.append("Key ")
        sb.append(key)
        sb.append(" expected ")
        sb.append(className)
        if (value != null) {
            sb.append(" but value was a ")
            sb.append(value::class.java.name)
        } else {
            sb.append(" but value was of a different type")
        }
        sb.append(".  The default value ")
        sb.append(defaultValue)
        sb.append(" was returned.")
        Log.w(TAG, sb.toString())
        Log.w(TAG, "Attempt to cast generated internal exception:", e)
    }

    internal fun typeWarning(key: String?, value: Any?, className: String, e: RuntimeException) {
        typeWarning(key, value, className, "<null>", e)
    }

    internal fun typeWarning(key: String?, className: String, e: RuntimeException) {
        typeWarning(key, /* value */ null, className, "<null>", e)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a boolean value
     */
    open fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        val o = mMap.get(key)
        if (o == null) {
            return defaultValue
        }
        try {
            return o as Boolean
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Boolean", defaultValue, e)
            return defaultValue
        }
    }

    /**
     * Returns the value associated with the given key, or (byte) 0 if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @return a byte value
     */
    internal open fun getByte(key: String?): Byte {
        return getByte(key, 0)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a byte value
     */
    internal open fun getByte(key: String?, defaultValue: Byte): Byte {
        val o = mMap.get(key)
        if (o == null) {
            return defaultValue
        }
        try {
            return o as Byte
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Byte", defaultValue, e)
            return defaultValue
        }
    }

    /**
     * Returns the value associated with the given key, or (char) 0 if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @return a char value
     */
    internal open fun getChar(key: String?): Char {
        return getChar(key, '\u0000')
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a char value
     */
    internal open fun getChar(key: String?, defaultValue: Char): Char {
        val o = mMap.get(key)
        if (o == null) {
            return defaultValue
        }
        try {
            return o as Char
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Character", defaultValue, e)
            return defaultValue
        }
    }

    /**
     * Returns the value associated with the given key, or (short) 0 if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @return a short value
     */
    internal open fun getShort(key: String?): Short {
        return getShort(key, 0)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a short value
     */
    internal open fun getShort(key: String?, defaultValue: Short): Short {
        val o = mMap.get(key)
        if (o == null) {
            return defaultValue
        }
        try {
            return o as Short
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Short", defaultValue, e)
            return defaultValue
        }
    }

    /**
     * Returns the value associated with the given key, or 0 if no mapping of the desired type
     * exists for the given key.
     *
     * @param key a String
     * @return an int value
     */
    open fun getInt(key: String?): Int {
        return getInt(key, 0)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return an int value
     */
    open fun getInt(key: String?, defaultValue: Int): Int {
        val o = mMap.get(key)
        if (o == null) {
            return defaultValue
        }
        try {
            return o as Int
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Integer", defaultValue, e)
            return defaultValue
        }
    }

    /**
     * Returns the value associated with the given key, or 0L if no mapping of the desired type
     * exists for the given key.
     *
     * @param key a String
     * @return a long value
     */
    open fun getLong(key: String?): Long {
        return getLong(key, 0L)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a long value
     */
    open fun getLong(key: String?, defaultValue: Long): Long {
        val o = mMap.get(key)
        if (o == null) {
            return defaultValue
        }
        try {
            return o as Long
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Long", defaultValue, e)
            return defaultValue
        }
    }

    /**
     * Returns the value associated with the given key, or 0.0f if no mapping of the desired type
     * exists for the given key.
     *
     * @param key a String
     * @return a float value
     */
    internal open fun getFloat(key: String?): Float {
        return getFloat(key, 0.0f)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a float value
     */
    internal open fun getFloat(key: String?, defaultValue: Float): Float {
        val o = mMap.get(key)
        if (o == null) {
            return defaultValue
        }
        try {
            return o as Float
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Float", defaultValue, e)
            return defaultValue
        }
    }

    /**
     * Returns the value associated with the given key, or 0.0 if no mapping of the desired type
     * exists for the given key.
     *
     * @param key a String
     * @return a double value
     */
    open fun getDouble(key: String?): Double {
        return getDouble(key, 0.0)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a double value
     */
    open fun getDouble(key: String?, defaultValue: Double): Double {
        val o = mMap.get(key)
        if (o == null) {
            return defaultValue
        }
        try {
            return o as Double
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Double", defaultValue, e)
            return defaultValue
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a String value, or null
     */
    open fun getString(key: String?): String? {
        val o = mMap.get(key)
        try {
            return o as String
        } catch (e: ClassCastException) {
            typeWarning(key, o, "String", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key or if a null value is explicitly associated with the given key.
     *
     * @param key a String, or null
     * @param defaultValue Value to return if key does not exist or if a null value is associated
     *   with the given key.
     * @return the String value associated with the given key, or defaultValue if no valid String
     *   object is currently mapped to that key.
     */
    open fun getString(key: String?, defaultValue: String?): String? {
        return getString(key) ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharSequence value, or null
     */
    internal open fun getCharSequence(key: String?): CharSequence? {
        val o = mMap.get(key)
        try {
            return o as CharSequence
        } catch (e: ClassCastException) {
            typeWarning(key, o, "CharSequence", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key or if a null value is explicitly associated with the given key.
     *
     * @param key a String, or null
     * @param defaultValue Value to return if key does not exist or if a null value is associated
     *   with the given key.
     * @return the CharSequence value associated with the given key, or defaultValue if no valid
     *   CharSequence object is currently mapped to that key.
     */
    internal open fun getCharSequence(key: String?, defaultValue: CharSequence?): CharSequence? {
        return getCharSequence(key) ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Serializable value, or null
     * @deprecated Use {@link #getSerializable(String, Class)}. This method should only be used in
     *   other deprecated APIs.
     */
    @Suppress("DEPRECATION")
    @Deprecated(
        "Use getSerializable(String, Class). This method should only be used in " +
            "other deprecated APIs."
    )
    internal open fun getSerializable(key: String?): Serializable? {
        val o = getValue(key)
        if (o == null) {
            return null
        }
        try {
            return o as Serializable
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Serializable", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or `null` if:
     * <ul>
     * <li>No mapping of the desired type exists for the given key.
     * <li>A `null` value is explicitly associated with the key.
     * <li>The object is not of type [clazz].
     * </ul>
     *
     * @param key a String, or null
     * @param clazz The expected class of the returned type
     * @return a Serializable value, or null
     */
    internal open fun <T : Serializable> getSerializable(key: String?, clazz: Class<T>): T? {
        return get(key, clazz)
    }

    @Suppress("UNCHECKED_CAST")
    internal open fun <T> getArrayList(key: String?, clazz: Class<out T>): ArrayList<T>? {
        try {
            return getValue(key, ArrayList::class.java, requireNonNull(clazz)) as ArrayList<T>?
        } catch (e: ClassCastException) {
            typeWarning(key, "ArrayList<" + clazz.getCanonicalName() + ">", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
     */
    internal open fun getIntegerArrayList(key: String?): ArrayList<Int>? {
        return getArrayList(key, Int::class.java)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
     */
    internal open fun getStringArrayList(key: String?): ArrayList<String>? {
        return getArrayList(key, String::class.java)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<CharSequence> value, or null
     */
    internal open fun getCharSequenceArrayList(key: String?): ArrayList<CharSequence>? {
        return getArrayList(key, CharSequence::class.java)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a BooleanArray value, or null
     */
    open fun getBooleanArray(key: String?): BooleanArray? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as BooleanArray
        } catch (e: ClassCastException) {
            typeWarning(key, o, "byte[]", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a ByteArray value, or null
     */
    internal open fun getByteArray(key: String?): ByteArray? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as ByteArray
        } catch (e: ClassCastException) {
            typeWarning(key, o, "byte[]", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a ShortArray value, or null
     */
    internal open fun getShortArray(key: String?): ShortArray? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as ShortArray
        } catch (e: ClassCastException) {
            typeWarning(key, o, "short[]", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharArray value, or null
     */
    internal open fun getCharArray(key: String?): CharArray? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as CharArray
        } catch (e: ClassCastException) {
            typeWarning(key, o, "char[]", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an IntArray value, or null
     */
    internal open fun getIntArray(key: String?): IntArray? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as IntArray
        } catch (e: ClassCastException) {
            typeWarning(key, o, "int[]", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a LongArray value, or null
     */
    fun getLongArray(key: String?): LongArray? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as LongArray
        } catch (e: ClassCastException) {
            typeWarning(key, o, "long[]", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a FloatArray value, or null
     */
    internal open fun getFloatArray(key: String?): FloatArray? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as FloatArray
        } catch (e: ClassCastException) {
            typeWarning(key, o, "float", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a DoubleArray value, or null
     */
    fun getDoubleArray(key: String?): DoubleArray? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as DoubleArray
        } catch (e: ClassCastException) {
            typeWarning(key, o, "double[]", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Array<String> value, or null
     */
    @Suppress("UNCHECKED_CAST")
    fun getStringArray(key: String?): Array<String>? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as Array<String>
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Array<String>", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Array<CharSequence> value, or null
     */
    @Suppress("UNCHECKED_CAST")
    internal open fun getCharSequenceArray(key: String?): Array<CharSequence>? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as Array<CharSequence>
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Array<CharSequence>", e)
            return null
        }
    }
}
