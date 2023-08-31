/*
 * Copyright (C) 2007 The Android Open Source Project
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

import androidx.collection.ArrayMap
import java.io.Serializable
import java.util.Objects.requireNonNull

/**
 * A mapping from key: Strings to various {@link Parcelable} values.
 *
 * <p><b>Warning:</b> Note that {@link Bundle} is a lazy container and as such it does NOT implement
 * {@link #equals(Object)} or {@link #hashCode()}.
 *
 * @see PersistableBundle
 */
actual class Bundle : BaseBundle, Cloneable, Parcelable {
    companion object {
        // /** An unmodifiable {@code Bundle} that is always {@link #isEmpty() empty}. */
        // val EMPTY = Bundle().apply { mMap = ArrayMap.EMPTY }

        // /**
        //  * Special extras used to denote extras have been stripped off.
        //  *
        //  * @hide
        //  */
        // val STRIPPED = Bundle().apply { putInt("STRIPPED", 1) }

        /**
         * Make a Bundle for a single key/value pair.
         *
         * @hide
         */
        // @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
        fun forPair(key: String?, value: String?): Bundle {
            val b = Bundle(1)
            b.putString(key, value)
            return b
        }
    }

    /** Constructs a new, empty Bundle. */
    constructor() : super() {}

    /**
     * Constructs a {@link Bundle} containing a copy of {@code from}.
     *
     * @param from The bundle to be copied.
     * @param deep Whether is a deep or shallow copy.
     * @hide
     */
    constructor(from: Bundle, deep: Boolean) : super(from, deep) {}

    /**
     * Constructs a new, empty Bundle that uses a specific ClassLoader for instantiating Parcelable
     * and Serializable objects.
     *
     * @param loader An explicit ClassLoader to use when instantiating objects inside of the Bundle.
     */
    constructor(loader: ClassLoader) : super(loader) {}

    /**
     * Constructs a new, empty Bundle sized to hold the given number of elements. The Bundle will
     * grow as needed.
     *
     * @param capacity the initial capacity of the Bundle
     */
    constructor(capacity: Int) : super(capacity) {}

    /**
     * Constructs a [Bundle] containing a copy of the mappings from the given Bundle. Does only a
     * shallow copy of the original Bundle -- see {@link #deepCopy()} if that is not what you want.
     *
     * @param b a Bundle to be copied.
     * @see #deepCopy()
     */
    constructor(b: Bundle) : super(b) {}

    /**
     * Changes the [ClassLoader] this [Bundle] uses when instantiating objects.
     *
     * @param loader An explicit [ClassLoader] to use when instantiating objects inside of the Bundle.
     */
    public override fun setClassLoader(loader: ClassLoader?) {
        super.setClassLoader(loader)
    }

    /** Return the [ClassLoader] currently associated with this Bundle. */
    public override fun getClassLoader(): ClassLoader? {
        return super.getClassLoader()
    }

    /**
     * Clones the current Bundle. The internal map is cloned, but the keys and values to which it
     * refers are copied by reference.
     */
    override fun clone(): Any {
        return Bundle(this)
    }

    /**
     * Make a deep copy of the given bundle. Traverses into inner containers and copies them as
     * well, so they are not shared across bundles. Will traverse in to {@link Bundle}, {@link
     * PersistableBundle}, {@link ArrayList}, and all types of primitive arrays. Other types of
     * objects (such as Parcelable or Serializable) are referenced as-is and not copied in any way.
     */
    fun deepCopy(): Bundle {
        return Bundle(this, deep = true)
    }

    /** Removes all elements from the mapping of this Bundle. */
    public override fun clear() {
        super.clear()
    }

    /**
     * Removes any entry with the given key from the mapping of this Bundle.
     *
     * @param key a key: String
     */
    override fun remove(key: String?) {
        super.remove(key)
    }

    /**
     * Inserts all mappings from the given Bundle into this Bundle.
     *
     * @param bundle a Bundle
     */
    fun putAll(bundle: Bundle) {
        mMap.putAll(bundle.mMap as Map<String, Any?>)
    }

    /** {@hide} */
    public override fun putObject(key: String?, value: Any?) {
        when (val v = value) {
            is Byte -> putByte(key, v)
            is Char-> putChar(key, v)
            is Short -> putShort(key, v)
            is Float -> putFloat(key, v)
            is CharSequence -> putCharSequence(key, v)
            is Parcelable -> putParcelable(key, v)
            is ArrayList<*> -> putParcelableArrayList(key, v as ArrayList<out Parcelable>)
            is List<*> -> putParcelableList(key, v as List<out Parcelable>)
            is Serializable -> putSerializable(key, v)
            is ByteArray -> putByteArray(key, v)
            is ShortArray -> putShortArray(key, v)
            is CharArray -> putCharArray(key, v)
            is FloatArray -> putFloatArray(key, v)
            is Array<*> ->
                when {
                    v.isArrayOf<Parcelable>() -> putParcelableArray(key, v as Array<Parcelable>)
                    v.isArrayOf<CharSequence>() ->
                        putCharSequenceArray(key, v as Array<CharSequence>)
                    else -> super.putObject(key, value)
                }
            is Bundle -> putBundle(key, value as Bundle)
            else -> super.putObject(key, value)
        }
    }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a byte
     */
    public override fun putByte(key: String?, value: Byte) {
        super.putByte(key, value)
    }

    /**
     * Inserts a char value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a char
     */
    public override fun putChar(key: String?, value: Char) {
        super.putChar(key, value)
    }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a short
     */
    public override fun putShort(key: String?, value: Short) {
        super.putShort(key, value)
    }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing any existing value for the
     * given key.
     *
     * @param key a String, or null
     * @param value a float
     */
    public override fun putFloat(key: String?, value: Float) {
        super.putFloat(key, value)
    }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing any existing value
     * for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence, or null
     */
    public override fun putCharSequence(key: String?, value: CharSequence?) {
        super.putCharSequence(key, value)
    }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Parcelable object, or null
     */
    fun putParcelable(key: String?, value: Parcelable?) {
        mMap.put(key, value)
    }

    /**
     * Inserts an array of Parcelable values into the mapping of this Bundle, replacing any existing
     * value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an array of Parcelable objects, or null
     */
    fun putParcelableArray(key: String?, value: Array<Parcelable>?) {
        mMap.put(key, value)
    }

    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle, replacing any existing
     * value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList of Parcelable objects, or null
     */
    fun putParcelableArrayList(key: String?, value: ArrayList<out Parcelable>?) {
        mMap.put(key, value)
    }

    /** {@hide} */
    // @UnsupportedAppUsage
    fun putParcelableList(key: String?, value: List<out Parcelable>?) {
        mMap.put(key, value)
    }

    /**
     * Inserts an ArrayList<Integer> value into the mapping of this Bundle, replacing any existing
     * value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<Integer> object, or null
     */
    public override fun putIntegerArrayList(key: String?, value: ArrayList<Integer>?) {
        super.putIntegerArrayList(key, value)
    }

    /**
     * Inserts an ArrayList<String> value into the mapping of this Bundle, replacing any existing
     * value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<String> object, or null
     */
    public override fun putStringArrayList(key: String?, value: ArrayList<String>?) {
        super.putStringArrayList(key, value)
    }

    /**
     * Inserts an ArrayList<CharSequence> value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<CharSequence> object, or null
     */
    public override fun putCharSequenceArrayList(key: String?, value: ArrayList<CharSequence>?) {
        super.putCharSequenceArrayList(key, value)
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing any existing value
     * for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Serializable object, or null
     */
    public override fun putSerializable(key: String?, value: Serializable?) {
        super.putSerializable(key, value)
    }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a byte array object, or null
     */
    public override fun putByteArray(key: String?, value: ByteArray?) {
        super.putByteArray(key, value)
    }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a short array object, or null
     */
    public override fun putShortArray(key: String?, value: ShortArray?) {
        super.putShortArray(key, value)
    }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a char array object, or null
     */
    public override fun putCharArray(key: String?, value: CharArray?) {
        super.putCharArray(key, value)
    }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing any existing value for
     * the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a float array object, or null
     */
    public override fun putFloatArray(key: String?, value: FloatArray?) {
        super.putFloatArray(key, value)
    }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing any existing
     * value for the given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence array object, or null
     */
    public override fun putCharSequenceArray(key: String?, value: Array<CharSequence>?) {
        super.putCharSequenceArray(key, value)
    }

    /**
     * Inserts a Bundle value into the mapping of this Bundle, replacing any existing value for the
     * given key. Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Bundle object, or null
     */
    fun putBundle(key: String?, value: Bundle?) {
        mMap.put(key, value)
    }

    /**
     * Returns the value associated with the given key, or (byte) 0 if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @return a byte value
     */
    public override fun getByte(key: String?): Byte {
        return super.getByte(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a byte value
     */
    public override fun getByte(key: String?, defaultValue: Byte): Byte {
        return super.getByte(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or (char) 0 if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @return a char value
     */
    public override fun getChar(key: String?): Char {
        return super.getChar(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a char value
     */
    public override fun getChar(key: String?, defaultValue: Char): Char {
        return super.getChar(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or (short) 0 if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @return a short value
     */
    public override fun getShort(key: String?): Short {
        return super.getShort(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a short value
     */
    public override fun getShort(key: String?, defaultValue: Short): Short {
        return super.getShort(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or 0.0f if no mapping of the desired type
     * exists for the given key.
     *
     * @param key a String
     * @return a float value
     */
    public override fun getFloat(key: String?): Float {
        return super.getFloat(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a float value
     */
    public override fun getFloat(key: String?, defaultValue: Float): Float {
        return super.getFloat(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharSequence value, or null
     */
    public override fun getCharSequence(key: String?): CharSequence? {
        return super.getCharSequence(key)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no mapping of the desired
     * type exists for the given key or if a null value is explicitly associatd with the given key.
     *
     * @param key a String, or null
     * @param defaultValue Value to return if key does not exist or if a null value is associated
     *   with the given key.
     * @return the CharSequence value associated with the given key, or defaultValue if no valid
     *   CharSequence object is currently mapped to that key.
     */
    public override fun getCharSequence(key: String?, defaultValue: CharSequence?): CharSequence? {
        return super.getCharSequence(key, defaultValue)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Bundle value, or null
     */
    fun getBundle(key: String?): Bundle? {
        val o = mMap.get(key)
        if (o == null) {
            return null
        }
        try {
            return o as Bundle
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Bundle", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or {@code null} if no mapping of the desired
     * type exists for the given key or a {@code null} value is explicitly associated with the key.
     *
     * <p><b>Note: </b> if the expected value is not a class provided by the Android platform, you
     * must call {@link #setClassLoader(ClassLoader)} with the proper {@link ClassLoader} first.
     * Otherwise, this method might throw an exception or return {@code null}.
     *
     * @param key a String, or {@code null}
     * @return a Parcelable value, or {@code null}
     * @deprecated Use the type-safer {@link #getParcelable(String, Class)} starting from Android
     *   {@link Build.VERSION_CODES#TIRAMISU}.
     */
    @Suppress("DEPRECATION")
    @Deprecated("Use the typesafer getParcelable(String, Class)")
    fun <T : Parcelable> getParcelable(key: String?): T? {
        val o = getValue(key)
        if (o == null) {
            return null
        }
        try {
            return o as T
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Parcelable", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key or {@code null} if:
     * <ul>
     * <li>No mapping of the desired type exists for the given key.
     * <li>A {@code null} value is explicitly associated with the key.
     * <li>The object is not of type {@code clazz}.
     * </ul>
     *
     * <p><b>Note: </b> if the expected value is not a class provided by the Android platform, you
     * must call {@link #setClassLoader(ClassLoader)} with the proper {@link ClassLoader} first.
     * Otherwise, this method might throw an exception or return {@code null}.
     *
     * <p><b>Warning: </b> the class that implements {@link Parcelable} has to be the immediately
     * enclosing class of the runtime type of its CREATOR field (that is, {@link
     * Class#getEnclosingClass()} has to return the parcelable implementing class), otherwise this
     * method might throw an exception. If the Parcelable class does not enclose the CREATOR, use
     * the deprecated {@link #getParcelable(String)} instead.
     *
     * @param key a String, or {@code null}
     * @param clazz The type of the object expected
     * @return a Parcelable value, or {@code null}
     */
    fun <T> getParcelable(key: String?, clazz: Class<T>): T? {
        // The reason for not using <T extends Parcelable> is because the caller could provide a
        // super class to restrict the children that doesn't implement Parcelable itself while the
        // children do, more details at b/210800751 (same reasoning applies here).
        return get(key, clazz)
    }

    /**
     * Returns the value associated with the given key, or {@code null} if no mapping of the desired
     * type exists for the given key or a null value is explicitly associated with the key.
     *
     * <p><b>Note: </b> if the expected value is not a class provided by the Android platform, you
     * must call {@link #setClassLoader(ClassLoader)} with the proper {@link ClassLoader} first.
     * Otherwise, this method might throw an exception or return {@code null}.
     *
     * @param key a String, or {@code null}
     * @return a Array<Parcelable> value, or {@code null}
     * @deprecated Use the type-safer {@link #getParcelableArray(String, Class)} starting from
     *   Android {@link Build.VERSION_CODES#TIRAMISU}.
     */
    @Suppress("DEPRECATION")
    @Deprecated("Use the typesafer getParcelableArray(String, Class)")
    fun getParcelableArray(key: String?): Array<Parcelable>? {
        val o = getValue(key)
        if (o == null) {
            return null
        }
        try {
            return o as Array<Parcelable>
        } catch (e: ClassCastException) {
            typeWarning(key, o, "Array<Parcelable>", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or {@code null} if:
     * <ul>
     * <li>No mapping of the desired type exists for the given key.
     * <li>A {@code null} value is explicitly associated with the key.
     * <li>The object is not of type {@code clazz}.
     * </ul>
     *
     * <p><b>Note: </b> if the expected value is not a class provided by the Android platform, you
     * must call {@link #setClassLoader(ClassLoader)} with the proper {@link ClassLoader} first.
     * Otherwise, this method might throw an exception or return {@code null}.
     *
     * <p><b>Warning: </b> if the list contains items implementing the {@link Parcelable} interface,
     * the class that implements {@link Parcelable} has to be the immediately enclosing class of the
     * runtime type of its CREATOR field (that is, {@link Class#getEnclosingClass()} has to return
     * the parcelable implementing class), otherwise this method might throw an exception. If the
     * Parcelable class does not enclose the CREATOR, use the deprecated
     * {@link #getParcelableArray(String)} instead.
     *
     * @param key a String, or {@code null}
     * @param clazz The type of the items inside the array. This is only verified when unparceling.
     * @return a Array<Parcelable> value, or {@code null}
     */
    fun <T> getParcelableArray(key: String?, clazz: Class<T>): Array<T>? {
        // The reason for not using <T extends Parcelable> is because the caller could provide a
        // super class to restrict the children that doesn't implement Parcelable itself while the
        // children do, more details at b/210800751 (same reasoning applies here).
        try {
            // In Java 12, we can pass clazz.arrayType() instead of Array<Parcelable> and later
            // casting.
            return getValue(key, Array<Parcelable>::class.java, requireNonNull(clazz)) as Array<T>
        } catch (e: ClassCastException) {
            typeWarning(key, clazz.getCanonicalName() + "[]", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or {@code null} if no mapping of the desired
     * type exists for the given key or a {@code null} value is explicitly associated with the key.
     *
     * <p><b>Note: </b> if the expected value is not a class provided by the Android platform, you
     * must call {@link #setClassLoader(ClassLoader)} with the proper {@link ClassLoader} first.
     * Otherwise, this method might throw an exception or return {@code null}.
     *
     * @param key a String, or {@code null}
     * @return an ArrayList<T> value, or {@code null}
     * @deprecated Use the type-safer {@link #getParcelableArrayList(String, Class)} starting from
     *   Android {@link Build.VERSION_CODES#TIRAMISU}.
     */
    @Suppress("DEPRECATION")
    @Deprecated("Use the typesafer getParcelableArrayList(String, Class")
    fun <T : Parcelable> getParcelableArrayList(key: String?): ArrayList<T>? {
        val o = getValue(key)
        if (o == null) {
            return null
        }
        try {
            return o as ArrayList<T>
        } catch (e: ClassCastException) {
            typeWarning(key, o, "ArrayList", e)
            return null
        }
    }

    /**
     * Returns the value associated with the given key, or {@code null} if:
     * <ul>
     * <li>No mapping of the desired type exists for the given key.
     * <li>A {@code null} value is explicitly associated with the key.
     * <li>The object is not of type {@code clazz}.
     * </ul>
     *
     * <p><b>Note: </b> if the expected value is not a class provided by the Android platform, you
     * must call {@link #setClassLoader(ClassLoader)} with the proper {@link ClassLoader} first.
     * Otherwise, this method might throw an exception or return {@code null}.
     *
     * <p><b>Warning: </b> if the list contains items implementing the {@link Parcelable} interface,
     * the class that implements {@link Parcelable} has to be the immediately enclosing class of the
     * runtime type of its CREATOR field (that is, {@link Class#getEnclosingClass()} has to return
     * the parcelable implementing class), otherwise this method might throw an exception. If the
     * Parcelable class does not enclose the CREATOR, use the deprecated
     * {@link #getParcelableArrayList(String)} instead.
     *
     * @param key a String, or {@code null}
     * @param clazz The type of the items inside the array list. This is only verified when
     *   unparceling.
     * @return an ArrayList<T> value, or {@code null}
     */
    fun <T> getParcelableArrayList(key: String?, clazz: Class<out T>): ArrayList<T>? {
        // The reason for not using <T extends Parcelable> is because the caller could provide a
        // super class to restrict the children that doesn't implement Parcelable itself while the
        // children do, more details at b/210800751 (same reasoning applies here).
        return getArrayList(key, clazz)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Serializable value, or null
     * @deprecated Use the type-safer {@link #getSerializable(String, Class)} starting from Android
     *   {@link Build.VERSION_CODES#TIRAMISU}.
     */
    @Suppress("DEPRECATION")
    @Deprecated("Use the typesafer getSeriablizable(String, Class)")
    override fun getSerializable(key: String?): Serializable? {
        return super.getSerializable(key)
    }

    /**
     * Returns the value associated with the given key, or {@code null} if:
     * <ul>
     * <li>No mapping of the desired type exists for the given key.
     * <li>A {@code null} value is explicitly associated with the key.
     * <li>The object is not of type {@code clazz}.
     * </ul>
     *
     * @param key a String, or null
     * @param clazz The expected class of the returned type
     * @return a Serializable value, or null
     */
    override fun <T : Serializable> getSerializable(key: String?, clazz: Class<T>): T? {
        return super.getSerializable(key, requireNonNull(clazz))
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
     */
    override fun getIntegerArrayList(key: String?): ArrayList<Int>? {
        return super.getIntegerArrayList(key)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
     */
    override fun getStringArrayList(key: String?): ArrayList<String>? {
        return super.getStringArrayList(key)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<CharSequence> value, or null
     */
    override fun getCharSequenceArrayList(key: String?): ArrayList<CharSequence>? {
        return super.getCharSequenceArrayList(key)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a byte array value, or null
     */
    override fun getByteArray(key: String?): ByteArray? {
        return super.getByteArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a short array value, or null
     */
    override fun getShortArray(key: String?): ShortArray? {
        return super.getShortArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a char array value, or null
     */
    @Override
    override fun getCharArray(key: String?): CharArray? {
        return super.getCharArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a float array value, or null
     */
    override fun getFloatArray(key: String?): FloatArray? {
        return super.getFloatArray(key)
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of the desired type
     * exists for the given key or a null value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Array<CharSequence> value, or null
     */
    override fun getCharSequenceArray(key: String?): Array<CharSequence>? {
        return super.getCharSequenceArray(key)
    }

    /**
     * Returns a string representation of the {@link Bundle} that may be suitable for debugging. It
     * won't print the internal map if its content hasn't been unparcelled.
     */
    override fun toString(): String {
        synchronized(this) {
            return "Bundle[" + mMap.toString() + "]"
        }
    }

    /** @hide */
    fun toShortString(): String {
        synchronized(this) {
            return mMap.toString()
        }
    }
}
