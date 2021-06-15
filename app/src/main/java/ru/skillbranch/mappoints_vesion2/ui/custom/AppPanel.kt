package ru.skillbranch.mappoints_vesion2.ui.custom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.app_panel_layout.view.*
import ru.skillbranch.mappoints_vesion2.R
import ru.skillbranch.mappoints_vesion2.ui.custom.behavior.AppPanelBahavior

class AppPanel @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    private var from : LatLng? = null
    private var to : LatLng? = null
    private var yourLocation : Double? = null

    init {
        View.inflate(context, R.layout.app_panel_layout, this)

        google_maps_btn.setOnClickListener {
            val saddr = "${from?.latitude},${from?.longitude}"
            val daddr = "${to?.latitude},${to?.longitude}"
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=${saddr}&daddr=${daddr}"))
            context.startActivity(intent)
        }

        waze_btn.setOnClickListener {
            val uri = "waze://?ll=${yourLocation}, ${to?.latitude}&navigate=yes";
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        }

    }

    fun initCoordinates(from : LatLng, to : LatLng, yourLocation : Double) {
        this.from = from
        this.to = to
        this.yourLocation = yourLocation
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return AppPanelBahavior()
    }

}