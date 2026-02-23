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
// STEP 3: Tap Voice Note Button (Send Audio)
// ===================================================================
KeywordUtil.logInfo("=== STEP 3: Tapping Voice Note Button ===")

def xmlAudio = dumpAndParseXML()

// Try by resource-id first
def audioBtn = xmlAudio.depthFirst().find {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/sendAudio"
}

if (audioBtn) {
	tapNodeCenter(xmlAudio, "resource-id", "io.senlab.cosmo:id/sendAudio")
	KeywordUtil.logInfo("🎤 Voice Note button tapped successfully using resource-id!")
} else {
	KeywordUtil.logInfo("⚠️ sendAudio NOT found, using bounds fallback…")
	tapNodeCenter(xmlAudio, "bounds", "[170,168][236,236]")
	KeywordUtil.logInfo("🎤 Voice Note button tapped using fallback bounds!")
}



// ===================================================================
// STEP 4: Tap New Voice Note Button
// ===================================================================
KeywordUtil.logInfo("=== STEP 4: Tapping 'New note' Button ===")

def xmlNewNote = dumpAndParseXML()

// First try locating using resource-id
def newNoteBtn = xmlNewNote.depthFirst().find {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/new_recording"
}

if (newNoteBtn) {
	tapNodeCenter(xmlNewNote, "resource-id", "io.senlab.cosmo:id/new_recording")
	KeywordUtil.logInfo("🎤 'New note' button tapped successfully using resource-id!")
} else {
	// Fallback using bounds from your XML dump
	KeywordUtil.logInfo("⚠️ new_recording not found — using fallback bounds...")
	tapNodeCenter(xmlNewNote, "bounds", "[3,22][237,82]")
	KeywordUtil.logInfo("🎤 'New note' button tapped using fallback bounds!")
}

Thread.sleep(5000)


// ===================================================================
// STEP 5: Tap the Record Button ("Tap to record")
// ===================================================================
KeywordUtil.logInfo("=== STEP 5: Tapping 'Tap to record' Button ===")

// Give UI a moment to fully render
Thread.sleep(1500)

// Dump fresh XML
def xmlRecord = dumpAndParseXML()

// Tap Record button using stable resource-id
tapNodeCenter(
	xmlRecord,
	"resource-id",
	"io.senlab.cosmo:id/btnSoundrecStart"
)

KeywordUtil.logInfo("🎤 Record button tapped successfully")

Thread.sleep(35000)// let the video record, i tried to get the xml in the middle but couldn't get because screen wasn't in idle state


// ===================================================================
// STEP 6: Tap Upload / Send Audio Button
// ===================================================================
KeywordUtil.logInfo("=== STEP 6: Tapping 'Upload / Send Audio' Button ===")

// Give UI a moment to fully stabilize after recording
Thread.sleep(1500)

// Dump fresh XML for post-record screen
def xmlSendAudio = dumpAndParseXML()

// Tap Send / Upload button using stable resource-id
tapNodeCenter(
	xmlSendAudio,
	"resource-id",
	"io.senlab.cosmo:id/btnSoundrecSend"
)

KeywordUtil.logInfo("📤 Audio uploaded / sent successfully")

Thread.sleep(5000)



// ===================================================================
// STEP 7: Verify Latest Audio Message & Capture Details
// ===================================================================
KeywordUtil.logInfo("=== STEP 7: Verifying Latest Audio Message ===")

// Allow chat UI to fully refresh
Thread.sleep(3000)

// Dump latest chat XML
def xmlChat = dumpAndParseXML()

// =======================
// 1️⃣ Verify Audio Exists
// =======================
def audioMessages = xmlChat.depthFirst().findAll {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_audio"
}

if (!audioMessages || audioMessages.isEmpty()) {
	KeywordUtil.markFailed("❌ No audio message found in chat")
	return
}

// Latest audio message = last one
def latestAudio = audioMessages.last()

// =======================
// 2️⃣ Capture Audio Duration
// =======================
def durationNode = latestAudio.depthFirst().find {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/audioTimeLeft"
}

if (!durationNode) {
	KeywordUtil.markFailed("❌ Audio duration not found for latest audio message")
	return
}

