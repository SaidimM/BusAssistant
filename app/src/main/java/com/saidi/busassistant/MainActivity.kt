package com.saidi.busassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saidi.busassistant.ui.addline.AddLineScreen
import com.saidi.busassistant.ui.home.HomeScreen
import com.saidi.busassistant.ui.settings.SettingsScreen
import com.saidi.busassistant.ui.theme.BusAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity —— 应用入口
 * 使用 Jetpack Navigation + Compose 管理页面路由
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        // 首页 —— 实时公交看板
                        composable(
                            route = Screen.Home.route,
                            enterTransition = { fadeIn(animationSpec = tween(300)) },
                            exitTransition = { fadeOut(animationSpec = tween(300)) }
                        ) {
                            HomeScreen(
                                onAddLineClick = {
                                    navController.navigate(Screen.AddLine.route)
                                },
                                onSettingsClick = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }

                        // 添加线路
                        composable(
                            route = Screen.AddLine.route,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeIn(tween(300))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(300))
                            }
                        ) {
                            AddLineScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 设置
                        composable(
                            route = Screen.Settings.route,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeIn(tween(300))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(300))
                            }
                        ) {
                            SettingsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 页面路由定义
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddLine : Screen("add_line")
    data object Settings : Screen("settings")
}
