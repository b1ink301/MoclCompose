package kr.b1ink.mocl.ui.components.appdrawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.b1ink.domain.model.MainNavOption
import kr.b1ink.domain.model.SiteType
import kr.b1ink.mocl.DrawerParams
import kr.b1ink.mocl.ui.theme.AppTheme
import kr.b1ink.mocl.util.singleClick

@Composable
fun <T> AppDrawerItem(
    modifier: Modifier = Modifier,
    item: AppDrawerItemInfo<T>,
    isChecked: Boolean,
    onClick: (options: T) -> Unit,
) =
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = singleClick {
                onClick(item.drawerOption)
            })
            .height(58.dp)
            .padding(16.dp, 0.dp, 8.dp, 0.dp),
    ) {
        Text(
            text = stringResource(id = item.title),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
        )

        if (isChecked) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.Check,
                contentDescription = "check_button",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }

class MainStateProvider : PreviewParameterProvider<AppDrawerItemInfo<SiteType>> {
    override val values = sequence {
        DrawerParams.drawerButtons.forEach { element ->
            yield(element)
        }
    }
}

@Preview
@Composable
fun AppDrawerItemPreview(@PreviewParameter(MainStateProvider::class) state: AppDrawerItemInfo<MainNavOption>) {
    AppTheme {
        AppDrawerItem(item = state, isChecked = true, onClick = {})
    }
}