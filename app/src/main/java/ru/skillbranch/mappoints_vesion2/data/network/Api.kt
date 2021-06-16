package ru.skillbranch.mappoints_vesion2.data.network

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query
import ru.skillbranch.mappoints_vesion2.data.network.pojo.geocode.NetworkStreet
import ru.skillbranch.mappoints_vesion2.data.network.pojo.path.RouteResponse

interface Api {

      @GET("geocode/json")
      fun getStreetByLatLng(@Query("latlng") latlng : String,
                            @Query("sensor") sensor : Boolean = false,
                            @Query("language") language : String = "ru"
      ): Deferred<NetworkStreet>


      @GET("geocode/json")
      fun getSteetByName(@Query("address") address : String,
                         @Query("sensor") sensor : Boolean = false,
                         @Query("language") language : String = "ru"
      ) : Deferred<NetworkStreet>

      @GET("directions/json")
      fun getRouteResponse(
          @Query("origin") origin : String,
          @Query("destination") destination : String,
          @Query("sensor") sensor : Boolean = false,
          @Query("language") language : String = "ru"
      ) : Deferred<RouteResponse>

}