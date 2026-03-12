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
Thread.sleep(3000)
swipeCommand.execute()
Thread.sleep(2000)
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




// ===================================================================
// STEP 3: Open Video App
// ===================================================================
KeywordUtil.logInfo("=== STEP: Opening Video App ===")

def xmlVideo = dumpAndParseXML()

tapNodeCenter(xmlVideo, "bounds", "[4,19][120,126]")

Thread.sleep(1500)

// ===================================================================
// STEP 5: Open Photos / Gallery
// ===================================================================
KeywordUtil.logInfo("=== STEP: Opening Photos Gallery ===")

xmlGalleryAfter = dumpAndParseXML()

tapNodeCenter(xmlGalleryAfter, "resource-id", "io.senlab.cosmo:id/btnGallery")

Thread.sleep(1500)

// ===================================================================
// FUNCTION: Count videos in gallery
// ===================================================================
def countGalleryVideos(xml) {

	def videos = xml.depthFirst().findAll {
		it.@'resource-id'.toString() == "io.senlab.cosmo:id/image_type_icon"
	}

	return videos.size()
}

//=== STEP 6: Counting Videos Before Recording ===//
KeywordUtil.logInfo("=== STEP: Counting Videos Before Recording ===")

def xmlBeforeVideos = dumpAndParseXML()

int videosBefore = countGalleryVideos(xmlBeforeVideos)

KeywordUtil.logInfo("Videos before recording: " + videosBefore)

Thread.sleep(1500)

// ========== STEP : Go back to previous screen ==========
CustomKeywords.'smartWatch.SmartWatchNavigation.swipeBackToPreviousScreen'()
Thread.sleep(3000)

// ===================================================================
// STEP 7: Start Video Recording
// ===================================================================
KeywordUtil.logInfo("=== STEP: Start Video Recording ===")

def xmlStartVideo = dumpAndParseXML()

tapNodeCenter(xmlStartVideo, "resource-id", "io.senlab.cosmo:id/btnVideo")

KeywordUtil.logInfo("🎥 Recording video for 6 seconds")

Thread.sleep(6000)


// ===================================================================
// STEP 8: Stop Video Recording
// ===================================================================
KeywordUtil.logInfo("=== STEP: Stop Video Recording ===")

def xmlStopVideo = dumpAndParseXML()

tapNodeCenter(xmlStopVideo, "resource-id", "io.senlab.cosmo:id/btnVideo")

KeywordUtil.logInfo("✅ Video recording stopped")

Thread.sleep(3000)


// ===================================================================
// STEP 9: Open Photos / Gallery
// ===================================================================
KeywordUtil.logInfo("=== STEP: Opening Photos Gallery ===")

def xmlGalleryAfter = dumpAndParseXML()

tapNodeCenter(xmlGalleryAfter, "resource-id", "io.senlab.cosmo:id/btnGallery")

Thread.sleep(1500)

//=== STEP 10: Counting Videos After Recording ===//
KeywordUtil.logInfo("=== STEP: Counting Videos After Recording ===")

def xmlAfterVideos = dumpAndParseXML()

int videosAfter = countGalleryVideos(xmlAfterVideos)

KeywordUtil.logInfo("Videos after recording: " + videosAfter)

// Validating count
if(videosAfter == videosBefore + 1) {
	KeywordUtil.markPassed("✅ Video recorded successfully and saved in gallery")
}
else {
	KeywordUtil.markFailed("❌ Video not saved in gallery")
}
