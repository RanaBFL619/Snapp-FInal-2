package com.snapp.data.repository

import com.snapp.data.api.SnappApiClient
import com.snapp.data.model.layout.LayoutConfig
import com.snapp.data.model.layout.NavbarConfig
import com.snapp.data.model.layout.NavbarUserMenuItem
import com.snapp.data.model.layout.NavItem
import com.snapp.data.store.TokenStorage
import com.snapp.domain.repository.LayoutRepository

class LayoutRepositoryImpl(
    private val api: SnappApiClient,
    private val tokenStorage: TokenStorage
) : LayoutRepository {

    override suspend fun getLayout(): LayoutConfig {
        val raw = api.getLayout()
        return raw.copy(navbar = raw.navbar?.let { normalizeNavbar(it) })
    }

    /** Strip "/page" prefix from routes so they match web and work with "page/{slug}" navigation. */
    private fun normalizeRoute(route: String): String =
        if (route.startsWith("/page/")) route.removePrefix("/page/")
        else if (route == "/page") ""
        else route.trimStart('/')

    private fun normalizeNavItem(item: NavItem): NavItem =
        item.copy(
            route = normalizeRoute(item.route),
            children = item.children?.map { normalizeNavItem(it) }
        )

    private fun normalizeNavbar(navbar: NavbarConfig): NavbarConfig =
        navbar.copy(
            nav = navbar.nav.map { normalizeNavItem(it) },
            userMenu = navbar.userMenu.map { it.copy(route = normalizeRoute(it.route)) }
        )
}
