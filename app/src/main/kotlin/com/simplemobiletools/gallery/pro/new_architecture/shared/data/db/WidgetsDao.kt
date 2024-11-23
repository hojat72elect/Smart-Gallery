package com.simplemobiletools.gallery.pro.new_architecture.shared.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.Widget

@Dao
interface WidgetsDao {
    @Query("SELECT * FROM widgets")
    fun getWidgets(): List<Widget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(widget: Widget): Long

    @Query("DELETE FROM widgets WHERE widget_id = :widgetId")
    fun deleteWidgetId(widgetId: Int)
}
