/*****************************************************************************
 * AlbumsProvider.kt
 *****************************************************************************
 * Copyright © 2019 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.vlc.providers.medialibrary

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.videolan.medialibrary.Medialibrary
import org.videolan.medialibrary.media.Album
import org.videolan.medialibrary.media.Artist
import org.videolan.medialibrary.media.Genre
import org.videolan.medialibrary.media.MediaLibraryItem
import org.videolan.vlc.util.Settings
import org.videolan.vlc.viewmodels.SortableModel


@ExperimentalCoroutinesApi
class AlbumsProvider(val parent : MediaLibraryItem?, context: Context, scope: SortableModel) : MedialibraryProvider<Album>(context, scope) {

    override val sortKey = "${super.sortKey}_${parent?.javaClass?.simpleName}"
    override fun canSortByDuration() = true
    override fun canSortByReleaseDate() = true

    init {
        sort = Settings.getInstance(context).getInt(sortKey, if (parent is Artist) Medialibrary.SORT_RELEASEDATE else Medialibrary.SORT_DEFAULT)
        desc = Settings.getInstance(context).getBoolean("${sortKey}_desc", false)
    }

    override fun getAll() : Array<Album> = when (parent) {
        is Artist -> parent.getAlbums(sort, scope.desc)
        is Genre -> parent.getAlbums(sort, scope.desc)
        else -> medialibrary.getAlbums(sort, scope.desc)
    }

    override fun getPage(loadSize: Int, startposition: Int) : Array<Album> {
        val list = if (scope.filterQuery == null) when(parent) {
            is Artist -> parent.getPagedAlbums(sort, scope.desc, loadSize, startposition)
            is Genre -> parent.getPagedAlbums(sort, scope.desc, loadSize, startposition)
            else -> medialibrary.getPagedAlbums(sort, scope.desc, loadSize, startposition)
        } else when(parent) {
            is Artist -> parent.searchAlbums(scope.filterQuery, sort, scope.desc, loadSize, startposition)
            is Genre -> parent.searchAlbums(scope.filterQuery, sort, scope.desc, loadSize, startposition)
            else -> medialibrary.searchAlbum(scope.filterQuery, sort, scope.desc, loadSize, startposition)
        }
        return list.also { completeHeaders(it, startposition) }
    }

    override fun getTotalCount() = if (scope.filterQuery == null) when(parent) {
        is Artist -> parent.albumsCount
        is Genre -> parent.albumsCount
        else -> medialibrary.albumsCount
    } else when (parent) {
        is Artist -> parent.searchAlbumsCount(scope.filterQuery)
        is Genre -> parent.searchAlbumsCount(scope.filterQuery)
        else -> medialibrary.getAlbumsCount(scope.filterQuery)
    }
}