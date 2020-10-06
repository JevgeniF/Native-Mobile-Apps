package ee.taltech.connect4

import android.icu.number.IntegerWidth
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    var nextMoveByPOne = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gameButtonOnClick(view: View) {
        if ((view as Button).text == "") {
            val tag = (Integer.parseInt((view.tag).toString()))
            println(view.id);
        }
    }
}
