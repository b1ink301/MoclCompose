package kr.b1ink.mocl.ui.site.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kr.b1ink.domain.model.MainItem
import kr.b1ink.mocl.R
import kr.b1ink.mocl.ui.components.ListDivider
import kr.b1ink.mocl.ui.components.ListText
import kr.b1ink.mocl.ui.components.LoadingRow
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AddDialog(
    modifier: Modifier = Modifier,
    viewModel: AddDialogViewModel = hiltViewModel(),
    onDismiss: (Boolean) -> Unit,
) {
    val uiState by viewModel.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()

    viewModel.collectSideEffect {
        when (it) {
            AddDialogUiSideEffect.OkDialog -> onDismiss(true)
            AddDialogUiSideEffect.CancelDialog -> onDismiss(false)
        }
    }

    AlertDialog(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 60.dp),
        containerColor = colorScheme.background,
        onDismissRequest = {},
        title = { Text(stringResource(R.string.select_boards)) },
        text = {
            when (uiState) {
                AddDialogUiState.Empty -> Text("항목이 없습니다")

                is AddDialogUiState.Error -> {
                    val message = (uiState as AddDialogUiState.Error).message ?: "Unknown Error"
                    Text(message)
                }

                AddDialogUiState.Loading -> LoadingRow()

                is AddDialogUiState.Success -> {
                    val list = (uiState as AddDialogUiState.Success).data
                    BuildSuccess(list, selectedItems) { item, isChecked ->
                        viewModel.toggleSelection(item, isChecked)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::addLinks,
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = colorScheme.tertiary)
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = viewModel::cancelDialog,
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = colorScheme.onTertiary)
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        })
}

@Composable
private fun BuildSuccess(
    list: List<MainItem>,
    selectedItems: Set<MainItem>,
    onSelected: (MainItem, Boolean) -> Unit,
) = LazyColumn {
    itemsIndexed(items = list, key = { _, item -> item.id }) { index, item ->
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp, 0.dp, 0.dp, 0.dp)
                    .clickable {
                        onSelected(item, !selectedItems.contains(item))
                    }, verticalAlignment = Alignment.CenterVertically
            ) {
                ListText(
                    text = { item.title }, modifier = Modifier.padding(vertical = 14.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Checkbox(
                    checked = selectedItems.contains(item),
                    colors = CheckboxDefaults.colors()
                        .copy(
                            checkedCheckmarkColor = Color.White, checkedBoxColor = colorScheme.tertiary
                        ),
                    onCheckedChange = { isChecked ->
                        onSelected(item, isChecked)
                    },
                )
            }
            ListDivider()
        }
    }
}