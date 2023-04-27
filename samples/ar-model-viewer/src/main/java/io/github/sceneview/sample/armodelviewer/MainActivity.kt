package io.github.sceneview.sample.armodelviewer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var placeModelButton: ExtendedFloatingActionButton
    lateinit var newModelButton: ExtendedFloatingActionButton

    data class Model(
        val fileLocation: String,
        val scaleUnits: Float? = null,
        val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE,
        val applyPoseRotation: Boolean = true
    )

    val models = listOf(
        Model("models/spiderbot.glb",
//            scaleUnits = 0.5f,
        ),
        Model(
            fileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
            scaleUnits = 1.0f,
            placementMode = PlacementMode.BEST_AVAILABLE,
            applyPoseRotation = true
        ),
        Model("models/chair.glb",
//            scaleUnits = 0.5f,
            placementMode = PlacementMode.BEST_AVAILABLE,
        ),
        Model("models/drawer.glb",
            placementMode = PlacementMode.BEST_AVAILABLE,
//            scaleUnits = 0.5f,
        ),
        Model("models/bed.glb",
//            scaleUnits = 0.5f,
            placementMode = PlacementMode.INSTANT,
        ),
        Model(
            fileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/GiantPanda/model.glb",
            placementMode = PlacementMode.PLANE_HORIZONTAL,
            scaleUnits = 0.5f
        ),

//        Model(
//            fileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
//            scaleUnits = 1.0f,
//            placementMode = PlacementMode.BEST_AVAILABLE,
//            applyPoseRotation = false
//        ),
//        Model(
//            fileLocation = "https://sceneview.github.io/assets/models/DamagedHelmet.glb",
//            placementMode = PlacementMode.INSTANT,
//            scaleUnits = 0.3f
//        ),
//        Model(
//            fileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/GiantPanda/model.glb",
//            placementMode = PlacementMode.PLANE_HORIZONTAL,
//            scaleUnits = 1.5f
//        ),
//        Model(
//            fileLocation = "https://sceneview.github.io/assets/models/Spoons.glb",
//            placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL,
//            scaleUnits = null
//        ),
//        Model(
//            fileLocation = "https://sceneview.github.io/assets/models/Halloween.glb",
//            placementMode = PlacementMode.PLANE_HORIZONTAL,
//            scaleUnits = 2.5f
//        ),
    )
    var modelIndex = 0
    var modelNode: ArModelNode? = null


    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sceneView = findViewById(R.id.sceneView)
        loadingView = findViewById(R.id.loadingView)
        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )


        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar)?.apply {
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
            }
            title = ""
        })

        newModelButton = findViewById<ExtendedFloatingActionButton>(R.id.newModelButton).apply {
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener { newModelNode() }
        }
        placeModelButton = findViewById<ExtendedFloatingActionButton>(R.id.placeModelButton).apply {
            setOnClickListener { placeModelNode() }
        }



        newModelNode()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = !item.isChecked
        modelNode?.detachAnchor()
        modelNode?.placementMode = when (item.itemId) {
            R.id.menuPlanePlacement -> PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL
            R.id.menuInstantPlacement -> PlacementMode.INSTANT
            R.id.menuDepthPlacement -> PlacementMode.DEPTH
            R.id.menuBestPlacement -> PlacementMode.BEST_AVAILABLE
            else -> PlacementMode.DISABLED
        }
        return super.onOptionsItemSelected(item)
    }

    fun placeModelNode() {
        modelNode?.anchor()
        placeModelButton.isVisible = false
        sceneView.planeRenderer.isVisible = false
    }

    fun newModelNode() {
        isLoading = true
        modelNode?.takeIf { !it.isAnchored }?.let {
            sceneView.removeChild(it)
            it.destroy()
        }
        val model = models[modelIndex]
        modelIndex = (modelIndex + 1) % models.size
        modelNode = ArModelNode(model.placementMode).apply {
            applyPoseRotation = model.applyPoseRotation
            loadModelAsync(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = model.fileLocation,
                autoAnimate = true,
                scaleToUnits = model.scaleUnits,
                centerOrigin = Position(y = -1.0f)
            ) {
                sceneView.planeRenderer.isVisible = true
                isLoading = false
            }
            onAnchorChanged = { node, _ ->
                placeModelButton.isGone = node.isAnchored
            }
            onHitResult = { node, _ ->
                placeModelButton.isGone = !node.isTracking
            }
        }
        sceneView.addChild(modelNode!!)
        sceneView.selectedNode = modelNode
    }


}