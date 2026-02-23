import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.util.KeywordUtil
import cosmo.fota.FotaMessageHelper


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


//// ===================================================================
//// STEP 3: Tap Send Text Button (dynamic)
//// ===================================================================
//KeywordUtil.logInfo("=== STEP 3: Tapping Send Text Icon Dynamically ===")
//
//def xml3 = dumpAndParseXML()
//
//tapNodeCenter(xml3, "resource-id", "io.senlab.cosmo:id/sendText")
//
//KeywordUtil.logInfo("🎉 COMPLETE: Messages App → Contact → Send Text tapped dynamically!")
//
//
//// ===================================================================
//// STEP 4: Tap Predefined "Ok" Message
//// ===================================================================
//KeywordUtil.logInfo("=== STEP 4: Tapping predefined message 'Ok' ===")
//
//// Give UI a moment to fully render
//Thread.sleep(1500)
//
//// Dump XML for predefined text screen
//def xmlOkScreen = dumpAndParseXML()
//
//// Tap "Ok" using text (reusable method)
//tapNodeCenter(
//    xmlOkScreen,
//    "text",
//    "Ok"
//)
//
//KeywordUtil.logInfo("✅ Predefined message 'Ok' tapped successfully")
//
//Thread.sleep(3000)



// ===================================================================
// STEP 5: Verify Latest Text Message & Capture Details
// ===================================================================
KeywordUtil.logInfo("=== STEP 5: Verifying Latest Text Message ===")

Thread.sleep(3000)

// Dump chat XML
def xmlChat = dumpAndParseXML()

// Find all text messages
def textMessages = xmlChat.depthFirst().findAll {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_text"
}

if (!textMessages || textMessages.isEmpty()) {
	KeywordUtil.markFailed("❌ No text messages found in chat")
	return
}

// Latest message = last visible
def latestTextNode = textMessages.last()
String latestText = latestTextNode.@text.toString()

// Capture time
def timeNode = latestTextNode.parent().depthFirst().find {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
}
String sentTime = timeNode ? timeNode.@text.toString() : "Time not found"

// Capture date
def dateNodes = xmlChat.depthFirst().findAll {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/day"
}
String sentDate = dateNodes && !dateNodes.isEmpty()
		? dateNodes.last().@text.toString()
		: "Date not found"

// Capture contact
def contactNode = xmlChat.depthFirst().find {
	it.@'resource-id'.toString() == "io.senlab.cosmo:id/contactName"
}
String contactName = contactNode ? contactNode.@text.toString() : "Contact not found"

// Log result
KeywordUtil.logInfo("✅ Latest text message verified")
KeywordUtil.logInfo("👤 Contact Name : ${contactName}")
KeywordUtil.logInfo("💬 Message     : ${latestText}")
KeywordUtil.logInfo("🕒 Sent Time   : ${sentTime}")
KeywordUtil.logInfo("📅 Sent Date   : ${sentDate}")


// ===================================================================
// STEP 6: Scroll Chat & Collect All Text Messages (STRUCTURED)
// ===================================================================
KeywordUtil.logInfo("=== STEP 6: Scrolling chat and collecting all text messages ===")

List<Map<String, String>> messageList = []

int maxScrolls = 10
int previousCount = -1

for (int scroll = 0; scroll < maxScrolls; scroll++) {

    KeywordUtil.logInfo("🔄 Scroll iteration ${scroll + 1}")

    def xmlChatScroll = dumpAndParseXML()

    // Collect visible text messages
    def textMsgNodes = xmlChatScroll.depthFirst().findAll {
        it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_text"
    }

    // Capture visible date
    def visibleDayNodes = xmlChatScroll.depthFirst().findAll {
        it.@'resource-id'.toString() == "io.senlab.cosmo:id/day"
    }

    String visibleDate =
        visibleDayNodes && !visibleDayNodes.isEmpty()
            ? visibleDayNodes.last().@text.toString()
            : "Date not visible"

    textMsgNodes.each { msgNode ->

        String msgText = msgNode.@text.toString()

        def msgTimeNode = msgNode.parent().depthFirst().find {
            it.@'resource-id'.toString() == "io.senlab.cosmo:id/msg_time"
        }

        if (!msgTimeNode) return

        String msgTimeVal = msgTimeNode.@text.toString()

        Map<String, String> msgObject = [
            contact : contactName,
            text    : msgText,
            time    : msgTimeVal,
            date    : visibleDate
        ]

        boolean exists = messageList.any {
            it.text == msgText && it.time == msgTimeVal
        }

        if (!exists) {
            messageList.add(msgObject)
        }
    }

    // ⬆️ STOP condition (AFTER processing all messages)
    if (messageList.size() == previousCount) {
        KeywordUtil.logInfo("📌 No new text messages detected — reached top of chat.")
        break
    }

    previousCount = messageList.size()

    // Scroll up
    KeywordUtil.logInfo("⬆️ Scrolling up")
    "adb shell input swipe 120 80 120 200".execute().waitFor()
    Thread.sleep(350)
}



// =======================
// 📋 FINAL REPORT
// =======================
KeywordUtil.logInfo("=== 📋 UNIQUE TEXT MESSAGES FOUND (${messageList.size()}) ===")

messageList.eachWithIndex { msg, i ->
    KeywordUtil.logInfo(
        "(${i + 1}) 👤 ${msg.contact} | 💬 '${msg.text}' | 🕒 ${msg.time} | 📅 ${msg.date}"
    )
}

if (messageList.isEmpty()) {
    KeywordUtil.markFailed("❌ No text messages found after scrolling")
}


// =====================================================
// 🧸 POST-FOTA VERIFICATION
// =====================================================
KeywordUtil.logInfo("=== 🧪 POST-FOTA MESSAGE VERIFICATION STARTED ===")

def savedPreFotaMessages = FotaMessageHelper.loadPreFotaMessages()

savedPreFotaMessages.each { preMsg ->

	boolean found = messageList.any { postMsg ->
		postMsg.text == preMsg.text &&
		postMsg.time == preMsg.time
	}

	if (!found) {
		KeywordUtil.markFailed(
			"❌ Message missing after FOTA → " +
			"Text: '${preMsg.text}', Time: ${preMsg.time}"
		)
	}
}

KeywordUtil.logInfo("🎉 ALL PRE-FOTA MESSAGES FOUND AFTER FOTA")
