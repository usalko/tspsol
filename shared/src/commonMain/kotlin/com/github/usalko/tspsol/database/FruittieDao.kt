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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.usalko.tspsol.model.Tspsol
import kotlinx.coroutines.flow.Flow

@Dao
interface TspsolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tspsol: Tspsol)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tspsol: List<Tspsol>)

    @Query("SELECT * FROM Tspsol")
    fun getAllAsFlow(): Flow<List<Tspsol>>

    @Query("SELECT COUNT(*) as count FROM Tspsol")
    suspend fun count(): Int

    @Query("SELECT * FROM Tspsol WHERE id in (:ids)")
    suspend fun loadAll(ids: List<Long>): List<Tspsol>

    @Query("SELECT * FROM Tspsol WHERE id in (:ids)")
    suspend fun loadMapped(ids: List<Long>): Map<
        @MapColumn(columnName = "id")
        Long,
        Tspsol,
        >
}
