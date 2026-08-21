package info.bvlion.wearlink

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import info.bvlion.wearlink.compose.ErrorDialogCompose
import info.bvlion.wearlink.compose.LoadingCompose
import info.bvlion.wearlink.compose.MenuBottomNavigation
import info.bvlion.wearlink.compose.MenuList
import info.bvlion.wearlink.compose.RequestCreate
import info.bvlion.wearlink.compose.RequestHistoryDetailContent
import info.bvlion.wearlink.compose.RequestHistoryList
import info.bvlion.wearlink.compose.SavedRequestList
import info.bvlion.wearlink.data.AppConstants
import info.bvlion.wearlink.data.Constant
import info.bvlion.wearlink.data.RequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.ResponseParams
import info.bvlion.wearlink.data.ResponseParams.Companion.parseResponseParams
import info.bvlion.wearlink.mobile.R
import info.bvlion.wearlink.request.WearMobileConnector
import info.bvlion.wearlink.ui.MainAnimatedVisibility
import info.bvlion.wearlink.ui.theme.WearLinkTheme
import kotlinx.coroutines.flow.collectLatest

class MobileMainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

  private val viewModel by viewModels<MobileMainViewModel>()
  private val messageClient by lazy { Wearable.getMessageClient(this) }
  private val isLocalNetworkPermissionGranted = mutableStateOf(false)

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    setContent {
      val errorDialog = viewModel.errorDialog.collectAsState()
      val viewMode = viewModel.viewMode.collectAsState()

      val savedRequests = viewModel.savedRequest.collectAsState()
      val savedResponses = viewModel.savedResponse.collectAsState()
      val loading = viewModel.loading.collectAsState()

      val bottomMenuIndex = rememberSaveable { mutableIntStateOf(0) }
      val editRequest = rememberSaveable { mutableStateOf<RequestParams?>(null) }
      val editRequestIndex = rememberSaveable { mutableIntStateOf(-1) }
      val response = remember { mutableStateOf<ResponseParams?>(null) }

      val snackbarHostState = remember { SnackbarHostState() }
      val closeLabel = stringResource(R.string.close)

      val getString = { id: Int -> getString(id) }
      val syncErrorTitle = stringResource(R.string.sync_wearable_error_title)
      val syncErrorDescription =
        stringResource(R.string.sync_wearable_error_description, Constant.MAX_SYNC_COUNT)
      val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
      ) {
        isLocalNetworkPermissionGranted.value = ContextCompat.checkSelfPermission(
          this@MobileMainActivity,
          Manifest.permission.ACCESS_LOCAL_NETWORK
        ) == PackageManager.PERMISSION_GRANTED
      }
      val isDarkMode = AppConstants.isDarkMode(viewMode.value, isSystemInDarkTheme())

      LaunchedEffect(Unit) {
        viewModel.snackbar.collectLatest {
          snackbarHostState.currentSnackbarData?.dismiss()
          snackbarHostState.showSnackbar(
            message = it,
            actionLabel = closeLabel,
            duration = SnackbarDuration.Short,
          )
        }
      }
      LaunchedEffect(Unit) {
        viewModel.clipboard.collect {
          clipboardManager.setPrimaryClip(it)
        }
      }

      SideEffect {
        enableEdgeToEdge(
          statusBarStyle = AppConstants.getStatusBarStyle(viewMode.value, AppConstants.isSystemInDarkTheme(this)),
          navigationBarStyle = AppConstants.getSystemBarStyle(viewMode.value)
        )
      }

      BackHandler(response.value != null || bottomMenuIndex.intValue > 1) {
        if (response.value != null) {
          response.value = null
        } else if (bottomMenuIndex.intValue > 1) {
          bottomMenuIndex.intValue = 0
        }
      }

      WearLinkTheme(isDarkMode) {
        errorDialog.value?.let {
          ErrorDialogCompose(it) {
            viewModel.dismissErrorDialog()
          }
        }

        Surface(
          modifier = Modifier.imePadding().fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            content = {
              Box(modifier = Modifier.fillMaxSize()) {
                MainAnimatedVisibility(bottomMenuIndex.intValue == 0) {
                  SavedRequestList(
                    requests = if (savedRequests.value == null) {
                      emptyList()
                    } else {
                      savedRequests.value?.let {
                        if (it.isEmpty()) {
                          null // 起動時に入力画面が出ないようにする
                        } else {
                          it.parseRequestParams()
                        }
                      } ?: emptyList()
                    },
                    newCreateClick = { bottomMenuIndex.intValue = 1 },
                    topPadding = it.calculateTopPadding(),
                    bottomPadding = it.calculateBottomPadding(),
                    watchSync = { index, request ->
                      viewModel.saveRequest(index, request, syncErrorTitle, syncErrorDescription, null)
                    },
                    edit = { index, request ->
                      editRequestIndex.intValue = index
                      editRequest.value = request
                      bottomMenuIndex.intValue = 1
                    },
                    send = { request -> viewModel.sendRequest(request, getString) },
                    moveUp = { request -> viewModel.moveRequest(request.id, -1) },
                    moveDown = { request -> viewModel.moveRequest(request.id, 1) }
                  )
                  if (loading.value) {
                    LoadingCompose()
                  }
                }
                MainAnimatedVisibility(bottomMenuIndex.intValue == 1) {
                  RequestCreate(
                    editRequest.value?.title ?: "",
                    editRequest.value?.url ?: "https://",
                    editRequest.value?.method ?: Constant.HttpMethod.GET,
                    editRequest.value?.bodyType ?: Constant.BodyType.QUERY,
                    editRequest.value?.headers
                      ?: "Content-type:application/x-www-form-urlencoded\nUser-Agent:myApp\n",
                    editRequest.value?.parameters ?: "a=b",
                    editRequest.value?.watchSync ?: false,
                    editRequest.value?.watchfaceShortcut ?: false,
                    editRequestIndex.intValue,
                    it.calculateTopPadding(),
                    it.calculateBottomPadding(),
                    cancel = {
                      editRequest.value = null
                      editRequestIndex.intValue = -1
                      bottomMenuIndex.intValue = 0
                    },
                    save = { index, request ->
                      editRequest.value = null
                      editRequestIndex.intValue = -1
                      viewModel.saveRequest(index, request, syncErrorTitle, syncErrorDescription) { resId, title ->
                        getString(resId, title)
                      }
                      bottomMenuIndex.intValue = 0
                    },
                    delete = {
                      editRequest.value = null
                      editRequestIndex.intValue = -1
                      viewModel.deleteRequest(it, getString)
                      bottomMenuIndex.intValue = 0
                    },
                    addShortcut = { request ->
                      viewModel.addShortcut(request, getString)
                    },
                    defaultId = editRequest.value?.id
                  )
                }
                MainAnimatedVisibility(bottomMenuIndex.intValue == 2) {
                  RequestHistoryList(
                    if (savedResponses.value.isEmpty()) {
                      emptyList()
                    } else {
                      savedResponses.value.parseResponseParams()
                    },
                    it.calculateTopPadding(),
                    it.calculateBottomPadding()
                  ) {
                    response.value = it
                  }
                }
                MainAnimatedVisibility(bottomMenuIndex.intValue == 3) {
                  MenuList(
                    it.calculateTopPadding(),
                    it.calculateBottomPadding(),
                    viewMode.value,
                    saveViewMode = { viewModel.saveViewMode(it) },
                    syncWatch = {
                      viewModel.syncWatch(getString)
                    },
                    historyDelete = {
                      viewModel.deleteResponses(getString)
                    },
                    savePasteRequest = {
                      viewModel.saveRequest(it, getString)
                    },
                    copyToClipboard = {
                      viewModel.copyToClipboard(it, getString)
                    },
                    isLocalNetworkPermissionGranted = isLocalNetworkPermissionGranted.value,
                    requestLocalNetworkPermission = {
                      if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
                        !isLocalNetworkPermissionGranted.value
                      ) {
                        localNetworkPermissionLauncher.launch(
                          Manifest.permission.ACCESS_LOCAL_NETWORK
                        )
                      }
                    }
                  )
                }
                Box( // status bar の透け感
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(it.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                    .align(Alignment.TopCenter)
                )
              }
            },
            bottomBar = {
              MenuBottomNavigation(bottomMenuIndex) {
                if (it != 1) {
                  editRequest.value = null
                  editRequestIndex.intValue = -1
                }
                response.value = null
              }
            }
          )

          val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
          )
          LaunchedEffect(response.value) {
            if (response.value != null) {
              sheetState.expand()
            } else {
              sheetState.hide()
            }
          }

          response.value?.let {
            ModalBottomSheet(
              onDismissRequest = { response.value = null },
              sheetState = sheetState,
              shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            ) {
              RequestHistoryDetailContent(it, 0.dp)
            }
          }
        }
      }
    }
  }

  override fun onMessageReceived(messageEvent: MessageEvent) {
    when (messageEvent.path) {
      WearMobileConnector.MOBILE_SAVE_RESPONSE_PATH -> {
        val responses = String(messageEvent.data)
        if (responses.isNotEmpty()) {
          viewModel.saveWearResponses(responses)
        }
      }
      WearMobileConnector.MOBILE_REQUEST_SYNC_PATH -> {
        viewModel.syncWatch { getString(it) }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
      isLocalNetworkPermissionGranted.value = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_LOCAL_NETWORK
      ) == PackageManager.PERMISSION_GRANTED
    }
    messageClient.addListener(this)
    viewModel.requestResponsesToWear()
  }

  override fun onPause() {
    super.onPause()
    messageClient.removeListener(this)
  }
}

@Preview(showBackground = true)
@Composable
fun HomeEmptyPreview() {
  WearLinkTheme {
    Scaffold(
      content = { SavedRequestList(emptyList(), bottomPadding = it.calculateBottomPadding()) },
      bottomBar = { MenuBottomNavigation(remember { mutableIntStateOf(0) }) { } }
    )
  }
}

@Preview(showBackground = true)
@Composable
fun HomeListPreview() {
  WearLinkTheme {
    Scaffold(
      content = {
        SavedRequestList(
          List(15) {
            RequestParams(
              "ぐーぐる$it",
              "https://www.google.com/",
              Constant.HttpMethod.GET,
              Constant.BodyType.QUERY,
            )
          },
          bottomPadding = it.calculateBottomPadding()
        )
                },
      bottomBar = { MenuBottomNavigation(remember { mutableIntStateOf(0) }) { } }
    )
  }
}
