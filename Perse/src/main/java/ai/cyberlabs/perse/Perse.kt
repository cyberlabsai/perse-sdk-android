/**
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Perse SDK Android.                                              |
 * | More About: https://www.getperse.com/                           |
 * | From CyberLabs.AI: https://cyberlabs.ai/                        |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2021             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

package ai.cyberlabs.perse

import ai.cyberlabs.perselite.PerseLite

/**
 * Heritage of PerseLite main class.
 */
open class Perse(
    apiKey: String,
    baseUrl: String = BuildConfig.BASE_URL
): PerseLite(apiKey, baseUrl)