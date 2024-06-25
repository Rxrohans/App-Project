import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubmissionDateTimeHelper {
    companion object {
        fun getCurrentDateTime(): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }
}
