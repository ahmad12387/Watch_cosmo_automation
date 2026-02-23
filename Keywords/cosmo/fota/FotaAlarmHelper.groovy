package cosmo.fota

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper
import com.kms.katalon.core.util.KeywordUtil

public class FotaAlarmHelper {

	static String BASE_FOLDER = System.getProperty("user.dir") + "\\cosmo_fota_data"
	static String FILE_PATH = BASE_FOLDER + "\\alarms_pre_fota.json"

	// =============================
	// Save alarms BEFORE FOTA
	// =============================
	static void savePreFotaAlarms(List alarms) {

		File folder = new File(BASE_FOLDER)
		if (!folder.exists()) {
			folder.mkdirs()
		}

		File file = new File(FILE_PATH)
		file.text = JsonOutput.prettyPrint(JsonOutput.toJson(alarms))

		KeywordUtil.logInfo("💾 Saved ${alarms.size()} alarms BEFORE FOTA")
	}

	// =============================
	// Load alarms BEFORE FOTA
	// =============================
	static List loadPreFotaAlarms() {

		File file = new File(FILE_PATH)

		if (!file.exists()) {
			KeywordUtil.markFailed("❌ Pre-FOTA alarm file not found")
			return []
		}

		return new JsonSlurper().parse(file)
	}

	// =============================
	// Compare alarms
	// =============================
	static void compareAlarms(List before, List after) {

		if (before.size() != after.size()) {
			KeywordUtil.markFailed("❌ Alarm count mismatch! Before=${before.size()} After=${after.size()}")
		}

		before.each { alarm ->
			if (!after.contains(alarm)) {
				KeywordUtil.markFailed("❌ Missing alarm after FOTA: ${alarm}")
			}
		}

		KeywordUtil.logInfo("✅ All alarms validated successfully after FOTA")
	}
}