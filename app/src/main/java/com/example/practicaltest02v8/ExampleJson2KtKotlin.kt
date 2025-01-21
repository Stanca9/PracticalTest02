package com.example.practicaltest02v8

import com.google.gson.annotations.SerializedName


data class ExampleJson2KtKotlin (

  @SerializedName("time"       ) var time       : Time?   = Time(),
  @SerializedName("disclaimer" ) var disclaimer : String? = null,
  @SerializedName("bpi"        ) var bpi        : Bpi?    = Bpi()

)