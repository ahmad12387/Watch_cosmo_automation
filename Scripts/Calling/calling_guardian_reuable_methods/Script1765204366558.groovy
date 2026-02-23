import smartWatch.*
import com.kms.katalon.core.util.KeywordUtil

SmartWatchBase base = new SmartWatchBase()
SmartWatchNavigation nav = new SmartWatchNavigation()
SmartWatchCallingPage call = new SmartWatchCallingPage()

base.wakeScreen()
nav.goToAppGrid()
nav.verifyDialerAppPresent() // <--- VALIDATION
nav.openDialerApp()

call.waitForContactsToLoad()
call.callContactByName("Ahmad Alam")
call.validateCallConnected() //---VALIDATION
call.endCall()
call.waitForCallToEnd()   // <--- NEW VALIDATION

KeywordUtil.logInfo("🎯 Reusable flow executed successfully.")