package info.nightscout.androidaps.plugins.source

import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import info.nightscout.androidaps.TestBase
import info.nightscout.androidaps.logging.AAPSLogger
import info.nightscout.androidaps.utils.resources.ResourceHelper
import info.nightscout.androidaps.utils.sharedPreferences.SP
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
class NSClientPluginTest : TestBase() {

    private lateinit var nsClientSourcePlugin: NSClientSourcePlugin

    @Mock lateinit var resourceHelper: ResourceHelper
    @Mock lateinit var sp: SP

    @Before
    fun setup() {
        nsClientSourcePlugin = NSClientSourcePlugin(HasAndroidInjector { AndroidInjector { } }, resourceHelper, aapsLogger, sp)
    }

    @Test fun advancedFilteringSupported() {
        Assert.assertEquals(false, nsClientSourcePlugin.advancedFilteringSupported())
    }
}