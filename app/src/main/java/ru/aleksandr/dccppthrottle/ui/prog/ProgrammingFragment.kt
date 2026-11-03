package ru.aleksandr.dccppthrottle.ui.prog

import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.store.MockStore
import ru.aleksandr.dccppthrottle.ui.decoder.DecoderFragment

/** Uses the decoder helpers' cancellable CV requests and disconnected debug mocks. */
open class ProgrammingFragment : DecoderFragment() {
    protected suspend fun readProgrammingCv(cv: Int): Int = readCv(cv) { MockStore.programmingCvValue(cv) }

    protected suspend fun writeProgrammingCv(cv: Int, value: Int) {
        writeCv(cv, value)
        if (BuildConfig.DEBUG && !CommandStation.isConnected()) MockStore.setProgrammingCvValue(cv, value)
    }
}
