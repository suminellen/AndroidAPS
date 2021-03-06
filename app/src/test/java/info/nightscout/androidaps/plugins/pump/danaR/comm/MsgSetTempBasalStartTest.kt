package info.nightscout.androidaps.plugins.pump.danaR.comm

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
class MsgSetTempBasalStartTest : DanaRTestBase() {

    @Test fun runTest() {
        val packet = MsgSetTempBasalStart(aapsLogger, 250, 1)

        // test message decoding
        packet.handleMessage(createArray(34, 7.toByte()))
        Assert.assertEquals(true, packet.failed)
    }
}