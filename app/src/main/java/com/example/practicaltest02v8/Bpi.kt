package com.example.practicaltest02v8

import com.google.gson.annotations.SerializedName


data class Bpi (

  @SerializedName("USD" ) var USD : USD? = USD(),
  @SerializedName("EUR" ) var EUR : EUR? = EUR()

)