package com.example.shopapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shopapp.localization.LanguageManager
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.config.CloudinaryConfig
import com.example.shopapp.data.model.Product
import com.example.shopapp.navigation.AdminNavigation
import com.example.shopapp.navigation.AppNavigation
import com.example.shopapp.navigation.UserNavigation
import com.example.shopapp.ui.theme.ShopAppTheme
import com.example.shopapp.viewmodel.AuthViewModel
import com.example.shopapp.viewmodel.AuthViewModelFactory
import com.example.shopapp.zalopay.ZaloPayManager
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
//import vn.zalopay.sdk.ZaloPaySDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize LanguageManager
        LanguageManager.initialize(this)

        // CloudinaryConfig.init(this)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        callbackManager = CallbackManager.Factory.create()

        setContent {
            ShopAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModelFactory(auth, db, callbackManager)
                    )
//
//                    AdminNavigation(
//                        authViewModel = authViewModel,
//                        rootNavController = rememberNavController()
//                    )

                    AppNavigation(authViewModel, intent =intent)
//                    UserNavigation(
//                        authViewModel = authViewModel,
//                        rootNavController = rememberNavController(),
//                        intent = intent
//                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult called with requestCode=$requestCode, resultCode=$resultCode, data=$data")
        // Pass result to Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Pass result to ZaloPay SDK
//        data?.let {
//            if (!ZaloPayManager.isInitialized()) {
//                try {
//                    ZaloPayManager.init(this)
//                } catch (e: Exception) {
//                    Log.e("MainActivity", "Failed to initialize ZaloPay SDK in onActivityResult", e)
//                    return
//                }
//            }
//            ZaloPaySDK.getInstance().onResult(it)
//        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with intent: $intent")
        intent.let {
            if (!ZaloPayManager.isInitialized()) {
                try {
                    ZaloPayManager.init(this)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to initialize ZaloPay SDK in onNewIntent", e)
                    return
                }
            }
            //ZaloPaySDK.getInstance().onResult(it)
        }
    }


//    fun testFetchProducts() {
//        val db = FirebaseFirestore.getInstance()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val snapshot = db.collection("users").get().await()
//                snapshot.documents.forEach { doc ->
//                    Log.d("main", "User doc: ${doc.id} => ${doc.data}")
//                }
//            } catch (e: Exception) {
//                Log.e("Firestore", "Error fetching products: ${e.message}")
//            }
//        }
//    }
}


@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
}