/*
 * Copyright 2023 The Android Open Source Project
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
@file:Suppress("NOTHING_TO_INLINE", "RedundantVisibilityModifier")
@file:OptIn(ExperimentalContracts::class)

package androidx.collection

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmField

/**
 * [IntList] is a [List]-like collection for [Int] values. It allows retrieving
 * the elements without boxing. [IntList] is always backed by a [MutableIntList],
 * its [MutableList]-like subclass.
 *
 * This implementation is not thread-safe: if multiple threads access this
 * container concurrently, and one or more threads modify the structure of
 * the list (insertion or removal for instance), the calling code must provide
 * the appropriate synchronization. It is also not safe to mutate during reentrancy --
 * in the middle of a [forEach], for example. However, concurrent reads are safe.
 */
public sealed class IntList(initialCapacity: Int) {
    @JvmField
    @PublishedApi
    internal var content: IntArray = if (initialCapacity == 0) {
        EmptyIntArray
    } else {
        IntArray(initialCapacity)
    }

    @Suppress("PropertyName")
    @JvmField
    @PublishedApi
    internal var _size: Int = 0

    /**
     * The number of elements in the [IntList].
     */
    @get:androidx.annotation.IntRange(from = 0)
    public val size: Int
        get() = _size

    /**
     * Returns the last valid index in the [IntList]. This can be `-1` when the list is empty.
     */
    @get:androidx.annotation.IntRange(from = -1)
    public inline val lastIndex: Int get() = _size - 1

    /**
     * Returns an [IntRange] of the valid indices for this [IntList].
     */
    public inline val indices: IntRange get() = 0 until _size

    /**
     * Returns `true` if the collection has no elements in it.
     */
    public fun none(): Boolean {
        return isEmpty()
    }

    /**
     * Returns `true` if there's at least one element in the collection.
     */
    public fun any(): Boolean {
        return isNotEmpty()
    }

    /**
     * Returns `true` if any of the elements give a `true` return value for [predicate].
     */
    public inline fun any(predicate: (element: Int) -> Boolean): Boolean {
        contract { callsInPlace(predicate) }
        forEach {
            if (predicate(it)) {
                return true
            }
        }
        return false
    }

    /**
     * Returns `true` if any of the elements give a `true` return value for [predicate] while
     * iterating in the reverse order.
     */
    public inline fun reversedAny(predicate: (element: Int) -> Boolean): Boolean {
        contract { callsInPlace(predicate) }
        forEachReversed {
            if (predicate(it)) {
                return true
            }
        }
        return false
    }

    /**
     * Returns `true` if the [IntList] contains [element] or `false` otherwise.
     */
    public operator fun contains(element: Int): Boolean {
        forEach {
            if (it == element) {
                return true
            }
        }
        return false
    }

    /**
     * Returns `true` if the [IntList] contains all elements in [elements] or `false` if
     * one or more are missing.
     */
    public fun containsAll(elements: IntList): Boolean {
        for (i in elements.indices) {
            if (!contains(elements[i])) return false
        }
        return true
    }

    /**
     * Returns the number of elements in this list.
     */
    public fun count(): Int = _size

    /**
     * Counts the number of elements matching [predicate].
     * @return The number of elements in this list for which [predicate] returns true.
     */
    public inline fun count(predicate: (element: Int) -> Boolean): Int {
        contract { callsInPlace(predicate) }
        var count = 0
        forEach { if (predicate(it)) count++ }
        return count
    }

    /**
     * Returns the first element in the [IntList] or throws a [NoSuchElementException] if
     * it [isEmpty].
     */
    public fun first(): Int {
        if (isEmpty()) {
            throw NoSuchElementException("IntList is empty.")
        }
        return content[0]
    }

    /**
     * Returns the first element in the [IntList] for which [predicate] returns `true` or
     * throws [NoSuchElementException] if nothing matches.
     * @see indexOfFirst
     */
    public inline fun first(predicate: (element: Int) -> Boolean): Int {
        contract { callsInPlace(predicate) }
        forEach { item ->
            if (predicate(item)) return item
        }
        throw NoSuchElementException("IntList contains no element matching the predicate.")
    }