String audioDuration = durationNode.@text.toString()

// =======================
// 3️⃣ Capture Message Time
// =======================
def msgTimeNode = latestAudio.parent().depthFirst().find {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
}

String sentTime = msgTimeNode ? msgTimeNode.@text.toString() : "Time not found"

// =======================
// 4️⃣ Capture Message Date
// =======================
def dateNodes = xmlChat.depthFirst().findAll {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/day"
}

String sentDate = dateNodes && !dateNodes.isEmpty()
		? dateNodes.last().@text.toString()
		: "Date not found"

// =======================
// 5️⃣ Capture Contact Name
// =======================
def contactNode = xmlChat.depthFirst().find {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/contactName"
}

String contactName = contactNode ? contactNode.@text.toString() : "Contact not found"

// =======================
// ✅ SUCCESS LOGGING
// =======================
KeywordUtil.logInfo("✅ Audio message verified successfully")
KeywordUtil.logInfo("👤 Contact Name : ${contactName}")
KeywordUtil.logInfo("🎵 Audio Length : ${audioDuration}")
KeywordUtil.logInfo("🕒 Sent Time    : ${sentTime}")
KeywordUtil.logInfo("📅 Sent Date    : ${sentDate}")





// ===================================================================
// STEP 8: Scroll Chat & Collect All Audio Messages (DEDUPED)
// ===================================================================
KeywordUtil.logInfo("=== STEP 8: Scrolling chat and collecting all audio messages ===")

Map<String, String> audioLogMap = new LinkedHashMap<>()

int maxScrolls = 10
int previousCount = -1

for (int scroll = 0; scroll < maxScrolls; scroll++) {

    KeywordUtil.logInfo("🔄 Scroll iteration ${scroll + 1}")

    def xmlChatScroll = dumpAndParseXML()

    def audioMsgNodes = xmlChatScroll.depthFirst().findAll {
        it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_audio"
    }

    def dayNodesVisible = xmlChatScroll.depthFirst().findAll {
        it.@'resource-id'.toString() == "io.senlab.cosmo:id/day"
    }

    String visibleDate = dayNodesVisible && !dayNodesVisible.isEmpty()
            ? dayNodesVisible.last().@text.toString()
            : "Date not visible"

    audioMsgNodes.each { audioNode ->

        def audioDurationNode = audioNode.depthFirst().find {
            it.@'resource-id'.toString() == "io.senlab.cosmo:id/audioTimeLeft"
        }

        def audioTimeNode = audioNode.parent().depthFirst().find {
            it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
        }

        if (audioDurationNode && audioTimeNode) {

            String durationVal = audioDurationNode.@text.toString()
            String timeVal = audioTimeNode.@text.toString()

            // 🔑 Deduplication key (stable identity)
            String dedupKey = "${contactName}|${durationVal}|${timeVal}"

            // 📝 Full log entry (metadata)
            String logEntry =
                "👤 ${contactName} | 🎵 ${durationVal} | 🕒 ${timeVal} | 📅 ${visibleDate}"

            // Only update if key is new OR date was previously missing
            if (!audioLogMap.containsKey(dedupKey)
                || audioLogMap[dedupKey].contains("Date not visible")) {

                audioLogMap[dedupKey] = logEntry
            }
        }
    }

    // 🛑 Stop if no new unique audios
    if (audioLogMap.size() == previousCount) {
        KeywordUtil.logInfo("📌 No new audio messages detected — reached top of chat.")
        break
    }

    previousCount = audioLogMap.size()

    // Scroll (working gesture)
    "adb shell input swipe 120 80 120 200".execute().waitFor()
    Thread.sleep(350)
}

// =======================
// 📋 FINAL REPORT
// =======================
KeywordUtil.logInfo("=== 📋 UNIQUE AUDIO MESSAGES FOUND (${audioLogMap.size()}) ===")

audioLogMap.values().eachWithIndex { entry, i ->
    KeywordUtil.logInfo("(${i+1}) ${entry}")
}

if (audioLogMap.isEmpty()) {
    KeywordUtil.markFailed("❌ No audio messages found after scrolling")
}
