import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.util.KeywordUtil



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


// ===================================================================
// STEP 3: Tap Send Text Button (dynamic)
// ===================================================================
KeywordUtil.logInfo("=== STEP 3: Tapping Send Text Icon Dynamically ===")

def xml3 = dumpAndParseXML()

tapNodeCenter(xml3, "resource-id", "io.senlab.cosmo:id/sendText")

KeywordUtil.logInfo("🎉 COMPLETE: Messages App → Contact → Send Text tapped dynamically!")


// ===================================================================
// STEP 4 — Tap Emoji Button ("...")
// ===================================================================
KeywordUtil.logInfo("=== STEP 4: Tapping Emoji Button ===")

def xmlEmoji = dumpAndParseXML()

tapNodeCenter(xmlEmoji, "text", "...")

KeywordUtil.logInfo("😀 Emoji button tapped successfully!")


// ===================================================================
// STEP 5 — Tap an Emoji (select emoji to send)
// ===================================================================
KeywordUtil.logInfo("=== STEP 5: Selecting an Emoji ===")

def xmlEmojiGrid = dumpAndParseXML()

// Tap any emoji by its text — here selecting 😂
tapNodeCenter(xmlEmojiGrid, "text", "😂")

KeywordUtil.logInfo("😀 Emoji selected successfully!")


// ===================================================================
// STEP 6 — Tap Send Button (Emoji Screen)
// ===================================================================
KeywordUtil.logInfo("=== STEP 6: Sending Emoji ===")

xmlSend = dumpAndParseXML()   // <-- FIXED (no def)

// Correct bounds for sendEmojiBtn from your XML
String sendBounds = "[122,191][236,236]"

// Find the node by bounds
def sendNode = xmlSend.depthFirst().find {
    it.@bounds.toString() == sendBounds
}

if (!sendNode) {
    KeywordUtil.markFailed("❌ Send button NOT found on emoji screen!")
} else {
    // Tap by bounds (using reusable function)
    tapNodeCenter(xmlSend, "bounds", sendBounds)
    KeywordUtil.logInfo("📤 Emoji sent successfully!")
}


// ===================================================================
// STEP 7 — TRUE FULL CHAT EMOJI COLLECTION
// ===================================================================
KeywordUtil.logInfo("=== STEP 7: Collecting ALL Emoji Messages Accurately ===")

// List to store all unique emoji messages
def allEmojiMessages = []

// Helper function to extract emojis + time from a given XML
def extractEmojiMessages = { xmlObj ->

    def messages = []

    xmlObj.depthFirst().findAll { node ->
        node.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_text"
    }.each { emojiNode ->

        def parent = emojiNode.parent()

        def timeNode = parent.depthFirst().find {
            it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
        }

        messages << [
            emoji: emojiNode.@text.toString(),
            time : timeNode?.@text?.toString()
        ]
    }

    return messages
}


// ===================================================================
// LOOP: Scroll multiple times & extract messages on each scroll
// ===================================================================

int maxScrolls = 10
int previousCount = -1

for (int scroll = 0; scroll < maxScrolls; scroll++) {

    KeywordUtil.logInfo("🔄 Scroll iteration ${scroll + 1}")

    // Dump XML for the current view
    def currentXML = dumpAndParseXML()

    // Extract all emoji messages from this XML
    def extracted = extractEmojiMessages(currentXML)

   extracted.each { msg ->

    // 🔥 Skip emojis with null/blank time
    if (!msg.time || msg.time.trim() == "") {
        KeywordUtil.logInfo("⏭ Skipped emoji '${msg.emoji}' because time is null")
        return
    }

    // Add only unique valid messages
    if (!allEmojiMessages.contains(msg)) {
        allEmojiMessages << msg
    }
}


    // Check if list stopped growing → we reached top of chat  
    if (allEmojiMessages.size() == previousCount) {
        KeywordUtil.logInfo("📌 No new emojis detected — reached top of chat.")
        break
    }

    previousCount = allEmojiMessages.size()

    // Scroll upward for next batch
    "adb shell input swipe 120 80 120 200".execute().waitFor()
    Thread.sleep(350)
}


// ===================================================================
// STEP B: Display Results
// ===================================================================

if (allEmojiMessages.isEmpty()) {
    KeywordUtil.markFailed("❌ No emoji messages found in chat!")
} else {

    KeywordUtil.logInfo("📌 TOTAL UNIQUE EMOJIS FOUND: ${allEmojiMessages.size()}")

    allEmojiMessages.eachWithIndex { msg, i ->
        KeywordUtil.logInfo("(${i+1}) 🟦 Emoji: ${msg.emoji} | Time: ${msg.time}")
    }

    // Latest message = last one in list
    def latest = allEmojiMessages.last()

    KeywordUtil.logInfo("✅ Latest Emoji Found: ${latest.emoji} at ${latest.time}")

    if (latest.emoji != "😂") {
        KeywordUtil.markFailed("❌ Latest emoji does NOT match expected 😂")
    } else {
        KeywordUtil.logInfo("🎉 Emoji verification PASSED!")
    }
}
