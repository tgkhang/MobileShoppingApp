package com.example.shopapp.navigation

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Auth : Screen("auth")
    object AdminHome : Screen("adminHome")
    object UserHome : Screen("userHome")
    object ProductManagement : Screen("products_management")
    object UserManagement : Screen("users_management")
    object OrderManagement : Screen("orders_management")
    object EventManagement : Screen("events_management")
    object ProductDetail : Screen("productDetail/{productId}") {
        fun createRoute(productId: String) = "productDetail/$productId"
    }
    object ImageDetail: Screen("imageDetail/{productId}/{initialImageIndex}"){
        fun createRoute(productId: String, initialImageIndex: Int) = "imageDetail/$productId/$initialImageIndex"
    }
    object AllReviews : Screen("allReviews/{productId}") {
        fun createRoute(productId: String) = "allReviews/$productId"
    }
    object Search : Screen("search")
    object Carts : Screen("carts")
    object Orders : Screen("orders")
    object Checkout : Screen("checkout")


    object Profile : Screen("profile")
    object EditProfile: Screen("editProfile")
    object Signup: Screen("signup")
    object ResetPassword: Screen("resetPassword")
}