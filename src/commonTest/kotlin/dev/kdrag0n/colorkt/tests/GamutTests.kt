package dev.kdrag0n.colorkt.tests

import dev.kdrag0n.colorkt.Color.Companion.convert
import dev.kdrag0n.colorkt.cam.Zcam
import dev.kdrag0n.colorkt.cam.Zcam.Companion.toZcam
import dev.kdrag0n.colorkt.data.Illuminants
import dev.kdrag0n.colorkt.gamut.LchGamut.clipToLinearSrgb
import dev.kdrag0n.colorkt.gamut.OklabGamut.clipToLinearSrgb
import dev.kdrag0n.colorkt.rgb.LinearSrgb.Companion.toLinear
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.colorkt.tristimulus.CieXyzAbs
import dev.kdrag0n.colorkt.tristimulus.CieXyzAbs.Companion.DEFAULT_SDR_WHITE_LUMINANCE
import dev.kdrag0n.colorkt.tristimulus.CieXyzAbs.Companion.toAbs
import dev.kdrag0n.colorkt.ucs.lab.CieLab
import dev.kdrag0n.colorkt.ucs.lch.Oklch
import kotlin.test.Test
import kotlin.test.assertFalse

class GamutTests {
    private val cond = Zcam.ViewingConditions(
        surroundFactor = Zcam.ViewingConditions.SURROUND_AVERAGE,
        adaptingLuminance = 0.4 * DEFAULT_SDR_WHITE_LUMINANCE,
        backgroundLuminance = CieLab(50.0, 0.0, 0.0).toXyz().y * DEFAULT_SDR_WHITE_LUMINANCE,
        referenceWhite = Illuminants.D65.toAbs(DEFAULT_SDR_WHITE_LUMINANCE),
    )

    @Test
    fun oklabClipRgbk() {
        // R, G, B, black
        for (channel in 2 downTo -1) {
            val src = Srgb(0xff shl (channel * 8))
            val srcLinear = src.toLinear()
            val lch = src.convert<Oklch>()

            // Boost the chroma
            val clipped = lch.copy(chroma = lch.chroma * 5).toOklab().clipToLinearSrgb()

            // Now check
            assertApprox(clipped.r, srcLinear.r)
            assertApprox(clipped.g, srcLinear.g)
            assertApprox(clipped.b, srcLinear.b)
        }
    }

    @Test
    fun zcamClipRgbk() {
        // R, G, B, black
        for (channel in 2 downTo -1) {
            val src = Srgb(0xff shl (channel * 8))
            val srcLinear = src.toLinear()
            val zcam = src.convert<CieXyzAbs>().toZcam(cond, include2D = false)

            // Boost the chroma
            val clipped = zcam.copy(chroma = zcam.chroma * 5).clipToLinearSrgb()

            // Now check
            assertApprox(clipped.r, srcLinear.r)
            assertApprox(clipped.g, srcLinear.g)
            assertApprox(clipped.b, srcLinear.b)
        }
    }

    @Test
    fun zcamClipZeroChroma() {
        val zcam = Zcam(
            lightness = 50.0,
            chroma = 0.0,
            hue = 180.0,
            viewingConditions = cond,
        )

        val clipped = zcam.clipToLinearSrgb()
        assertFalse { clipped.r.isNaN() }
        assertFalse { clipped.g.isNaN() }
        assertFalse { clipped.b.isNaN() }
    }
}