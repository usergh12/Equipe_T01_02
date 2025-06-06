import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView

class ButtonClickHandler(
    private val context: Context,
    private val destination: Class<*>){

    fun mudarTela(b: Button){
        b.setOnClickListener(){
            context.startActivity(Intent(context,destination))
        }
    }

    fun erroLogin(b:Button, t: TextView){
        b.setOnClickListener(){
            t.visibility = View.VISIBLE
        }
    }

}
