/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.usalko.tspsol.database

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import com.github.usalko.tspsol.di.json
import com.github.usalko.tspsol.model.Tspsol
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.use

@Serializable
data class Cart(
    val items: List<CartItem>,
)
@Serializable
data class CartItem(
    val id: Long,
    val count: Int,
)
internal object CartJsonSerializer : OkioSerializer<Cart> {
    override val defaultValue: Cart = Cart(emptyList())
    override suspend fun readFrom(source: BufferedSource): Cart {
        return json.decodeFromString<Cart>(source.readUtf8())
    }
    override suspend fun writeTo(t: Cart, sink: BufferedSink) {
        sink.use {
            it.writeUtf8(json.encodeToString(Cart.serializer(), t))
        }
    }
}
class CartDataStore(
    private val produceFilePath: () -> String,
) {
    private val db = DataStoreFactory.create(
        storage = OkioStorage<Cart>(
            fileSystem = FileSystem.SYSTEM,
            serializer = CartJsonSerializer,
            producePath = {
                produceFilePath().toPath()
            },
        ),
    )
    val cart: Flow<Cart>
        get() = db.data
    suspend fun add(tspsol: Tspsol) = update(tspsol, 1)
    suspend fun remove(tspsol: Tspsol) = update(tspsol, -1)
    suspend fun update(tspsol: Tspsol, diff: Int) {
        db.updateData { prevCart ->
            val newItems = mutableListOf<CartItem>()
            var found = false
            prevCart.items.forEach {
                if (it.id == tspsol.id) {
                    found = true
                    newItems.add(
                        it.copy(
                            count = it.count + diff,
                        ),
                    )
                } else {
                    newItems.add(it)
                }
            }
            if (!found) {
                newItems.add(
                    CartItem(id = tspsol.id, count = diff),
                )
            }
            newItems.removeAll {
                it.count <= 0
            }
            Cart(
                items = newItems,
            )
        }
    }
}
