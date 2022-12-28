package com.lifedawn.bestweather


data class CloudMessagingDto(var token: String, var notification: Notification? = Notification()) {
    data class Notification(var title: String? = null,
                            var body: String? = null)
}
