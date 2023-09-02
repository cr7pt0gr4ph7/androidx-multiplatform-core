/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.lifecycle

import androidx.annotation.MainThread
import java.io.Closeable
import java.io.IOException
// import kotlin.concurrent.Volatile

/**
 * ViewModel is a class that is responsible for preparing and managing the data for
 * an [android.app.Activity][Activity] or a [androidx.fragment.app.Fragment][Fragment].
 * It also handles the communication of the Activity / Fragment with the rest of the application
 * (e.g. calling the business logic classes).
 *
 * A ViewModel is always created in association with a scope (a fragment or an activity) and will
 * be retained as long as the scope is alive. E.g. if it is an Activity, until it is
 * finished.
 *
 * In other words, this means that a ViewModel will not be destroyed if its owner is destroyed for a
 * configuration change (e.g. rotation). The new owner instance just re-connects to the existing model.
 *
 * The purpose of the ViewModel is to acquire and keep the information that is necessary for an
 * Activity or a Fragment. The Activity or the Fragment should be able to observe changes in the
 * ViewModel. ViewModels usually expose this information via {@link LiveData} or Android Data
 * Binding. You can also use any observability construct from your favorite framework.
 *
 * ViewModel's only responsibility is to manage the data for the UI. It *should never* access
 * your view hierarchy or hold a reference back to the Activity or the Fragment.
 *
 * Typical usage from an Activity standpoint would be:
 * <pre>
 * public class UserActivity extends Activity {
 *
 *     {@literal @}Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.user_activity_layout);
 *         final UserModel viewModel = new ViewModelProvider(this).get(UserModel.class);
 *         viewModel.getUser().observe(this, new Observer&lt;User&gt;() {
 *             {@literal @}Override
 *             public void onChanged(@Nullable User data) {
 *                 // update ui.
 *             }
 *         });
 *         findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
 *             {@literal @}Override
 *             public void onClick(View v) {
 *                  viewModel.doAction();
 *             }
 *         });
 *     }
 * }
 * </pre>
 *
 * ViewModel would be:
 * <pre>
 * public class UserModel extends ViewModel {
 *     private final MutableLiveData&lt;User&gt; userLiveData = new MutableLiveData&lt;&gt;();
 *
 *     public LiveData&lt;User&gt; getUser() {
 *         return userLiveData;
 *     }
 *
 *     public UserModel() {
 *         // trigger user load.
 *     }
 *
 *     void doAction() {
 *         // depending on the action, do necessary business logic calls and update the
 *         // userLiveData.
 *     }
 * }
 * </pre>
 *
 * ViewModels can also be used as a communication layer between different Fragments of an Activity.
 * Each Fragment can acquire the ViewModel using the same key via their Activity. This allows
 * communication between Fragments in a de-coupled fashion such that they never need to talk to
 * the other Fragment directly.
 * <pre>
 * public class MyFragment extends Fragment {
 *     public void onStart() {
 *         UserModel userModel = new ViewModelProvider(requireActivity()).get(UserModel.class);
 *     }
 * }
 * </pre>
 */
public abstract class ViewModel {
    // Can't use ConcurrentHashMap, because it can lose values on old apis (see b/37042460)
    private val mBagOfTags: MutableMap<String, Any>? = mutableMapOf()
    private val mCloseables: MutableSet<Closeable>? = mutableSetOf()
    // @Volatile
    private var mCleared: Boolean = false

    /**
     * Construct a new [ViewModel] instance.
     *
     * You should *never* manually construct a [ViewModel] outside of a
     * [ViewModelProvider.Factory].
     */
    constructor() {
    }

    /**
     * Construct a new [ViewModel] instance. Any [Closeable] objects provided here
     * will be closed directly before [onCleared] is called.
     *
     * You should *never* manually construct a [ViewModel] outside of a
     * [ViewModelProvider.Factory].
     */
    constructor(vararg closeables: Closeable) {
        mCloseables!!.addAll(closeables.asList())
    }

    /**
     * Add a new [Closeable] object that will be closed directly before
     * [onCleared] is called.
     *
     * @param closeable The object that should be [Closeable.close][closed] directly before
     *                  [onCleared] is called.
     */
    open fun addCloseable(closeable: Closeable) {
        // As this method is final, it will still be called on mock objects even
        // though mCloseables won't actually be created...we'll just not do anything
        // in that case.
        if (mCloseables != null) {
            synchronized(mCloseables) {
                mCloseables.add(closeable)
            }
        }
    }

    /**
     * This method will be called when this [ViewModel] is no longer used and will be destroyed.
     *
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    protected open fun onCleared() {
    }

    internal fun invokeOnCleared() {
        onCleared()
    }

    @MainThread
    internal fun clear() {
        mCleared = true
        // Since clear() is final, this method is still called on mock objects
        // and in those cases, mBagOfTags is null. It'll always be empty though
        // because setTagIfAbsent and getTag are not final so we can skip
        // clearing it
        if (mBagOfTags != null) {
            synchronized(mBagOfTags) {
                for (value in mBagOfTags.values) {
                    // see comment for the similar call in setTagIfAbsent
                    closeWithRuntimeException(value)
                }
            }
        }
        // We need the same null check here
        if (mCloseables != null) {
            synchronized(mCloseables) {
                for (closeable in mCloseables) {
                    closeWithRuntimeException(closeable)
                }
            }
        }
        onCleared()
    }

    /**
     * Sets a tag associated with this [ViewModel] and a key.
     * If the given [newValue] is [Closeable],
     * it will be closed once [clear].
     *
     * If a value was already set for the given key, this call does nothing and
     * returns currently associated value, the given {@code newValue} would be ignored
     *
     * If the ViewModel was already cleared then [Closeable.close] would be called on the returned object if
     * it implements [Closeable]. The same object may receive multiple close calls, so method
     * should be idempotent.
     */
    internal open fun <T> setTagIfAbsent(key: String, newValue: T): T {
        var previous: T
        synchronized(mBagOfTags!!) {
            previous = mBagOfTags.get(key) as T
            if (previous == null) {
                mBagOfTags.put(key, newValue as Any)
            }
        }
        val result = previous ?: newValue
        if (mCleared) {
            // It is possible that we'll call close() multiple times on the same object, but
            // Closeable interface requires close method to be idempotent:
            // "if the stream is already closed then invoking this method has no effect." (c)
            closeWithRuntimeException(result as Any)
        }
        return result
    }

    /**
     * Returns the tag associated with this viewmodel and the specified key.
     */
    internal open fun <T> getTag(key: String): T? {
        if (mBagOfTags == null) {
            return null
        }
        synchronized(mBagOfTags) {
            return mBagOfTags.get(key) as T?
        }
    }

    private fun closeWithRuntimeException(obj: Any) {
        if (obj is Closeable) {
            try {
                (obj as Closeable).close()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }
}
