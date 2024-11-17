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
package com.github.usalko.tspsol

import com.github.usalko.tspsol.database.AppDatabase
import com.github.usalko.tspsol.database.CartDataStore
import com.github.usalko.tspsol.model.CartItemDetails
import com.github.usalko.tspsol.model.Tspsol
import com.github.usalko.tspsol.network.TspsolApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class DataRepository(
    private val api: TspsolApi,
    private var database: AppDatabase,
    private val cartDataStore: CartDataStore,
    private val scope: CoroutineScope,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val cartDetails: Flow<List<CartItemDetails>>
        get() = cartDataStore.cart.mapLatest {
            val ids = it.items.map { it.id }
            val tspsol = database.tspsolDao().loadMapped(ids)
            it.items.mapNotNull {
                tspsol[it.id]?.let { tspsol ->
                    CartItemDetails(tspsol, it.count)
                }
            }
        }

    suspend fun addToCart(tspsol: Tspsol) {
        cartDataStore.add(tspsol)
    }

    fun getData(): Flow<List<Tspsol>> {
        scope.launch {
            if (database.tspsolDao().count() < 1) {
                refreshData()
            }
        }
        return loadData()
    }

    fun loadData(): Flow<List<Tspsol>> {
        return database.tspsolDao().getAllAsFlow()
    }

    suspend fun refreshData() {
        val response = api.getData()
        database.tspsolDao().insert(response.feed)
    }
}
