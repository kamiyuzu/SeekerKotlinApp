package com.seeker.views.screens

import androidx.annotation.StringRes
import com.seeker.R

enum class Screens(@StringRes val title: Int) {
    Login(title = R.string.login_name),
    Index(title = R.string.index_name),
    QR(title = R.string.qr_name),
    Details(title = R.string.details_name),
    Categories(title = R.string.categories_name),
}
