package kr.b1ink.mocl.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kr.b1ink.mocl.R
import kr.b1ink.mocl.ui.previews.AllPreviews
import kr.b1ink.mocl.ui.theme.AppTheme

@Composable
fun BackButton(onClick: OnClickFunction) {
    IconButton(
        onClick = onClick, modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            stringResource(id = R.string.ic_arrow_back),
            modifier = Modifier
                .size(32.dp)
                .padding(0.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

}

@AllPreviews
@Composable
fun BackButtonPreview() {
    AppTheme {
        BackButton() {}
    }
}