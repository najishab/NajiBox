package io.najishab.najibox.bg

import java.io.Closeable

interface AbstractInstance : Closeable {

    fun launch()

}