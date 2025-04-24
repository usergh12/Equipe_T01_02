import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView

class ButtonClickHandler(private val button: Button) {

    fun mudarTela(context: Context, destination: Class<*>) {
        button.setOnClickListener {
            val intent = Intent(context, destination)
            context.startActivity(intent)
        }
    }

    fun erroLogin(textView: TextView) {
        button.setOnClickListener {
            textView.visibility = View.VISIBLE
        }
    }
}
