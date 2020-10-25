//package com.example.skynetapp.api
//
//import retrofit2.Call
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Multipart
//import retrofit2.http.POST
//import retrofit2.http.Part
//
//interface APIInterface {
//    @Multipart
//    @POST("submit")
//    fun uploadImage(
//        @Part file: MultipartBody.Part
//    ): Call<uploadResponse>
//
//    companion object {
//        operator fun invoke(): APIInterface {
//            return Retrofit.Builder()
//                .baseUrl("http://89.39.144.160:5000/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//                .create(APIInterface::class.java)
//        }
//    }
//}