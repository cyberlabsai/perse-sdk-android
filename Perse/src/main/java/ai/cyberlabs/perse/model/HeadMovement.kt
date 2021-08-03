/**
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Perse SDK Android.                                              |
 * | More About: https://www.getperse.com/                           |
 * | From CyberLabs.AI: https://cyberlabs.ai/                        |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2021             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

package ai.cyberlabs.perse.model

/**
 * Enum class to represent the HeadMovement results
 */
enum class HeadMovement {
    UNDEFINED,
    VERTICAL_UP,
    VERTICAL_SUPER_UP,
    VERTICAL_NORMAL,
    VERTICAL_DOWN,
    VERTICAL_SUPER_DOWN,
    HORIZONTAL_LEFT,
    HORIZONTAL_SUPER_LEFT,
    HORIZONTAL_NORMAL,
    HORIZONTAL_RIGHT,
    HORIZONTAL_SUPER_RIGHT,
    TILT_LEFT,
    TILT_SUPER_LEFT,
    TILT_NORMAL,
    TILT_RIGHT,
    TILT_SUPER_RIGHT
}