package ru.aleksandr.dccppthrottle.store

/** NMRA S-9.2.2. Curve points are ordered by speed, not by CV number. */
internal object SpeedCurveStore {
    const val CONFIGURATION_CV = 29
    private const val SPEED_TABLE_BIT = 1 shl 4
    val simplePoints = listOf(2, 6, 5)
    val extendedPoints = (67..94).toList()

    fun pointCvs(extended: Boolean) = if (extended) extendedPoints else simplePoints
    fun dataCvs(extended: Boolean) = pointCvs(extended) + if (extended) listOf(66, 95) else emptyList()
    fun readCvs(extended: Boolean) = listOf(CONFIGURATION_CV) + dataCvs(extended)
    fun simpleEnabled(cv29: Int) = cv29 and SPEED_TABLE_BIT == 0
    fun withSimpleEnabled(cv29: Int, enabled: Boolean): Int =
        if (enabled) cv29 and SPEED_TABLE_BIT.inv() else cv29 or SPEED_TABLE_BIT

    fun defaults(): Map<Int, Int> = mapOf(2 to 0, 6 to 128, 5 to 255, 66 to 0, 95 to 0) +
        extendedPoints.mapIndexed { index, cv -> cv to ((index + 1) * 255 / 28) }.toMap()
}
