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

expect open class BaseBundle {
    /** Constructs a new, empty Bundle. */
    constructor()

    /**
     * Constructs a new, empty [Bundle] sized to hold the given number of elements. The [Bundle]
     * will grow as needed.
     *
     * @param capacity the initial capacity of the bundle.
     */
    constructor(capacity: Int)

    /**
     * Constructs a [Bundle] containing a copy of the mappings from the given [Bundle].
     *
     * @param b the bundle to be copied.
     */
    constructor(b: BaseBundle)

    /**
     * Constructs a [BaseBundle] containing a copy of [from].
     *
     * @param from The bundle to be copied.
     * @param deep Whether to perform a deep or shallow copy.
     * @hide
     */
    constructor(from: BaseBundle, deep: Boolean)

    /**
     * Constructs a new, empty [Bundle] that uses a specific [ClassLoader] for instantiating
     * [Parcelable] and [Serializable] objects.
     *
     * @param loader An explicit [ClassLoader] to use when instantiating objects inside of the
     *   [Bundle].
     */
    constructor(loader: ClassLoader?)

    /**
     * Constructs a new, empty Bundle that uses a specific [ClassLoader] for instantiating
     * [Parcelable] and [Serializable] objects.
     *
     * @param loader An explicit [ClassLoader] to use when instantiating objects inside of the
     *   [Bundle].
     * @param capacity the initial capacity of the bundle.
     */
    constructor(loader: ClassLoader?, capacity: Int)
}
