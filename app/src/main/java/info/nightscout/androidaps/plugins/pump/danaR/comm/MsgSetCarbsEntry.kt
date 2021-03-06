package info.nightscout.androidaps.plugins.pump.danaR.comm

import info.nightscout.androidaps.logging.AAPSLogger
import info.nightscout.androidaps.logging.LTag
import java.util.*

class MsgSetCarbsEntry(
    private val aapsLogger: AAPSLogger,
    val time: Long,
    val amount: Int
) : MessageBase() {

    init {
        SetCommand(0x0402)
        aapsLogger.debug(LTag.PUMPBTCOMM, "New message")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        AddParamByte(RecordTypes.RECORD_TYPE_CARBO)
        AddParamByte((calendar[Calendar.YEAR] % 100).toByte())
        AddParamByte((calendar[Calendar.MONTH] + 1).toByte())
        AddParamByte(calendar[Calendar.DAY_OF_MONTH].toByte())
        AddParamByte(calendar[Calendar.HOUR_OF_DAY].toByte())
        AddParamByte(calendar[Calendar.MINUTE].toByte())
        AddParamByte(calendar[Calendar.SECOND].toByte())
        AddParamByte(0x43.toByte()) //??
        AddParamInt(amount)
        aapsLogger.debug(LTag.PUMPBTCOMM, "Set carb entry: " + amount + " date " + calendar.time.toString())
    }

    override fun handleMessage(bytes: ByteArray) {
        val result = intFromBuff(bytes, 0, 1)
        if (result != 1) {
            failed = true
            aapsLogger.debug(LTag.PUMPBTCOMM, "Set carb entry result: $result FAILED!!!")
        } else {
            failed = false
            aapsLogger.debug(LTag.PUMPBTCOMM, "Set carb entry result: $result")
        }
    }
}