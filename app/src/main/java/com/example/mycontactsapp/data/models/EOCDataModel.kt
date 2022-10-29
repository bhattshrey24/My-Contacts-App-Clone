package com.example.mycontactsapp.data.models

import com.example.mycontactsapp.other.Type

data class EOCDataModel(
    var data: String = "",
    var category: String = "",
    var type: Type = Type.Phone
)