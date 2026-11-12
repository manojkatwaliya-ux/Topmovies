package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.local.SettingsPreferences
import com.example.data.local.TopMoviesDatabase
import com.example.data.remote.NetworkModule
import com.example.data.repository.TopMoviesRepository
import com.example.ui.navigation.DetailRoute
import com.example.ui.navigation.GenreBrowseRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.LibraryRoute
import com.example.ui.navigation.LiveTvRoute
import com.example.ui.navigation.MoviesRoute
import com.example.ui.navigation.PlayerRoute
import com.example.ui.navigation.SearchRoute
import com.example.ui.navigation.SeriesRoute
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.TopMoviesTheme
import com.example.ui.viewmodel.CatalogMode
import com.example.ui.viewmodel.CatalogViewModel
import com.example.ui.viewmodel.DetailViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.LiveTvViewModel
import com.example.ui.viewmodel.SearchViewModel

private data class TopLevelDestination(
    val route: Any,
    val routeClass: kotlin.reflect.KClass<*>,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = TopMoviesRepository(
            api = NetworkModule.apiService,
            watchlistDao = TopMoviesDatabase.getInstance(applicationContext).watchlistDao(),
            settingsPreferences = SettingsPreferences(applicationContext)
        )

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            TopMoviesTheme {
                TopMoviesApp(
                    repository = repository,
                    widthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }
    }
}

