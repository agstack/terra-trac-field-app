package org.technoserve.farmcollector.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.Language
import org.technoserve.farmcollector.ui.theme.Teal
import org.technoserve.farmcollector.ui.theme.Turquoise
import org.technoserve.farmcollector.ui.theme.White
import org.technoserve.farmcollector.ui.screens.settings.LanguageSelector
import org.technoserve.farmcollector.viewmodels.LanguageViewModel
import java.util.Locale


/**
 *
 *  This function is used to Display the home page of our application
 */

@Composable
fun Home(
    navController: NavController,
    languageViewModel: LanguageViewModel,
    languages: List<Language>
) {

    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(currentLanguage) {
        languageViewModel.updateLocale(context = context, Locale(currentLanguage.code))
    }

    Column(
        Modifier
            .padding(top = 20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Add language selector here and align on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageSelector(viewModel = languageViewModel, languages = languages)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .padding(bottom = 10.dp)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .padding(top = 10.dp)
                    .align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold,
                color = Turquoise, // Using the custom Turquoise color
                style = TextStyle(fontSize = 24.sp)
            )
        }

        Box(
            modifier = Modifier
                .padding(30.dp)
                .background(
                    color = Teal,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    navController.navigate("siteList")
                }
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.get_started),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = White
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.fillMaxHeight(0.2f))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_intro),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                modifier = Modifier.padding(start = 20.dp, end = 5.dp),
                text = stringResource(id = R.string.developed_by),
                color = Teal // Apply Teal color for the developer label
            )
            Image(
                painter = painterResource(id = R.drawable.tns_labs),
                contentDescription = null,
                modifier = Modifier
                    .width(130.dp)
                    .height(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))
    }
}


//import androidx.compose.ui.test.*
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.navigation.testing.TestNavHostController
//import androidx.test.core.app.ApplicationProvider
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mockito.mock
//import org.robolectric.RobolectricTestRunner
//import org.robolectric.annotation.Config
//import org.technoserve.farmcollector.database.models.Language
//import org.technoserve.farmcollector.ui.screens.home.Home
//import org.technoserve.farmcollector.viewmodels.LanguageViewModel
//
//@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
//class HomeKtTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    private lateinit var navController: TestNavHostController
//    private lateinit var languageViewModel: LanguageViewModel
//    private val testLanguages = listOf(
//        Language("en", "English"),
//        Language("es", "Spanish")
//    )
//
//    @Before
//    fun setup() {
//        // Use ApplicationProvider for a context in unit tests
//        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
//        navController = TestNavHostController(context)
//        languageViewModel = mock(LanguageViewModel::class.java)
//    }
//
//    @Test
//    fun homeScreen_displaysAppName() {
//        composeTestRule.setContent {
//            Home(
//                navController = navController,
//                languageViewModel = languageViewModel,
//                languages = testLanguages
//            )
//        }
//
//        composeTestRule
//            .onNodeWithText("TerraTrac") // Replace with the actual string if not resource-based
//            .assertExists()
//            .assertIsDisplayed()
//    }
//
//    @Test
//    fun homeScreen_displaysGetStartedButton() {
//        composeTestRule.setContent {
//            Home(
//                navController = navController,
//                languageViewModel = languageViewModel,
//                languages = testLanguages
//            )
//        }
//
//        composeTestRule
//            .onNodeWithText("Get Started")
//            .assertExists()
//            .assertIsDisplayed()
//            .assertHasClickAction()
//    }
//
//    @Test
//    fun homeScreen_clickGetStartedNavigatesToSiteList() {
//        composeTestRule.setContent {
//            Home(
//                navController = navController,
//                languageViewModel = languageViewModel,
//                languages = testLanguages
//            )
//        }
//
//        composeTestRule
//            .onNodeWithText("Get Started") // Replace with actual string if not resource-based
//            .performClick()
//
//        // Verify navigation occurred
//        assertEquals("siteList", navController.currentDestination?.route)
//    }
//}

