package org.openrndr.shape

import io.lacuna.artifex.*
import org.openrndr.math.Vector2


private fun Vector2.toVec2(): Vec2 {
    return Vec2(x, y)
}

private fun Vec2.toVector2(): Vector2 {
    return Vector2(x, y)
}

private fun Segment.toCurve2(): Curve2 {
    return when (control.size) {
        0 -> Line2.line(start.toVec2(), end.toVec2())
        1 -> Bezier2.curve(start.toVec2(), control[0].toVec2(), end.toVec2())
        2 -> Bezier2.curve(start.toVec2(), control[0].toVec2(), control[1].toVec2(), end.toVec2())
        else -> throw IllegalArgumentException("unsupported control count ${control.size}")
    }
}

private fun ShapeContour.toPath2(): Path2 {
    return Path2(segments.map { it.toCurve2() })
}

private fun ShapeContour.toRing2(): Ring2 {
    return Ring2(segments.map { it.toCurve2() })
}

private fun Shape.toRegion2(): Region2 {
    return Region2(contours.map { it.toRing2() })
}

private fun Region2.toShapes(): List<Shape> {
    val shapes = mutableListOf<Shape>()
    if (rings.isNotEmpty()) {

        var contours = mutableListOf<ShapeContour>()
        rings.forEach {

            if (it.isClockwise) {
                if (contours.isNotEmpty()) {
                    shapes.add(Shape(contours))
                }
                contours.clear()
            }
            contours.add(it.toShapeContour())
        }
        if (contours.isNotEmpty()) {
            shapes.add(Shape(contours))
        }
    }
    return shapes
}

private fun Curve2.toSegment(): Segment {
    return when (this) {
        is Line2 -> Segment(this.start().toVector2(), this.end().toVector2())
        is Bezier2.QuadraticBezier2 -> Segment(this.p0.toVector2(), this.p1.toVector2(), this.p2.toVector2())
        is Bezier2.CubicBezier2 -> Segment(this.p0.toVector2(), this.p1.toVector2(), this.p2.toVector2(), this.p3.toVector2())
        else -> throw IllegalArgumentException()
    }
}

private fun Ring2.toShapeContour(): ShapeContour {
    return ShapeContour(this.curves.map { it.toSegment() }, true)
}

fun difference(from: ShapeContour, subtract: ShapeContour): List<Shape> {
    val result = from.toRing2().region().difference(subtract.toRing2().region())
    return result.toShapes()
}

fun union(from: ShapeContour, add: ShapeContour): List<Shape> {
    val result = from.toRing2().region().union(add.toRing2().region())
    return result.toShapes()
}



fun intersection(from: ShapeContour, with: ShapeContour): List<Shape> {
    val result = from.toRing2().region().intersection(with.toRing2().region())
    return result.toShapes()
}



