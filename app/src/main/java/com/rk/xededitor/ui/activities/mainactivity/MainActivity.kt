package com.rk.xededitor.ui.activities.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rk.xededitor.MainActivity.file.getFragmentType
import com.rk.xededitor.MainActivity.tabs.core.FragmentType
import com.rk.xededitor.ui.components.ScrollableTabLayout
import com.rk.xededitor.ui.theme.KarbonTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    lateinit var viewModel: MyViewModel
    
    class MyViewModel : ViewModel() {
        val _openedTabs = mutableListOf<String>()
        val _openedTabsTypes = mutableListOf<FragmentType>()
        val _openedTabsFiles = mutableListOf<File?>()
        
        fun newTab(title:String,type: FragmentType) {
            _openedTabs.add(title)
            _openedTabsTypes.add(type)
            _openedTabsFiles.add(null)
        }
        
        fun newTab(file: File,type: FragmentType? = null) {
            _openedTabs.add(file.name)
            _openedTabsTypes.add(type ?: file.getFragmentType())
            _openedTabsFiles.add(file)
        }
        
        fun removeAt(index:Int){
            _openedTabs.removeAt(index)
            _openedTabsTypes.removeAt(index)
            _openedTabsFiles.removeAt(index)
        }
        
        
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            viewModel = viewModel()
            KarbonTheme {
                MainScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        
        
        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                //drawer content
            }
        }, content = {
            Scaffold(topBar = {
                TopAppBar(title = { }, navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                })
            }) { paddingValues ->
                
                ScrollableTabLayout(
                    Modifier.padding(paddingValues),
                    tabs = viewModel._openedTabs,
                    content = {
                        Box(Modifier.fillMaxSize()) {
                            viewModel._openedTabsFiles[it]?.let { it1 -> Text(it1.absolutePath) }
                        }
                    },
                    animation = false
                )
                
                
                
            }
        })
    }
}
