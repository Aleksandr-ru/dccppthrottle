package ru.aleksandr.dccppthrottle.store

object MockStore {
    private val locomotives = listOf<String>(
        "BR-80 (Piko)",
        "BR120 (Piko)",
        "GP 35 (Bachmann)",
        "Breuer traktor (Rivarossi)",
        "Hercules (Piko new)",
        "V 200 (Piko new)",
        "Y-2126 (Ree)",
        "BR 78 (Piko new)"
    )

    private val accessories = listOf<String>(
        "Turnout",
        "Signal"
    )

    fun randomLocomotive() : String { return locomotives.random() }
    fun randomAccessory() : String { return accessories.random() + " " + randomAddress() }
    fun randomAddress() : Int { return (1..255).random() }
    fun randomSpeed() : Int { return (0..100).random() }
}