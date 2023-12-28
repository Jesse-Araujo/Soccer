package com.example.soocer.weather

import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

enum class WeatherType() {
    SUNNY,
    RAINY,
    CLOUDY,
    ERROR,
}

class Weather(
    val temp: Double,
    val main: WeatherType,
    val lat: Double,
    val lng: Double,
) {
    companion object {

        private val client = OkHttpClient()
        private val weather : Weather? = null

        fun getWeather(
            date: LocalDateTime, lat: Double, lng: Double, uiVar: MutableState<Weather>
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val currentDate = LocalDate.now()
            when {
                date.toLocalDate() == currentDate ->getTodayWeather(lat,lng,uiVar)
                date.toLocalDate() == currentDate.plusDays(1) -> get5dayForecast(lat,lng,1,uiVar)
                date.toLocalDate() == currentDate.plusDays(2) -> get5dayForecast(lat,lng,2,uiVar)
                date.toLocalDate() == currentDate.plusDays(3) -> get5dayForecast(lat,lng,3,uiVar)
                date.toLocalDate() == currentDate.plusDays(4) -> get5dayForecast(lat,lng,4,uiVar)
                date.toLocalDate() == currentDate.plusDays(5) -> get5dayForecast(lat,lng,4,uiVar)
                date.toLocalDate() == currentDate.plusDays(5) -> getForecast56(lat,lng,5,uiVar)
                else -> getForecast56(lat,lng,6,uiVar)
            }
        }

        fun getTodayWeather(
            lat: Double,
            lng: Double,onFinished:MutableState<Weather>
        ){
            val APIKEY = "4cad2a7dce155a12176dd5bd6651e96f"
            val url =
                "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lng&exclude=hourly,daily&units=metric&appid=$APIKEY"
            val request = Request.Builder()
                .url(url)
                .build()
            //Log.d("vou chamar a openweathermap para sacar os de hoje","")
            callAPI(request,0,onFinished)
        }

        private fun getWeatherTodayClass(body: String?): Weather? {
            if (body == null) {
                return null
            }
            val output = JSONObject(body)
            val jsonArray = output.getJSONArray("weather") as JSONArray
            val jsonObject = jsonArray.getJSONObject(0)
            return Weather(
                output.getJSONObject("main").getDouble("temp"),
                changeWeatherDescriptionToWeatherType(
                    jsonObject.getString("main")
                ),
                output.getJSONObject("coord").getDouble("lat"),
                output.getJSONObject("coord").getDouble("lon")
            )
        }


        fun getForecast56(lat: Double, lng: Double, day: Int, onFinished: MutableState<Weather>) {
            val APIKEY = "03587bde9058494791f59d10c9866b42"
            val url =
                "https://api.weatherbit.io/v2.0/forecast/daily?&lat=$lat&lon=$lng&key=$APIKEY"
            val request = Request.Builder()
                .url(url)
                .build()
            callAPI(request,day,onFinished)
        }

        fun getForecast56WeatherClass(body: String?, day: Int): Weather? {
            if (body == null) {
                return null
            }
            val output = JSONObject(body)
            val jsonArray = output.getJSONArray("data") as JSONArray
            val jsonObject = jsonArray.getJSONObject(day)
            return Weather(
                jsonObject.getDouble("max_temp"),
                changeWeatherDescriptionToWeatherType(
                    jsonObject.getJSONObject("weather").getString("description")
                ),
                output.getDouble("lat"),
                output.getDouble("lon")
            )
        }

        fun callAPI(request: Request,day : Int,onFinished: MutableState<Weather>) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = client.newCall(request).execute()
                    val w = when (day) {
                        0 -> getWeatherTodayClass(response.body?.string())
                        5 -> getForecast56WeatherClass(response.body?.string(),day)
                        6 -> getForecast56WeatherClass(response.body?.string(),day)
                        else -> get5dayForecastWeatherClass(response.body?.string(),day)
                    }

                    withContext(Dispatchers.Main) {
                        //Log.d("deu godji",w.toString())
                        onFinished.value = w!!
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        //Log.d("deu F",e.printStackTrace().toString())
                        onFinished.value = Weather(0.0,WeatherType.ERROR,0.0,0.0)
                    }
                }
            }
        }

        fun get5dayForecast(
            lat: Double,
            lng: Double,
            day: Int,
            onFinished: MutableState<Weather>
        ) {
            val APIKEY = "ccGhAmJYn1azK93KCtB11xC2LNicQhM3"
            val url =
                "https://api.tomorrow.io/v4/weather/forecast?location=$lat,$lng&apikey=$APIKEY"
            val request = Request.Builder()
                .url(url)
                .build()
            callAPI(request,day,onFinished)
        }

        private fun get5dayForecastWeatherClass(body: String?, day: Int): Weather? {
            if (body == null) {
                return null
            }
            //Log.d("vou chamar a api.tomorrow",body)
            val output = JSONObject(body)
            val jsonArray = output.getJSONObject("timelines").getJSONArray("daily") as JSONArray
            val jsonObject = jsonArray.getJSONObject(day)
            //Log.d("day -> $day",jsonArray.getJSONObject(day).toString())
            val weatherCode = jsonObject.getJSONObject("values").getInt("weatherCodeMax")
            return Weather(
                jsonObject.getJSONObject("values").getDouble("temperatureMax"),
                changeCodeToWeatherType(weatherCode),
                output.getJSONObject("location").getDouble("lat"),
                output.getJSONObject("location").getDouble("lon")
            )
        }

        private fun changeWeatherDescriptionToWeatherType(description: String): WeatherType {
            return when {
                description.contains("sun") -> return WeatherType.SUNNY
                description.contains("cloud") -> return WeatherType.CLOUDY
                description.contains("rain") -> return WeatherType.RAINY
                else -> WeatherType.CLOUDY
            }
        }

        private fun changeCodeToWeatherType(code: Int): WeatherType {
            when (code) {
                1000 -> return WeatherType.SUNNY
                1100 -> return WeatherType.SUNNY
                1103 -> return WeatherType.SUNNY
                2101 -> return WeatherType.SUNNY
                2106 -> return WeatherType.CLOUDY
                1101 -> return WeatherType.CLOUDY
                1102 -> return WeatherType.CLOUDY
                1001 -> return WeatherType.CLOUDY
                2102 -> return WeatherType.CLOUDY
                2103 -> return WeatherType.CLOUDY
                4001 -> return WeatherType.RAINY
                4200 -> return WeatherType.RAINY
                4201 -> return WeatherType.RAINY
                else -> return WeatherType.CLOUDY
            }
        }
    }


}

//fun main() = Weather.getTodayWeather(38.752663, -9.184720)