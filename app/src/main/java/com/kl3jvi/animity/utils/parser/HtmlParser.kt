package com.kl3jvi.animity.utils.parser

import android.os.Build
import com.kl3jvi.animity.data.model.ui_models.AnimeInfoModel
import com.kl3jvi.animity.data.model.ui_models.EpisodeInfo
import com.kl3jvi.animity.data.model.ui_models.EpisodeModel
import com.kl3jvi.animity.data.model.ui_models.GenreModel
import com.kl3jvi.animity.domain.repositories.PersistenceRepository
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class HtmlParser @Inject constructor(
    private val preferences: PersistenceRepository
) : Parser {

    /**
     * It parses the HTML response and returns an AnimeInfoModel object
     *
     * @param response The response from the server
     * @return An AnimeInfoModel object
     */
    override fun parseAnimeInfo(response: String): AnimeInfoModel {
        val document = Jsoup.parse(response)
        val episodeInfo = document.getElementById("episode_page")
        val episodeList = episodeInfo.select("a").last()
        val endEpisode = episodeList.attr("ep_end")
        val alias = document.getElementById("alias_anime").attr("value")
        val id = document.getElementById("movie_id").attr("value")
        return AnimeInfoModel(
            id = id,
            alias = alias,
            endEpisode = endEpisode
        )
    }

    /**
     * It takes a list of HTML elements and returns a list of GenreModel objects.
     *
     * @param genreHtmlList The list of genres in the HTML document.
     * @return An ArrayList of GenreModel objects.
     */
    override fun getGenreList(genreHtmlList: Elements): List<GenreModel> {
        return genreHtmlList.map {
            val genreUrl = it.attr("href")
            val genreName = it.text()

            GenreModel(
                genreUrl = genreUrl,
                genreName = filterGenreName(genreName)
            )
        }
    }

    /**
     * It takes a string as an input, parses it using Jsoup, and returns an arraylist of EpisodeModel
     * objects
     *
     * @param response The response from the server.
     * @return An ArrayList of EpisodeModel objects.
     */
    override fun fetchEpisodeList(response: String): List<EpisodeModel> {
        val document = Jsoup.parse(response)
        val lists = document?.select("li")
        return lists?.map {
            val episodeUrl = it.select("a").first().attr("href").trim()
            val episodeNumber = it.getElementsByClass("name").first().text()
            val episodeType = it.getElementsByClass("cate").first().text()
            EpisodeModel(
                episodeNumber = episodeNumber,
                episodeType = episodeType,
                episodeUrl = episodeUrl
            )
        } ?: emptyList()
    }

    /**
     * Decrypts a string using AES
     *
     * @param encrypted The encrypted string
     * @param key "1234567890123456"
     * @param iv `"0000000000000000"`
     * @return The decrypted string
     */
    override fun decryptAES(encrypted: String, key: String, iv: String): String {
        val ix = IvParameterSpec(iv.toByteArray())
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ix)
        return String(cipher.doFinal(decodeBase64(encrypted)))
    }

    private fun decodeBase64(encrypted: String): ByteArray {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getDecoder().decode(encrypted)
        } else {
            android.util.Base64.decode(encrypted, android.util.Base64.URL_SAFE)
        }
    }

    /**
     * Encrypts a string using AES
     *
     * @param text The text to be encrypted
     * @param key The key used to encrypt the data.
     * @param iv The initialization vector. This is a random string that is used to encrypt the first
     * block of text.
     * @return The encrypted text
     */
    override fun encryptAes(text: String, key: String, iv: String): String {
        val ix = IvParameterSpec(iv.toByteArray())
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ix)
        return encodeBase64(cipher.doFinal(text.toByteArray()))
    }

    /**
     * It encodes the data into a base64 string.
     *
     * @param data The data to be encoded.
     * @return A string of the encoded data.
     */
    private fun encodeBase64(data: ByteArray): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(data)
        } else {
            android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT)
        }
    }

    /**
     * It takes in a response and an id, and returns a string.
     *
     * @param response The response from the server.
     * @param id The id of the episode you want to watch.
     * @return The encrypted string
     */
    override fun parseEncryptAjax(response: String, id: String): String {
        return try {
            val document = Jsoup.parse(response)
            val value2 = document.select("script[data-name=\"episode\"]").attr("data-value")
            val decrypted = decryptAES(
                value2,
                preferences.key.toString(),
                preferences.iv.toString()
            ).replace("\t", "").substringAfter(id)
            val encrypted = encryptAes(id, preferences.key.toString(), preferences.iv.toString())
            "id=$encrypted$decrypted&alias=$id"
        } catch (e: java.lang.Exception) {
            e.toString()
        }
    }

    /**
     * It takes a string as input, parses it using Jsoup, and returns an object of type EpisodeInfo
     *
     * @param response The response from the server.
     * @return EpisodeInfo
     */
    override fun parseMediaUrl(response: String): EpisodeInfo {
        val document = Jsoup.parse(response)
        val info = document?.getElementsByClass("vidcdn")?.first()?.select("a")
        val mediaUrl = info?.attr("data-video").toString()
        val nextEpisodeUrl =
            document.getElementsByClass("anime_video_body_episodes_r")?.select("a")?.first()
                ?.attr("href")
        val previousEpisodeUrl =
            document.getElementsByClass("anime_video_body_episodes_l")?.select("a")?.first()
                ?.attr("href")

        return EpisodeInfo(
            nextEpisodeUrl = nextEpisodeUrl,
            previousEpisodeUrl = previousEpisodeUrl,
            vidCdnUrl = mediaUrl
        )
    }

    /**
     * It takes a response from the server, decrypts it, parses it, and returns an array of URLs
     *
     * @param response The response from the server
     * @return an arraylist of urls.
     */
    override fun parseEncryptedUrls(response: String): List<String> {
        val urls = mutableListOf<String>()
        val data = JSONObject(response).getString("data")
        val decryptedData = decryptAES(
            data,
            preferences.secondKey.toString(),
            preferences.iv.toString()
        ).replace(
            """o"<P{#meme":""",
            """e":[{"file":"""
        )
        val res = JSONObject(decryptedData).getJSONArray("source")
        for (i in 0 until res.length()) {
            val label = res.getJSONObject(i).getString("label")
            if (label == "Auto") break
            urls.add(res.getJSONObject(i).getString("file"))
        }
        return urls
    }
}

private fun filterGenreName(genreName: String): String {
    return genreName.substringAfter(',')
}