    /**
     * Accumulates values, starting with [initial], and applying [operation] to each element
     * in the [IntList] in order.
     * @param initial The value of `acc` for the first call to [operation] or return value if
     * there are no elements in this list.
     * @param operation function that takes current accumulator value and an element, and
     * calculates the next accumulator value.
     */
    public inline fun <R> fold(initial: R, operation: (acc: R, element: Int) -> R): R {
        contract { callsInPlace(operation) }
        var acc = initial
        forEach { item ->
            acc = operation(acc, item)
        }
        return acc
    }

    /**
     * Accumulates values, starting with [initial], and applying [operation] to each element
     * in the [IntList] in order.
     */
    public inline fun <R> foldIndexed(
        initial: R,
        operation: (index: Int, acc: R, element: Int) -> R
    ): R {
        contract { callsInPlace(operation) }
        var acc = initial
        forEachIndexed { i, item ->
            acc = operation(i, acc, item)
        }
        return acc
    }

    /**
     * Accumulates values, starting with [initial], and applying [operation] to each element
     * in the [IntList] in reverse order.
     * @param initial The value of `acc` for the first call to [operation] or return value if
     * there are no elements in this list.
     * @param operation function that takes an element and the current accumulator value, and
     * calculates the next accumulator value.
     */
    public inline fun <R> foldRight(initial: R, operation: (element: Int, acc: R) -> R): R {
        contract { callsInPlace(operation) }
        var acc = initial
        forEachReversed { item ->
            acc = operation(item, acc)
        }
        return acc
    }

    /**
     * Accumulates values, starting with [initial], and applying [operation] to each element
     * in the [IntList] in reverse order.
     */
    public inline fun <R> foldRightIndexed(
        initial: R,
        operation: (index: Int, element: Int, acc: R) -> R
    ): R {
        contract { callsInPlace(operation) }
        var acc = initial
        forEachReversedIndexed { i, item ->
            acc = operation(i, item, acc)
        }
        return acc
    }

    /**
     * Calls [block] for each element in the [IntList], in order.
     * @param block will be executed for every element in the list, accepting an element from
     * the list
     */
    public inline fun forEach(block: (element: Int) -> Unit) {
        contract { callsInPlace(block) }
        val content = content
        for (i in 0 until _size) {
            block(content[i])
        }
    }

    /**
     * Calls [block] for each element in the [IntList] along with its index, in order.
     * @param block will be executed for every element in the list, accepting the index and
     * the element at that index.
     */
    public inline fun forEachIndexed(block: (index: Int, element: Int) -> Unit) {
        contract { callsInPlace(block) }
        val content = content
        for (i in 0 until _size) {
            block(i, content[i])
        }
    }

    /**
     * Calls [block] for each element in the [IntList] in reverse order.
     * @param block will be executed for every element in the list, accepting an element from
     * the list
     */
    public inline fun forEachReversed(block: (element: Int) -> Unit) {
        contract { callsInPlace(block) }
        val content = content
        for (i in _size - 1 downTo 0) {
            block(content[i])
        }
    }

    /**
     * Calls [block] for each element in the [IntList] along with its index, in reverse
     * order.
     * @param block will be executed for every element in the list, accepting the index and
     * the element at that index.
     */
    public inline fun forEachReversedIndexed(block: (index: Int, element: Int) -> Unit) {
        contract { callsInPlace(block) }
        val content = content
        for (i in _size - 1 downTo 0) {
            block(i, content[i])
        }
    }

    /**
     * Returns the element at the given [index] or throws [IndexOutOfBoundsException] if
     * the [index] is out of bounds of this collection.
     */
    public operator fun get(@androidx.annotation.IntRange(from = 0) index: Int): Int {
        if (index !in 0 until _size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$lastIndex")
        }
        return content[index]
    }

    /**
     * Returns the element at the given [index] or throws [IndexOutOfBoundsException] if
     * the [index] is out of bounds of this collection.
     */
    public fun elementAt(@androidx.annotation.IntRange(from = 0) index: Int): Int {
        if (index !in 0 until _size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$lastIndex")
        }
        return content[index]
    }

    /**
     * Returns the element at the given [index] or [defaultValue] if [index] is out of bounds
     * of the collection.
     * @param index The index of the element whose value should be returned
     * @param defaultValue A lambda to call with [index] as a parameter to return a value at
     * an index not in the list.
     */
    public inline fun elementAtOrElse(
        @androidx.annotation.IntRange(from = 0) index: Int,
        defaultValue: (index: Int) -> Int
    ): Int {
        if (index !in 0 until _size) {
            return defaultValue(index)
        }
        return content[index]
    }

