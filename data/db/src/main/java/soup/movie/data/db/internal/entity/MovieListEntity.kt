package soup.movie.data.db.internal.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_movie_list")
internal data class MovieListEntity(
    @PrimaryKey
    val type: String,
    val lastUpdateTime: Long,
    val list: List<MovieEntity>
) {

    companion object {

        const val TYPE_NOW = "type_now"
        const val TYPE_PLAN = "type_plan"
    }
}
