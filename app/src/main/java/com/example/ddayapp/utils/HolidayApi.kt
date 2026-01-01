package com.example.ddayapp.utils

import com.example.ddayapp.data.Holiday
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.ddayapp.BuildConfig


/**
 * 한국천문연구원 특일 정보 API
 */
interface HolidayApiService {
    @GET("service/SpcdeInfoService/getRestDeInfo")
    suspend fun getHolidays(
        @Query("solYear") year: String,
        @Query("ServiceKey", encoded = true) serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 50,
        @Query("pageNo") pageNo: Int = 1,
        @Query("_type") type: String = "json"
    ): HolidayResponse
}

data class HolidayResponse(
    @SerializedName("response") val response: Response?
)

data class Response(
    @SerializedName("body") val body: Body?
)

data class Body(
    @SerializedName("items") val items: Items?
)

data class Items(
    @SerializedName("item") val item: List<HolidayItem>?
)

data class HolidayItem(
    @SerializedName("locdate") val locdate: Int,
    @SerializedName("dateName") val dateName: String?
)

object HolidayApi {
    
    private const val BASE_URL = "https://apis.data.go.kr/B090041/openapi/"
    private const val SERVICE_KEY = BuildConfig.KOREA_HOLIDAY_API_KEY

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val service = retrofit.create(HolidayApiService::class.java)
    
    /**
     * 공휴일 가져오기
     * @param year 연도 (예: "2024")
     * @return 공휴일 리스트
     */
    suspend fun fetchHolidays(year: String): Result<List<Holiday>> {
        return try {
            val response = service.getHolidays(
                year = year,
                serviceKey = SERVICE_KEY
            )
            
            val items = response.response?.body?.items?.item ?: emptyList()
            
            val holidays = items.mapNotNull { item ->
                val locdate = item.locdate.toString()
                if (locdate.length == 8) {
                    val date = "${locdate.substring(0, 4)}-${locdate.substring(4, 6)}-${locdate.substring(6, 8)}"
                    Holiday(date = date, name = item.dateName ?: "")
                } else {
                    null
                }
            }
            
            Result.success(holidays)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
