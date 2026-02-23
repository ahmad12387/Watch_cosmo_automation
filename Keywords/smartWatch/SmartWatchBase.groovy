package smartWatch

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.util.KeywordUtil

public class SmartWatchBase {

	@Keyword
	def runAdb(String command, boolean isShell = true) {
		String adbCommand = isShell ? "adb shell ${command}" : "adb ${command}"
		KeywordUtil.logInfo("ADB CMD: " + adbCommand)
		adbCommand.execute().waitFor()
		Thread.sleep(600)
	}


	@Keyword
	def wakeScreen() {
		runAdb("am start -n io.senlab.cosmo/io.senlab.cosmo.MainActivity")
	}

	@Keyword
	def tap(int x, int y) {
		runAdb("input tap $x $y")
	}

	@Keyword
	def swipe(int x1, int y1, int x2, int y2) {
		runAdb("input swipe $x1 $y1 $x2 $y2")
	}
}
