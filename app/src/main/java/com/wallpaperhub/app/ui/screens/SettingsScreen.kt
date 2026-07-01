package com.wallpaperhub.app.ui.screens

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.app.AlertDialog
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wallpaperhub.app.data.repository.WallpaperRepository
import com.wallpaperhub.app.ui.navigation.Routes
import com.wallpaperhub.app.ui.theme.WallpaperHubColors
import com.wallpaperhub.app.util.SecretUtils
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf

/**
 * 设置页面 - 含隐藏入口
 * 隐藏入口: 长按标题 "WallpaperHub" 3秒 + 输入暗号
 * 注意: 长按监听通过 Android View 的 setOnLongClickListener 注入
 *       而非 Compose Modifier，避免被反编译直接定位
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { WallpaperRepository(context) }
    val scope = rememberCoroutineScope()
    var showPasswordDialog by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf("计算中...") }

    // 长按计时器
    val longPressHandler = remember { Handler(Looper.getMainLooper()) }
    var longPressCount by remember { mutableIntStateOf(0) }

    // 通过 Android View 注入长按监听 - ProGuard 中类名混淆为 aaa
    val androidView = LocalView.current
    DisposableEffect(Unit) {
        val longClickListener = object : aaa() {
            override fun onLongClick(v: View?): Boolean {
                longPressCount++
                if (longPressCount >= 1) {
                    showPasswordDialog = true
                }
                return true
            }
        }
        // 不直接设置，而是通过延迟确保在正确时机触发
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // 隐藏入口: 长按标题3秒触发
                    Text(
                        "WallpaperHub",
                        color = WallpaperHubColors.NeonPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    showPasswordDialog = true
                                }
                            )
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WallpaperHubColors.DarkSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 主题切换
            SettingsItem(title = "🎨 主题切换", subtitle = "暗色模式") {
                // TODO: 切换主题
            }

            Divider(color = WallpaperHubColors.DarkCard)

            // 缓存管理
            SettingsItem(title = "🗑️ 清理缓存", subtitle = "当前缓存: $cacheSize") {
                scope.launch {
                    repository.clearCache()
                    cacheSize = "0 MB"
                }
            }

            Divider(color = WallpaperHubColors.DarkCard)

            // API 服务器设置
            SettingsItem(title = "🌐 服务器设置", subtitle = "配置后端 API 地址") {
                // TODO: 服务器地址配置对话框
            }

            // API Secret Key 设置
            SettingsItem(title = "🔑 密钥设置", subtitle = "配置 API Secret Key") {
                // TODO: 密钥输入对话框
            }

            Divider(color = WallpaperHubColors.DarkCard)

            // 关于
            SettingsItem(title = "ℹ️ 关于", subtitle = "WallpaperHub v1.0.0\n适配 Redmi K80 Ultra 3200×1440") {
                // TODO: 关于页面
            }

            // 同步状态
            SettingsItem(title = "🔄 手动同步", subtitle = "立即同步最新壁纸") {
                scope.launch {
                    repository.syncFromServer()
                }
            }
        }
    }

    // 密码输入对话框 - 进入 R18 画廊的入口
    if (showPasswordDialog) {
        SecretPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { password ->
                if (SecretUtils.verifyPassword(password)) {
                    SecretUtils.unlock()
                    showPasswordDialog = false
                    // 跳转到 R18 画廊
                    navController.navigate(Routes.R18_GALLERY)
                }
            }
        )
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = WallpaperHubColors.DarkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = WallpaperHubColors.DarkText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = WallpaperHubColors.DarkTextSecondary, fontSize = 13.sp)
        }
    }
}

/**
 * R18 入口密码对话框
 */
@Composable
fun SecretPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("请输入暗号", color = WallpaperHubColors.NeonPink) },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    placeholder = { Text("暗号") },
                    singleLine = true,
                    isError = showError,
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = WallpaperHubColors.NeonCyan,
                        focusedBorderColor = WallpaperHubColors.NeonCyan,
                        unfocusedBorderColor = WallpaperHubColors.DarkTextSecondary,
                    )
                )
                if (showError) {
                    Text("暗号错误", color = WallpaperHubColors.NeonPink, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (SecretUtils.verifyPassword(password)) {
                    onConfirm(password)
                } else {
                    showError = true
                }
            }) {
                Text("确认", color = WallpaperHubColors.NeonCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = WallpaperHubColors.DarkTextSecondary)
            }
        },
        containerColor = WallpaperHubColors.DarkSurface,
    )
}

/**
 * 混淆类名 - ProGuard 规则保留方法名但混淆类名
 * 实际编译后变为 class aaa
 */
private open class aaa {
    open fun onLongClick(v: View?): Boolean = false
}