@Composable
fun TopMoviesApp(
    repository: TopMoviesRepository,
    widthSizeClass: WindowWidthSizeClass
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var libraryInitialTab by rememberSaveable { mutableIntStateOf(0) }

    val topDestinations = remember {
        listOf(
            TopLevelDestination(
                route = HomeRoute,
                routeClass = HomeRoute::class,
                label = "Home",
                selectedIcon = Icons.Rounded.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_home"
            ),
            TopLevelDestination(
                route = MoviesRoute,
                routeClass = MoviesRoute::class,
                label = "Movies",
                selectedIcon = Icons.Rounded.Movie,
                unselectedIcon = Icons.Outlined.Movie,
                testTag = "nav_movies"
            ),
            TopLevelDestination(
                route = SeriesRoute,
                routeClass = SeriesRoute::class,
                label = "Series",
                selectedIcon = Icons.Rounded.Tv,
                unselectedIcon = Icons.Outlined.Tv,
                testTag = "nav_series"
            ),
            TopLevelDestination(
                route = LiveTvRoute,
                routeClass = LiveTvRoute::class,
                label = "Live TV",
                selectedIcon = Icons.Rounded.LiveTv,
                unselectedIcon = Icons.Outlined.LiveTv,
                testTag = "nav_livetv"
            ),
            TopLevelDestination(
                route = LibraryRoute,
                routeClass = LibraryRoute::class,
                label = "Library",
                selectedIcon = Icons.Rounded.Bookmark,
                unselectedIcon = Icons.Outlined.BookmarkBorder,
                testTag = "nav_library"
            )
        )
    }

    val showNavigationChrome = topDestinations.any { dest ->
        currentDestination?.hasRoute(dest.routeClass) == true
    }
    val useRail = widthSizeClass != WindowWidthSizeClass.Compact

    fun navigateTopLevel(dest: TopLevelDestination) {
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showNavigationChrome && !useRail) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    topDestinations.forEach { dest ->
                        val selected = currentDestination?.hasRoute(dest.routeClass) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (dest.route == LibraryRoute) libraryInitialTab = 0
                                navigateTopLevel(dest)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.label
                                )
                            },
                            label = {
                                Text(
                                    text = dest.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (showNavigationChrome && useRail) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    topDestinations.forEach { dest ->
                        val selected = currentDestination?.hasRoute(dest.routeClass) == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                if (dest.route == LibraryRoute) libraryInitialTab = 0
                                navigateTopLevel(dest)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.label
                                )
                            },
                            label = { Text(dest.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute
                ) {
                    composable<HomeRoute> {
                        val homeVm: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
                        HomeScreen(
                            viewModel = homeVm,
                            onOpenSearch = { navController.navigate(SearchRoute) },
                            onOpenMovies = { navigateTopLevel(topDestinations[1]) },
                            onOpenSeries = { navigateTopLevel(topDestinations[2]) },
                            onOpenLiveTv = { navigateTopLevel(topDestinations[3]) },
                            onOpenGenre = { genreId, name ->
                                navController.navigate(
                                    GenreBrowseRoute(
                                        filterId = genreId,
                                        filterName = name,
                                        isCountry = false
                                    )
                                )
                            },
                            onOpenCountry = { countryId, name ->
                                navController.navigate(
                                    GenreBrowseRoute(
                                        filterId = countryId,
                                        filterName = name,
                                        isCountry = true
                                    )
                                )
                            },
                            onOpenDetail = { videoId, type ->
                                navController.navigate(DetailRoute(videoId = videoId, contentType = type))
                            },
                            onPlayLiveChannel = { channel ->
                                if (channel.streamUrl.isNotBlank()) {
                                    navController.navigate(
                                        PlayerRoute(
                                            title = channel.tvName,
                                            streamUrl = channel.streamUrl,
                                            subtitle = "Live TV",
                                            contentId = channel.liveTvId,
                                            contentType = "tv"
                                        )
                                    )
                                }
                            },
                            onOpenApiSettings = {
                                libraryInitialTab = 2
                                navigateTopLevel(topDestinations[4])
                            }
                        )
                    }

                    composable<MoviesRoute> {
                        val moviesVm: CatalogViewModel = viewModel(
                            key = "catalog_movies",
                            factory = CatalogViewModel.factory(repository, CatalogMode.MOVIES)
                        )
                        CatalogScreen(
                            title = "Movies",
                            viewModel = moviesVm,
                            onOpenDetail = { videoId, type ->
                                navController.navigate(DetailRoute(videoId = videoId, contentType = type))
                            },
                            onOpenSearch = { navController.navigate(SearchRoute) }
                        )
                    }

                    composable<SeriesRoute> {
                        val seriesVm: CatalogViewModel = viewModel(
                            key = "catalog_series",
                            factory = CatalogViewModel.factory(repository, CatalogMode.SERIES)
                        )
                        CatalogScreen(
                            title = "TV Series",
                            viewModel = seriesVm,
                            onOpenDetail = { videoId, _ ->
                                navController.navigate(DetailRoute(videoId = videoId, contentType = "tvseries"))
                            },
                            onOpenSearch = { navController.navigate(SearchRoute) }
                        )
                    }

                    composable<LiveTvRoute> {
                        val liveTvVm: LiveTvViewModel = viewModel(
                            factory = LiveTvViewModel.factory(repository)
                        )
                        LiveTvScreen(
                            viewModel = liveTvVm,
                            onPlayChannel = { channel ->
                                if (channel.streamUrl.isNotBlank()) {
                                    navController.navigate(
                                        PlayerRoute(
                                            title = channel.tvName,
                                            streamUrl = channel.streamUrl,
                                            subtitle = "Live TV",
                                            contentId = channel.liveTvId,
                                            contentType = "tv"
                                        )
                                    )
                                }
                            }
                        )
                    }

                    composable<LibraryRoute> {
                        val libraryVm: LibraryViewModel = viewModel(
                            factory = LibraryViewModel.factory(repository)
                        )
                        LibraryScreen(
                            viewModel = libraryVm,
                            initialTab = libraryInitialTab,
                            onOpenDetail = { videoId, type ->
                                navController.navigate(DetailRoute(videoId = videoId, contentType = type))
                            }
                        )
                    }

                    composable<GenreBrowseRoute> { backStackEntry ->
                        val args = backStackEntry.toRoute<GenreBrowseRoute>()
                        val mode = if (args.isCountry) CatalogMode.COUNTRY else CatalogMode.GENRE
                        val genreVm: CatalogViewModel = viewModel(
                            key = "browse_${args.isCountry}_${args.filterId}",
                            factory = CatalogViewModel.factory(repository, mode, args.filterId)
                        )
                        CatalogScreen(
                            title = args.filterName,
                            viewModel = genreVm,
                            onOpenDetail = { videoId, type ->
                                navController.navigate(DetailRoute(videoId = videoId, contentType = type))
                            },
                            onOpenSearch = { navController.navigate(SearchRoute) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<SearchRoute> {
                        val searchVm: SearchViewModel = viewModel(
                            factory = SearchViewModel.factory(repository)
                        )
                        SearchScreen(
                            viewModel = searchVm,
                            onBack = { navController.popBackStack() },
                            onOpenDetail = { videoId, type ->
                                navController.navigate(DetailRoute(videoId = videoId, contentType = type))
                            },
                            onPlayLiveChannel = { channel ->
                                if (channel.streamUrl.isNotBlank()) {
                                    navController.navigate(
                                        PlayerRoute(
                                            title = channel.tvName,
                                            streamUrl = channel.streamUrl,
                                            subtitle = "Live TV",
                                            contentId = channel.liveTvId,
                                            contentType = "tv"
                                        )
                                    )
                                }
                            }
                        )
                    }

                    composable<DetailRoute> { backStackEntry ->
                        val args = backStackEntry.toRoute<DetailRoute>()
                        val detailVm: DetailViewModel = viewModel(
                            key = "detail_${args.contentType}_${args.videoId}",
                            factory = DetailViewModel.factory(repository, args.videoId, args.contentType)
                        )
                        DetailScreen(
                            viewModel = detailVm,
                            contentType = args.contentType,
                            onBack = { navController.popBackStack() },
                            onPlayStream = { title, streamUrl, subtitle, contentId, type ->
                                navController.navigate(
                                    PlayerRoute(
                                        title = title,
                                        streamUrl = streamUrl,
                                        subtitle = subtitle,
                                        contentId = contentId,
                                        contentType = type
                                    )
                                )
                            },
                            onOpenRelated = { videoId, type ->
                                navController.navigate(DetailRoute(videoId = videoId, contentType = type))
                            }
                        )
                    }

                    composable<PlayerRoute> { backStackEntry ->
                        val args = backStackEntry.toRoute<PlayerRoute>()
                        PlayerScreen(
                            title = args.title,
                            subtitle = args.subtitle,
                            streamUrl = args.streamUrl,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
