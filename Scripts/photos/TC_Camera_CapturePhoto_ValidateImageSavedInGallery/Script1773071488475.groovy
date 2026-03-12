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
// FUNCTION: Tap Camera Icon
// ===========================
def tapCameraIcon() {

    "adb shell uiautomator dump /sdcard/ui.xml".execute().waitFor()
    "adb pull /sdcard/ui.xml C:\\cosmo_xml\\ui.xml".execute().waitFor()

    KeywordUtil.logInfo("📄 XML pulled successfully")

    File xmlFile = new File("C:\\cosmo_xml\\ui.xml")
    String xmlText = xmlFile.text

    def matcher = xmlText =~ /bounds="\[4,129\]\[120,236\]"/

    if(!matcher) {
        KeywordUtil.markFailed("❌ Camera icon NOT FOUND")
        return
    }

    int centerX = 62
    int centerY = 182

    KeywordUtil.logInfo("📌 Tapping Camera Icon at (${centerX}, ${centerY})")

    "adb shell input tap ${centerX} ${centerY}".execute().waitFor()

    KeywordUtil.logInfo("✅ Camera app opened successfully")
}

// Call function
tapCameraIcon()


// ===================================================================
// STEP 2: Open Photos / Gallery
// ===================================================================
KeywordUtil.logInfo("=== STEP: Opening Photos Gallery ===")

def xmlGalleryBefore = dumpAndParseXML()

tapNodeCenter(xmlGalleryBefore, "resource-id", "io.senlab.cosmo:id/btnGallery")

Thread.sleep(1500)

// ===================================================================
// FUNCTION: Count images in gallery
// ===================================================================
def countGalleryImages(xml) {

	def images = xml.depthFirst().findAll {
		it.@'resource-id'.toString() == "io.senlab.cosmo:id/image_small"
	}

	return images.size()
}


//=== STEP: Counting Images Before Taking Photo ===//
KeywordUtil.logInfo("=== STEP: Counting Images Before Taking Photo ===")

def xmlBefore = dumpAndParseXML()

int imagesBefore = countGalleryImages(xmlBefore)

KeywordUtil.logInfo("Images before capture: " + imagesBefore)

Thread.sleep(1500)

// ========== STEP : Go back to previous screen ==========
CustomKeywords.'smartWatch.SmartWatchNavigation.swipeBackToPreviousScreen'()
Thread.sleep(3000)



// ===================================================================
// STEP 3: Tap Camera Shutter Button
// ===================================================================
KeywordUtil.logInfo("=== STEP: Tapping Camera Shutter Button ===")

def xml3 = dumpAndParseXML()

tapNodeCenter(xml3, "resource-id", "io.senlab.cosmo:id/btnCam")

Thread.sleep(1500)

// ===================================================================
// STEP 4: Open Photos / Gallery
// ===================================================================
KeywordUtil.logInfo("=== STEP: Opening Photos Gallery ===")

def xmlGalleryAfter = dumpAndParseXML()

tapNodeCenter(xmlGalleryAfter, "resource-id", "io.senlab.cosmo:id/btnGallery")

Thread.sleep(1500)

//Step 5 — Count Images Again
Thread.sleep(2000)

def xmlAfter = dumpAndParseXML()

int imagesAfter = countGalleryImages(xmlAfter)

KeywordUtil.logInfo("Images after capture: " + imagesAfter)

//Step 6 — Validate Photo Added

if(imagesAfter == imagesBefore + 1) {
	KeywordUtil.markPassed("✅ New photo successfully added to gallery")
}
else {
	KeywordUtil.markFailed("❌ Photo not added to gallery")
}