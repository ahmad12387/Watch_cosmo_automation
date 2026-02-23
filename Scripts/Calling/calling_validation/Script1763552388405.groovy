import com.kms.katalon.core.util.KeywordUtil
import java.nio.file.*

//// ========== STEP 1: Wake up the smartwatch ==========
//def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
//wakeCommand.execute().waitFor()
//KeywordUtil.logInfo("✅ Screen awakened successfully.")
//Thread.sleep(2000)
//
//// ========== STEP 2: Swipe left to reach apps ==========
//def swipeCommand = 'adb shell input swipe 200 120 40 120'
//swipeCommand.execute().waitFor()
//KeywordUtil.logInfo("✅ Swipe left performed successfully.")
//Thread.sleep(2000)
//
//// ========== STEP 3: Open Dialer App ==========
//def openDialer = 'adb shell input tap 60 80'
//openDialer.execute().waitFor()
//KeywordUtil.logInfo("✅ Dialer app opened successfully.")
//Thread.sleep(2000)
//
//// ========== STEP 4: Tap on “Ahmad Alam” (top contact) ==========
//def tapContact = 'adb shell input tap 120 230'
//tapContact.execute().waitFor()
//KeywordUtil.logInfo("📞 Call initiated to Ahmad Alam.")
//Thread.sleep(3000) // let the call start ringing

// ========== STEP 5: Poll for Call Status ==========
KeywordUtil.logInfo("🔍 Monitoring call state — checking if receiver answers...")

boolean isConnected = false
boolean isRinging = false
int attempts = 0

while (attempts < 2) {  // ~30 seconds total (2 × 5s)
    // Dump current UI hierarchy
    'adb shell uiautomator dump /sdcard/window_dump.xml'.execute().waitFor()
    Thread.sleep(1000)

    // Pull XML to local system
    def xmlPath = Paths.get("D:\\window_dump.xml")
    def pullCmd = 'adb pull /sdcard/window_dump.xml D:\\window_dump.xml'
    pullCmd.execute().waitFor()
    Thread.sleep(1000)

    if (!Files.exists(xmlPath)) {
        xmlPath = Paths.get("C:\\Users\\ahmad\\Katalon Studio\\window_dump.xml")
        def pullAlt = 'adb pull /sdcard/window_dump.xml "C:\\Users\\ahmad\\Katalon Studio\\window_dump.xml"'
        pullAlt.execute().waitFor()
    }

    if (!Files.exists(xmlPath)) {
        KeywordUtil.markFailedAndStop("❌ XML dump not found — cannot validate call state.")
    }

    // Read XML content
    def xmlContent = Files.readString(xmlPath)

    // Check for ringing vs connected
    if (xmlContent.contains('io.senlab.cosmo:id/title') && xmlContent.contains('Calling...')) {
        isRinging = true
        KeywordUtil.logInfo("⏳ Still ringing... (Attempt ${attempts + 1}/6)")
    } 
    else if (xmlContent.contains('io.senlab.cosmo:id/title') &&                // timer present
             xmlContent.contains('io.senlab.cosmo:id/contactNameTextView') &&  // contact name
             xmlContent.contains('io.senlab.cosmo:id/volumeButton') &&         // speaker icon
             xmlContent.contains('io.senlab.cosmo:id/centerImageView')) {      // hang-up button
        isConnected = true
        KeywordUtil.logInfo("✅ Receiver picked up — call connected successfully.")
        break
    } 
    else {
        KeywordUtil.logInfo("⚠️ Unknown state detected — retrying...")
    }

    Thread.sleep(5000)
    attempts++
}

// ========== STEP 6: Validation Decision ==========
if (isConnected) {
    KeywordUtil.logInfo("🎯 Call validation passed — receiver answered.")
} else if (isRinging) {
    KeywordUtil.markFailed("❌ Call kept ringing — receiver did not answer.")
} else {
    KeywordUtil.markFailed("❌ Call failed — unexpected behavior or disconnected.")
}

//// ========== STEP 7: End the Call ==========
//def endCall = 'adb shell input tap 120 200'
//endCall.execute().waitFor()
//KeywordUtil.logInfo("📴 Call ended successfully.")
//
//// ========== STEP 8: Flow Completion ==========
//KeywordUtil.logInfo("🏁 Flow completed: Wake → Swipe Left → Open Dialer → Call → Validate → End Call.")
