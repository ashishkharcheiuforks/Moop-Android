package soup.movie.data.source.local

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import soup.movie.data.model.Movie
import soup.movie.data.model.Theater
import soup.movie.data.model.Timetable
import soup.movie.data.model.Version
import soup.movie.data.model.response.CachedMovieList
import soup.movie.data.model.response.CachedMovieList.Companion.TYPE_NOW
import soup.movie.data.model.response.CachedMovieList.Companion.TYPE_PLAN
import soup.movie.data.model.response.CodeResponse
import soup.movie.data.model.response.MovieListResponse
import soup.movie.data.source.MoopDataSource
import soup.movie.data.util.toAnObservable

class LocalMoopDataSource(private val moopDao: MoopDao) : MoopDataSource {

    private var codeResponse: CodeResponse? = null
    private val versionSubject: BehaviorSubject<Version> = BehaviorSubject.create()

    fun saveNowList(response: MovieListResponse) {
        saveMovieListAs(TYPE_NOW, response)
    }

    override fun getNowList(): Observable<MovieListResponse> {
        return getMovieListAs(TYPE_NOW)
    }

    fun savePlanList(response: MovieListResponse) {
        saveMovieListAs(TYPE_PLAN, response)
    }

    override fun getPlanList(): Observable<MovieListResponse> {
        return getMovieListAs(TYPE_PLAN)
    }

    private fun saveMovieListAs(type: String, response: MovieListResponse) {
        moopDao.insert(CachedMovieList(type, System.currentTimeMillis(), response.list))
    }

    private fun getMovieListAs(type: String): Observable<MovieListResponse> {
        return moopDao.findByType(type)
                .onErrorReturn { CachedMovieList.empty(type) }
                .toObservable()
                .filter { it.isUpToDate() }
                .map { MovieListResponse(it.lastUpdateTime, it.list) }
                .subscribeOn(Schedulers.io())
    }

    fun saveCodeList(response: CodeResponse) {
        codeResponse = response
    }

    override fun getCodeList(): Observable<CodeResponse> {
        return codeResponse?.toAnObservable()
                ?: Observable.empty()
    }

    override fun getTimetable(theater: Theater, movie: Movie): Observable<Timetable> = TODO()

    fun saveVersion(version: Version) {
        versionSubject.onNext(version)
    }

    override fun getVersion(): Observable<Version> {
        return versionSubject
    }
}
