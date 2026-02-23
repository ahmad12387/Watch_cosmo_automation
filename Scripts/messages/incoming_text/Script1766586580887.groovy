import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.util.KeywordUtil
import smartWatch.MessageStore
import smartWatch.SmartWatchMessagePage


// ========== STEP 1: Wake up the smartwatch ==========
def wakeCommand = 'adb shell am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity'
def process1 = wakeCommand.execute()
process1.waitFor()
KeywordUtil.logInfo("✅ Screen awakened successfully.")

// Small delay to allow UI to load
Thread.sleep(2000)


// ========== STEP 2: Swipe left ==========
def swipeCommand = 'adb shell input swipe 200 120 40 120'
def process2 = swipeCommand.execute()
process2.waitFor()
KeywordUtil.logInfo("✅ Swipe left performed successfully.")

// Small delay to allow homescreen animation
Thread.sleep(2000)


// ===================================================================
// FUNCTION: Dump XML + Return Parsed Object
// ===================================================================
def dumpAndParseXML() {
	"adb shell uiautomator dump /sdcard/uidump.xml".execute().waitFor()
	"adb pull /sdcard/uidump.xml C:\\cosmo_xml\\uidump.xml".execute().waitFor()

	KeywordUtil.logInfo("📄 XML dumped & pulled successfully")
	File xmlFile = new File("C:\\cosmo_xml\\uidump.xml")
	return new XmlSlurper().parse(xmlFile)
}

// ===================================================================
// FUNCTION: Tap center of a node by resource-id or text match
// ===================================================================
def tapNodeCenter(xml, String attributeType, String attributeValue) {
    def node

    if (attributeType == "resource-id") {
        node = xml.depthFirst().find { it.@'resource-id'.toString() == attributeValue }
    } else if (attributeType == "text") {
        node = xml.depthFirst().find { it.@text.toString() == attributeValue }
    } else if (attributeType == "bounds") {
        node = xml.depthFirst().find { it.@bounds.toString() == attributeValue }
    }

    if (!node) {
        KeywordUtil.markFailed("❌ Node not found for ${attributeType}: ${attributeValue}")
        return
    }

    String bounds = node.@bounds.toString()
    def matcher = bounds =~ /\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
    matcher.find()

    int x1 = matcher.group(1) as int
    int y1 = matcher.group(2) as int
    int x2 = matcher.group(3) as int
    int y2 = matcher.group(4) as int

    int centerX = (x1 + x2) / 2
    int centerY = (y1 + y2) / 2

    "adb shell input tap ${centerX} ${centerY}".execute().waitFor()
    KeywordUtil.logInfo("✅ Successfully tapped bounds element at (${centerX}, ${centerY})")
}


// ===========================
// FUNCTION: Tap Messages Icon
// ===========================
def tapMessagesIcon() {

    // Dump XML from the watch
    "adb shell uiautomator dump /sdcard/ui.xml".execute().waitFor()
    "adb pull /sdcard/ui.xml C:\\cosmo_xml\\ui.xml".execute().waitFor()
    KeywordUtil.logInfo("📄 XML pulled successfully")

    File xmlFile = new File("C:\\cosmo_xml\\ui.xml")
    String xmlText = xmlFile.text

    // Regex to find the Messages icon by its bounds index=1
    def matcher = xmlText =~ /bounds="\[120,19\]\[236,129\]"/

    if(!matcher) {
        KeywordUtil.markFailed("❌ Messages icon NOT FOUND in UI XML")
        return
    }

    int x1 = 120
    int y1 = 19
    int x2 = 236
    int y2 = 129

    int centerX = (x1 + x2) / 2
    int centerY = (y1 + y2) / 2

    KeywordUtil.logInfo("📌 Tapping Messages Icon at center (${centerX}, ${centerY})")

    String tapCmd = "adb shell input tap ${centerX} ${centerY}"
    tapCmd.execute().waitFor()

    KeywordUtil.logInfo("✅ Messages app opened successfully")
}

// Call the function
tapMessagesIcon()


// ===================================================================
// STEP 2: Tap Contact → “Ahmad Alam” (dynamic)
// ===================================================================
KeywordUtil.logInfo("=== STEP 2: Tapping Contact 'Ahmad Alam' Dynamically ===")

def xml2 = dumpAndParseXML()

tapNodeCenter(xml2, "text", "Ahmad Alam")

Thread.sleep(1500)


int maxScrolls = 2
//int overlapCount = 0


// (Step 4): Store BEFORE FOTA
MessageStore.clearAll()

for (int i = 0; i < maxScrolls; i++) {

    def xml = dumpAndParseXML()
    def incoming = SmartWatchMessagePage.extractIncomingText(xml)

    MessageStore.incomingTextBeforeFota.addAll(incoming)

    "adb shell input swipe 120 80 120 200".execute().waitFor()
    Thread.sleep(400)
}

int rawBeforeFotaCount = MessageStore.incomingTextBeforeFota.size()
//int adjustedBeforeFotaCount = rawBeforeFotaCount - overlapCount

KeywordUtil.logInfo("📦 BEFORE FOTA raw incoming message count = ${rawBeforeFotaCount}")
KeywordUtil.logInfo("📦 BEFORE FOTA adjusted incoming message count = ${adjustedBeforeFotaCount}")

if (adjustedBeforeFotaCount < 0) {
    KeywordUtil.markFailed("❌ Adjusted BEFORE FOTA message count is invalid (less than 0)")
}

Thread.sleep(10000)
//// (Step 5): Store AFTER FOTA
//MessageStore.incomingTextAfterFota.clear()
//
//for (int i = 0; i < maxScrolls; i++) {
//
//    def xml = dumpAndParseXML()
//    def incoming = SmartWatchMessagePage.extractIncomingText(xml)
//
//    MessageStore.incomingTextAfterFota.addAll(incoming)
//
//    "adb shell input swipe 120 80 120 200".execute().waitFor()
//    Thread.sleep(400)
//}
//
//int rawAfterFotaCount = MessageStore.incomingTextAfterFota.size()
//int adjustedAfterFotaCount = rawAfterFotaCount - overlapCount
//
//KeywordUtil.logInfo("📦 AFTER FOTA raw incoming message count = ${rawAfterFotaCount}")
//KeywordUtil.logInfo("📦 AFTER FOTA adjusted incoming message count = ${adjustedAfterFotaCount}")
//
//if (adjustedAfterFotaCount < 0) {
//    KeywordUtil.markFailed("❌ Adjusted AFTER FOTA message count is invalid (less than 0)")
//}
//
//
//// Step 6 Count Comparison
//if (adjustedBeforeFotaCount != adjustedAfterFotaCount) {
//    KeywordUtil.markFailed(
//        "❌ Incoming message count mismatch after FOTA. " +
//        "Before FOTA = ${adjustedBeforeFotaCount}, " +
//        "After FOTA = ${adjustedAfterFotaCount}"
//    )
//}
//
//KeywordUtil.logInfo(
//    "✅ Incoming message count is consistent after FOTA. " +
//    "Count = ${adjustedAfterFotaCount}"
//)

