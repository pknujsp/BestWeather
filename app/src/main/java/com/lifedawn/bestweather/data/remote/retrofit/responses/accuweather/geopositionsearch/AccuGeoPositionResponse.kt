package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.geopositionsearch

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AccuGeoPositionResponse {
    @Expose @SerializedName("Code") var code: String? = null
        private set
    @Expose @SerializedName("Version") var version: String? = null
    @Expose @SerializedName("Key") var key: String? = null
    @Expose @SerializedName("Type") var type: String? = null
        private set
    @Expose @SerializedName("Rank") var rank: String? = null
    @Expose @SerializedName("LocalizedName") var localizedName: String? = null
    @Expose @SerializedName("EnglishName") var englishName: String? = null
    @Expose @SerializedName("PrimaryPostalCode") var primaryPostalCode: String? = null
    @Expose @SerializedName("Region") var region: Region? = null
    @Expose @SerializedName("Country") var country: Country? = null
    @Expose @SerializedName("AdministrativeArea") var administrativeArea: AdministrativeArea? = null
    @Expose @SerializedName("TimeZone") var timeZone: TimeZone? = null
    @Expose @SerializedName("GeoPosition") var geoPosition: GeoPosition? = null
    @Expose @SerializedName("ParentCity") var parentCity: ParentCity? = null

    class Region {
        @Expose @SerializedName("ID") var id: String? = null
        @Expose @SerializedName("LocalizedName") var localizedName: String? = null
        @Expose @SerializedName("EnglishName") var englishName: String? = null
    }

    class Country {
        @Expose @SerializedName("ID") var id: String? = null
        @Expose @SerializedName("LocalizedName") var localizedName: String? = null
        @Expose @SerializedName("EnglishName") var englishName: String? = null
    }

    class AdministrativeArea {
        @Expose @SerializedName("ID") var id: String? = null
        @Expose @SerializedName("LocalizedName") var localizedName: String? = null
        @Expose @SerializedName("EnglishName") var englishName: String? = null
        @Expose @SerializedName("Level") var level: String? = null
        @Expose @SerializedName("LocalizedType") var localizedType: String? = null
        @Expose @SerializedName("EnglishType") var englishType: String? = null
        @Expose @SerializedName("CountryID") var countryID: String? = null
    }

    class TimeZone {
        @Expose @SerializedName("Code") var code: String? = null
        @Expose @SerializedName("Name") var name: String? = null
        @Expose @SerializedName("GmtOffset") var gmtOffset: String? = null
        @Expose @SerializedName("IsDaylightSaving") var isDaylightSaving: String? = null
        @Expose @SerializedName("NextOffsetChange") var nextOffsetChange: String? = null
    }

    class GeoPosition {
        @Expose @SerializedName("Latitude") var latitude: String? = null
        @Expose @SerializedName("Longitude") var longitude: String? = null
        @Expose @SerializedName("Elevation") var elevation: Elevation? = null

        class Elevation {
            @Expose @SerializedName("Metric") var metric: Metric? = null
            @Expose @SerializedName("Imperial") var imperial: Imperial? = null

            class Metric {
                @Expose @SerializedName("Value") var value: String? = null
                @Expose @SerializedName("Unit") var unit: String? = null
                @Expose @SerializedName("UnitType") var unitType: String? = null
            }

            class Imperial {
                @Expose @SerializedName("Value") var value: String? = null
                @Expose @SerializedName("Unit") var unit: String? = null
                @Expose @SerializedName("UnitType") var unitType: String? = null
            }
        }
    }

    @Expose @SerializedName("IsAlias") var isAlias: String? = null

    class ParentCity {
        @Expose @SerializedName("Key") var key: String? = null
        @Expose @SerializedName("LocalizedName") var localizedName: String? = null
        @Expose @SerializedName("EnglishName") var englishName: String? = null
    }

    @Expose @SerializedName("SupplementalAdminAreas") var supplementalAdminAreas: List<SupplementalAdminAreas>? = null
        private set

    class SupplementalAdminAreas {
        @Expose @SerializedName("Level") var level: String? = null
        @Expose @SerializedName("LocalizedName") var localizedName: String? = null
        @Expose @SerializedName("EnglishName") var englishName: String? = null
    }

    @Expose @SerializedName("DataSets") var dataSets: List<String>? = null
    fun setCode(code: String?): AccuGeoPositionResponse {
        this.code = code
        return this
    }

    fun setType(type: String?): AccuGeoPositionResponse {
        this.type = type
        return this
    }

    fun setSupplementalAdminAreas(supplementalAdminAreas: List<SupplementalAdminAreas>?): AccuGeoPositionResponse {
        this.supplementalAdminAreas = supplementalAdminAreas
        return this
    }
}