    /**
     * Returns the index of [element] in the [IntList] or `-1` if [element] is not there.
     */
    public fun indexOf(element: Int): Int {
        forEachIndexed { i, item ->
            if (element == item) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns the index if the first element in the [IntList] for which [predicate]
     * returns `true`.
     */
    public inline fun indexOfFirst(predicate: (element: Int) -> Boolean): Int {
        contract { callsInPlace(predicate) }
        forEachIndexed { i, item ->
            if (predicate(item)) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns the index if the last element in the [IntList] for which [predicate]
     * returns `true`.
     */
    public inline fun indexOfLast(predicate: (element: Int) -> Boolean): Int {
        contract { callsInPlace(predicate) }
        forEachReversedIndexed { i, item ->
            if (predicate(item)) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns `true` if the [IntList] has no elements in it or `false` otherwise.
     */
    public fun isEmpty(): Boolean = _size == 0

    /**
     * Returns `true` if there are elements in the [IntList] or `false` if it is empty.
     */
    public fun isNotEmpty(): Boolean = _size != 0

    /**
     * Returns the last element in the [IntList] or throws a [NoSuchElementException] if
     * it [isEmpty].
     */
    public fun last(): Int {
        if (isEmpty()) {
            throw NoSuchElementException("IntList is empty.")
        }
        return content[lastIndex]
    }

    /**
     * Returns the last element in the [IntList] for which [predicate] returns `true` or
     * throws [NoSuchElementException] if nothing matches.
     * @see indexOfLast
     */
    public inline fun last(predicate: (element: Int) -> Boolean): Int {
        contract { callsInPlace(predicate) }
        forEachReversed { item ->
            if (predicate(item)) {
                return item
            }
        }
        throw NoSuchElementException("IntList contains no element matching the predicate.")
    }

    /**
     * Returns the index of the last element in the [IntList] that is the same as
     * [element] or `-1` if no elements match.
     */
    public fun lastIndexOf(element: Int): Int {
        forEachReversedIndexed { i, item ->
            if (item == element) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns a hash code based on the contents of the [IntList].
     */
    override fun hashCode(): Int {
        var hashCode = 0
        forEach { element ->
            hashCode += 31 * element.hashCode()
        }
        return hashCode
    }

    /**
     * Returns `true` if [other] is a [IntList] and the contents of this and [other] are the
     * same.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is IntList || other._size != _size) {
            return false
        }
        val content = content
        val otherContent = other.content
        for (i in indices) {
            if (content[i] != otherContent[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Returns a String representation of the list, surrounded by "[]" and each element
     * separated by ", ".
     */
    override fun toString(): String {
        if (isEmpty()) {
            return "[]"
        }
        val last = lastIndex
        return buildString {
            append('[')
            val content = content
            for (i in 0 until last) {
                append(content[i])
                append(',')
                append(' ')
            }
            append(content[last])
            append(']')
        }
    }
}

/**
 * [MutableIntList] is a [MutableList]-like collection for [Int] values.
 * It allows storing and retrieving the elements without boxing. Immutable
 * access is available through its base class [IntList], which has a [List]-like
 * interface.
 *
 * This implementation is not thread-safe: if multiple threads access this
 * container concurrently, and one or more threads modify the structure of
 * the list (insertion or removal for instance), the calling code must provide
 * the appropriate synchronization. It is also not safe to mutate during reentrancy --
 * in the middle of a [forEach], for example. However, concurrent reads are safe.
 *
 * @constructor Creates a [MutableIntList] with a [capacity] of `initialCapacity`.
 */
public class MutableIntList(
    initialCapacity: Int = DefaultCapacity
) : IntList(initialCapacity) {
    /**
     * Returns the total number of elements that can be held before the [MutableIntList] must
     * grow.
     *
     * @see ensureCapacity
     */
    public inline val capacity: Int
        get() = content.size

    /**
     * Adds [element] to the [MutableIntList] and returns `true`.
     */
    public fun add(element: Int): Boolean {
        ensureCapacity(_size + 1)
        content[_size] = element
        _size++
        return true
    }

    /**
     * Adds [element] to the [MutableIntList] at the given [index], shifting over any
     * elements at [index] and after, if any.
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive
     */
    public fun add(@androidx.annotation.IntRange(from = 0) index: Int, element: Int) {
        if (index !in 0.._size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$_size")
        }
        ensureCapacity(_size + 1)
        val content = content
        if (index != _size) {
            content.copyInto(
                destination = content,
                destinationOffset = index + 1,
                startIndex = index,
                endIndex = _size
            )
        }
        content[index] = element
        _size++
    }

    /**
     * Adds all [elements] to the [MutableIntList] at the given [index], shifting over any
     * elements at [index] and after, if any.
     * @return `true` if the [MutableIntList] was changed or `false` if [elements] was empty
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive.
     */
    public fun addAll(
        @androidx.annotation.IntRange(from = 0) index: Int,
        elements: IntArray
    ): Boolean {
        if (index !in 0.._size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$_size")
        }
        if (elements.isEmpty()) return false
        ensureCapacity(_size + elements.size)
        val content = content
        if (index != _size) {
            content.copyInto(
                destination = content,
                destinationOffset = index + elements.size,
                startIndex = index,
                endIndex = _size
            )
        }
        elements.copyInto(content, index)
        _size += elements.size
        return true
    }

    /**
     * Adds all [elements] to the [MutableIntList] at the given [index], shifting over any
     * elements at [index] and after, if any.
     * @return `true` if the [MutableIntList] was changed or `false` if [elements] was empty
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive
     */
    public fun addAll(
        @androidx.annotation.IntRange(from = 0) index: Int,
        elements: IntList
    ): Boolean {
        if (index !in 0.._size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$_size")
        }
        if (elements.isEmpty()) return false
        ensureCapacity(_size + elements._size)
        val content = content
        if (index != _size) {
            content.copyInto(
                destination = content,
                destinationOffset = index + elements._size,
                startIndex = index,
                endIndex = _size
            )
        }
        elements.content.copyInto(
            destination = content,
            destinationOffset = index,
            startIndex = 0,
            endIndex = elements._size
        )
        _size += elements._size
        return true
    }

    /**
     * Adds all [elements] to the end of the [MutableIntList] and returns `true` if the
     * [MutableIntList] was changed or `false` if [elements] was empty.
     */
    public fun addAll(elements: IntList): Boolean {
        return addAll(_size, elements)
    }

    /**
     * Adds all [elements] to the end of the [MutableIntList] and returns `true` if the
     * [MutableIntList] was changed or `false` if [elements] was empty.
     */
    public fun addAll(elements: IntArray): Boolean {
        return addAll(_size, elements)
    }

    /**
     * Adds all [elements] to the end of the [MutableIntList].
     */
    public operator fun plusAssign(elements: IntList) {
        addAll(_size, elements)
    }

    /**
     * Adds all [elements] to the end of the [MutableIntList].
     */
    public operator fun plusAssign(elements: IntArray) {
        addAll(_size, elements)
    }

    /**
     * Removes all elements in the [MutableIntList]. The storage isn't released.
     * @see trim
     */
    public fun clear() {
        _size = 0
    }

    /**
     * Reduces the internal storage. If [capacity] is greater than [minCapacity] and [size], the
     * internal storage is reduced to the maximum of [size] and [minCapacity].
     * @see ensureCapacity
     */
    public fun trim(minCapacity: Int = _size) {
        val minSize = maxOf(minCapacity, _size)
        if (capacity > minSize) {
            content = content.copyOf(minSize)
        }
    }

    /**
     * Ensures that there is enough space to store [capacity] elements in the [MutableIntList].
     * @see trim
     */
    public fun ensureCapacity(capacity: Int) {
        val oldContent = content
        if (oldContent.size < capacity) {
            val newSize = maxOf(capacity, oldContent.size * 3 / 2)
            content = oldContent.copyOf(newSize)
        }
    }

    /**
     * [add] [element] to the [MutableIntList].
     */
    public inline operator fun plusAssign(element: Int) {
        add(element)
    }

    /**
     * [remove] [element] from the [MutableIntList]
     */
    public inline operator fun minusAssign(element: Int) {
        remove(element)
    }

    /**
     * Removes [element] from the [MutableIntList]. If [element] was in the [MutableIntList]
     * and was removed, `true` will be returned, or `false` will be returned if the element
     * was not found.
     */
    public fun remove(element: Int): Boolean {
        val index = indexOf(element)
        if (index >= 0) {
            removeAt(index)
            return true
        }
        return false
    }

    /**
     * Removes all [elements] from the [MutableIntList] and returns `true` if anything was removed.
     */
    public fun removeAll(elements: IntArray): Boolean {
        val initialSize = _size
        for (i in elements.indices) {
            remove(elements[i])
        }
        return initialSize != _size
    }

    /**
     * Removes all [elements] from the [MutableIntList] and returns `true` if anything was removed.
     */
    public fun removeAll(elements: IntList): Boolean {
        val initialSize = _size
        for (i in 0..elements.lastIndex) {
            remove(elements[i])
        }
        return initialSize != _size
    }

    /**
     * Removes all [elements] from the [MutableIntList].
     */
    public operator fun minusAssign(elements: IntArray) {
        elements.forEach { element ->
            remove(element)
        }
    }

    /**
     * Removes all [elements] from the [MutableIntList].
     */
    public operator fun minusAssign(elements: IntList) {
        elements.forEach { element ->
            remove(element)
        }
    }

    /**
     * Removes the element at the given [index] and returns it.
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [lastIndex], inclusive
     */
    public fun removeAt(@androidx.annotation.IntRange(from = 0) index: Int): Int {
        if (index !in 0 until _size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$lastIndex")
        }
        val content = content
        val item = content[index]
        if (index != lastIndex) {
            content.copyInto(
                destination = content,
                destinationOffset = index,
                startIndex = index + 1,
                endIndex = _size
            )
        }
        _size--
        return item
    }

    /**
     * Removes items from index [start] (inclusive) to [end] (exclusive).
     * @throws IndexOutOfBoundsException if [start] or [end] isn't between 0 and [size], inclusive
     * @throws IllegalArgumentException if [start] is greater than [end]
     */
    public fun removeRange(
        @androidx.annotation.IntRange(from = 0) start: Int,
        @androidx.annotation.IntRange(from = 0) end: Int
    ) {
        if (start !in 0.._size || end !in 0.._size) {
            throw IndexOutOfBoundsException("Start ($start) and end ($end) must be in 0..$_size")
        }
        if (end < start) {
            throw IllegalArgumentException("Start ($start) is more than end ($end)")
        }
        if (end != start) {
            if (end < _size) {
                content.copyInto(
                    destination = content,
                    destinationOffset = start,
                    startIndex = end,
                    endIndex = _size
                )
            }
            _size -= (end - start)
        }
    }

    /**
     * Keeps only [elements] in the [MutableIntList] and removes all other values.
     * @return `true` if the [MutableIntList] has changed.
     */
    public fun retainAll(elements: IntArray): Boolean {
        val initialSize = _size
        val content = content
        for (i in lastIndex downTo 0) {
            val item = content[i]
            if (elements.indexOfFirst { it == item } < 0) {
                removeAt(i)
            }
        }
        return initialSize != _size
    }

    /**
     * Keeps only [elements] in the [MutableIntList] and removes all other values.
     * @return `true` if the [MutableIntList] has changed.
     */
    public fun retainAll(elements: IntList): Boolean {
        val initialSize = _size
        val content = content
        for (i in lastIndex downTo 0) {
            val item = content[i]
            if (item !in elements) {
                removeAt(i)
            }
        }
        return initialSize != _size
    }

    /**
     * Sets the value at [index] to [element].
     * @return the previous value set at [index]
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [lastIndex], inclusive
     */
    public operator fun set(
        @androidx.annotation.IntRange(from = 0) index: Int,
        element: Int
    ): Int {
        if (index !in 0 until _size) {
            throw IndexOutOfBoundsException("set index $index must be between 0 .. $lastIndex")
        }
        val content = content
        val old = content[index]
        content[index] = element
        return old
    }

    /**
     * Sorts the [MutableIntList] elements in ascending order.
     */
    public fun sort() {
        content.sort(fromIndex = 0, toIndex = _size)
    }

    /**
     * Sorts the [MutableIntList] elements in descending order.
     */
    public fun sortDescending() {
        content.sortDescending(fromIndex = 0, toIndex = _size)
    }
}

@Suppress("ConstPropertyName")
private const val DefaultCapacity = 16

// Empty array used when nothing is allocated
@Suppress("PrivatePropertyName")
private val EmptyIntArray = IntArray(0)

/**
 * Creates and returns an empty [MutableIntList] with the default capacity.
 */
public inline fun mutableIntListOf(): MutableIntList = MutableIntList()

/**
 * Creates and returns a [MutableIntList] with the given values.
 */
public inline fun mutableIntListOf(vararg elements: Int): MutableIntList =
    MutableIntList(elements.size).also { it.addAll(elements) }
