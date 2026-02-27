package org.openpdf.reader

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import org.openpdf.feature.filemanager.RecentFilesScreen
import org.openpdf.feature.settings.section.AboutScreen
import org.openpdf.feature.settings.section.AppearanceSettingsScreen
import org.openpdf.feature.settings.SettingsScreen
import org.openpdf.feature.viewer.ViewerScreen

object Routes {
    const val RECENT_FILES = "recent_files"
    const val VIEWER = "viewer?uri={documentUri}"
    const val VIEWER_ARG = "documentUri"
    const val SETTINGS = "settings"
    const val SETTINGS_APPEARANCE = "settings/appearance"
    const val SETTINGS_ABOUT = "settings/about"
}

@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    initialUri: Uri? = null,
) {
    val startDestination = if (initialUri != null) {
        "viewer?uri=${Uri.encode(initialUri.toString())}"
    } else {
        Routes.RECENT_FILES
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.RECENT_FILES) {
            RecentFilesScreen(
                onOpenDocument = { uri ->
                    navController.navigate("viewer?uri=${Uri.encode(uri.toString())}")
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
            )
        }

        composable(
            route = Routes.VIEWER,
            arguments = listOf(
                navArgument(Routes.VIEWER_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString(Routes.VIEWER_ARG).orEmpty()
            ViewerScreen(
                documentUri = if (uriString.isNotEmpty()) Uri.parse(uriString) else null,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        navigation(startDestination = Routes.SETTINGS, route = "settings_graph") {
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenAppearance = { navController.navigate(Routes.SETTINGS_APPEARANCE) },
                    onOpenAbout = { navController.navigate(Routes.SETTINGS_ABOUT) },
                )
            }

            composable(Routes.SETTINGS_APPEARANCE) {
                AppearanceSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Routes.SETTINGS_ABOUT) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
