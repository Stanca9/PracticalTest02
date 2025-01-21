package com.example.practicaltest02v8

import com.google.gson.annotations.SerializedName


data class Time (

  @SerializedName("updated"    ) var updated    : String? = null,
  @SerializedName("updatedISO" ) var updatedISO : String? = null,
  @SerializedName("updateduk"  ) var updateduk  : String? = null

)