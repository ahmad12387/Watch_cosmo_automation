package smartWatch

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.util.KeywordUtil

public class SmartWatchNavigation extends SmartWatchBase {

	@Keyword
	def goToAppGrid() {
		KeywordUtil.logInfo("Navigating to app grid...")
		swipe(200,120,40,120)
		Thread.sleep(1000)
	}

	@Keyword
	def openDialerApp() {
		KeywordUtil.logInfo("Opening Dialer app...")
		tap(60, 80)   // You can replace with dynamic locator later
		Thread.sleep(500)
	}


	//dumps xml after swipe to check if we are on screen 1 with contacts messages... etc
	@Keyword
	def dumpNavigationXML() {

		KeywordUtil.logInfo("📄 Dumping Navigation (Apps Screen) XML...")

		// Dump XML from device
		runAdb("uiautomator dump /sdcard/nav_dump.xml", true)

		// Pull XML locally
		runAdb("pull /sdcard/nav_dump.xml nav_dump.xml", false)

		Thread.sleep(800) // wait for file

		String xml = new File("nav_dump.xml").text
		return xml
	}

	@Keyword
	def verifyDialerAppPresent() {

		KeywordUtil.logInfo("🔍 Verifying Dialer App using ONLY bounds check...")

		int timeoutMs = 7000
		int intervalMs = 700
		long endTime = System.currentTimeMillis() + timeoutMs

		// The only thing we check:
		String expectedBounds = 'bounds="[4,19][120,126]"'

		while (System.currentTimeMillis() < endTime) {

			String xml = dumpNavigationXML()

			if (xml.contains(expectedBounds)) {
				KeywordUtil.logInfo("✅ Dialer App FOUND with bounds [4,19][120,126].")
				return
			}

			KeywordUtil.logInfo("⏳ Dialer bounds not found yet, retrying...")
			Thread.sleep(intervalMs)
		}

		// If failed after retries
		String finalXml = dumpNavigationXML()
		KeywordUtil.logInfo("❌ XML Preview:\n" +
				finalXml.substring(0, Math.min(finalXml.length(), 800)))

		KeywordUtil.markFailedAndStop(
				"❌ Dialer App NOT found — bounds [4,19][120,126] missing!"
				)
	}

	// ===================================================================
	// KEYWORD: Swipe back to previous screen (Cosmo standard BACK gesture)
	// ===================================================================
	@Keyword
	def swipeBackToPreviousScreen() {

		KeywordUtil.logInfo("⬅️ Swiping back to previous screen...")

		// Golden BACK gesture for Cosmo
		runAdb("input swipe 5 120 235 120 600", true)

		// Allow UI transition
		Thread.sleep(1500)
	}
}
