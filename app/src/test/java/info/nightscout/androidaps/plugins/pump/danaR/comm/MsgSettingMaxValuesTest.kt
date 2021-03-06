package info.nightscout.androidaps.plugins.pump.danaR.comm

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
class MsgSettingMaxValuesTest : DanaRTestBase() {

    @Test fun runTest() {
        val packet = MsgSettingMaxValues(aapsLogger, danaRPump)

        // test message decoding
        packet.handleMessage(createArray(34, 7.toByte()))
        Assert.assertEquals(MessageBase.intFromBuff(createArray(10, 7.toByte()), 0, 2).toDouble() / 100.0, danaRPump.maxBolus, 0.0)
    }
}