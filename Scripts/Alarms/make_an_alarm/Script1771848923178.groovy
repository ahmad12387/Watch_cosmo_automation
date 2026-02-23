import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonOutput
import groovy.xml.XmlSlurper
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import cosmo.fota.FotaAlarmHelper
import groovy.xml.XmlSlurper

// ===================================================================
// HELPER — Dump UI XML from watch and parse it (STABLE VERSION)
// ===================================================================
def dumpAndParseXML() {

    // Ensure folder exists
    File dir = new File("C:\\cosmo_xml")
    if (!dir.exists()) {
        dir.mkdirs()
    }

    // Dump UI hierarchy on device
    "adb shell uiautomator dump /sdcard/ui.xml".execute().waitFor()

    // Pull XML to local fixed path
    "adb pull /sdcard/ui.xml C:\\cosmo_xml\\ui.xml".execute().waitFor()

    KeywordUtil.logInfo("📄 UI XML dumped & pulled successfully")

    File xmlFile = new File("C:\\cosmo_xml\\ui.xml")
    return new XmlSlurper().parse(xmlFile)
}

// ========== STEP 1: Wake up the smartwatch ==========
def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
def process1 = wakeCommand.execute()
process1.waitFor()
KeywordUtil.logInfo("✅ Screen awakened successfully.")

// Small delay to allow UI to load
Thread.sleep(2000)

// ========== STEP 2: Swipe left Till it reaches Alarm App==========
def swipeCommand = 'adb shell input swipe 200 120 40 120'
def process2 = swipeCommand.execute()
Thread.sleep(2000)
swipeCommand.execute()
Thread.sleep(2000)
swipeCommand.execute()
Thread.sleep(2000)
process2.waitFor()
KeywordUtil.logInfo("✅ Swipe left performed successfully.")

// ========== STEP 3: Tap Alarm App (Top-Left) ==========
def tapAlarmCommand = 'adb shell input tap 62 72'
tapAlarmCommand.execute()
Thread.sleep(2000)

KeywordUtil.logInfo("✅ Alarm app opened successfully.")

// ========== STEP 4: Tap New Alarm (ADB – correct for current flow) ==========
def tapNewAlarmCommand = 'adb shell input tap 120 52'
tapNewAlarmCommand.execute()
Thread.sleep(2000)

KeywordUtil.logInfo("✅ New Alarm button tapped successfully.")

Thread.sleep(4000)

// ===================================================================
// STEP X — CAPTURE REAL-TIME WATCH CLOCK (ADB + XML)
// ===================================================================

KeywordUtil.logInfo("=== Capturing real-time watch clock from XML ===")

def xml = dumpAndParseXML()

def watchTimeNode = xml.depthFirst().find {
    it.@'resource-id' == 'io.senlab.cosmo:id/top_bar_clock'
}

if (!watchTimeNode) {
    KeywordUtil.markFailed("❌ Could not find top_bar_clock in XML")
}

String currentWatchTime = watchTimeNode.@text.toString()
KeywordUtil.logInfo("🕒 Watch time captured: ${currentWatchTime}")

// ✅ LOCAL variable (NO GlobalVariable)
def savedAlarmTime = currentWatchTime



// ===================================================================
// STEP 5 — SCROLL DOWN TO REVEAL SET ALARM BUTTON
// ===================================================================

KeywordUtil.logInfo("=== STEP 5: Scrolling down to reveal Set Alarm button ===")

// Swipe 1 — initial safe swipe (top area)
"adb shell input swipe 120 140 120 90 300".execute().waitFor()
Thread.sleep(2000)

// Swipe 2 — bottom swipe to move past time selector
"adb shell input swipe 120 215 120 130 400".execute().waitFor()
Thread.sleep(2000)

// Swipe 3 — repeat bottom swipe to fully reveal button
"adb shell input swipe 120 215 120 130 400".execute().waitFor()
Thread.sleep(2000)

KeywordUtil.logInfo("⬇️ Completed controlled scrolls — Set Alarm button should now be visible.")



// ===================================================================
// STEP 6 — TAP SAVE BUTTON (ADB)
// ===================================================================

KeywordUtil.logInfo("=== STEP 6: Clicking Save Alarm button (ADB) ===")

"adb shell input tap 120 206".execute().waitFor()
Thread.sleep(2000)

KeywordUtil.logInfo("✅ Save Alarm button tapped successfully via ADB.")


// ===================================================================
// VERIFY ALARM CREATED WITH REAL-TIME CLOCK (XML)
// ===================================================================

def xmlAfterSave = dumpAndParseXML()

def alarmFound = xmlAfterSave.depthFirst().any {
    it.@'resource-id' == 'io.senlab.cosmo:id/time' &&
    it.@text?.toString()?.equalsIgnoreCase(savedAlarmTime)
}

if (alarmFound) {
    KeywordUtil.logInfo("✅ Alarm created successfully at ${savedAlarmTime}")
} else {
    KeywordUtil.markFailed("❌ Alarm with time ${savedAlarmTime} not found")
}
