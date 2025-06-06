import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView

class TextViewClickHandler(
    private val context: Context,
    private val destination: Class<*>
) {
    fun irParaTela(textView: TextView) {
        textView.setOnClickListener{
            context.startActivity(Intent(context, destination))
        }
    }
}
