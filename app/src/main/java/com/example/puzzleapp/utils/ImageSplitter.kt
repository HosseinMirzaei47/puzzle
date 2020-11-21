package com.example.puzzleapp.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.example.puzzleapp.models.JigsawPiece
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt


fun splitImage(
    context: Context,
    imageView: ImageView,
    piecesNumber: Int
): ArrayList<JigsawPiece> {
    val cr = sqrt(piecesNumber.toDouble()).toInt()

    val pieces: ArrayList<JigsawPiece> = ArrayList<JigsawPiece>(piecesNumber)

    // Get the scaled bitmap of the source image
    val drawable = imageView.drawable as BitmapDrawable
    val bitmap = drawable.bitmap
    val dimensions: IntArray = getBitmapPositionInsideImageView(imageView)
    val scaledBitmapLeft = dimensions[0]
    val scaledBitmapTop = dimensions[1]
    val scaledBitmapWidth = dimensions[2]
    val scaledBitmapHeight = dimensions[3]
    val croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft)
    val croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop)
    val scaledBitmap =
        Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true)
    val croppedBitmap = Bitmap.createBitmap(
        scaledBitmap,
        abs(scaledBitmapLeft),
        abs(scaledBitmapTop),
        croppedImageWidth,
        croppedImageHeight
    )

    val combined =
        Bitmap.createBitmap(croppedImageWidth, croppedImageWidth, Bitmap.Config.ARGB_8888)
    val combinedcanvas = Canvas(combined)


    // Calculate the with and height of the pieces
    val pieceWidth = croppedImageWidth / cr
    val pieceHeight = croppedImageHeight / cr

    // Create each bitmap piece and add it to the resulting array
    var yCoord = 0
    for (row in 0 until cr) {
        var xCoord = 0
        for (col in 0 until cr) {
            // calculate offset for each piece
            var offsetX = 0
            var offsetY = 0
            if (col > 0) {
                offsetX = pieceWidth / 3
            }
            if (row > 0) {
                offsetY = pieceHeight / 3
            }

            // apply the offset to each piece
            val pieceBitmap = Bitmap.createBitmap(
                croppedBitmap,
                xCoord - offsetX,
                yCoord - offsetY,
                pieceWidth + offsetX,
                pieceHeight + offsetY
            )
            val piece = JigsawPiece(context)
            piece.setImageBitmap(pieceBitmap)
            piece.xCoord = xCoord - offsetX + imageView.left
            piece.yCoord = yCoord - offsetY + imageView.top
            piece.pieceWidth = pieceWidth + offsetX
            piece.pieceHeight = pieceHeight + offsetY

            // this bitmap will hold our final puzzle piece image
            val jigsawPiece = Bitmap.createBitmap(
                pieceWidth + offsetX,
                pieceHeight + offsetY,
                Bitmap.Config.ARGB_8888
            )

            // draw path
            val bumpSize = pieceHeight / 4
            val canvas = Canvas(jigsawPiece)
            val path = Path()
            path.moveTo(offsetX.toFloat(), offsetY.toFloat())
            if (row == 0) {
                // top side piece
                path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
            } else {
                // top bump
                path.lineTo(
                    offsetX + (pieceBitmap.width - offsetX) / 3.toFloat(),
                    offsetY.toFloat()
                )
                path.cubicTo(
                    offsetX + (pieceBitmap.width - offsetX) / 6.toFloat(),
                    offsetY - bumpSize.toFloat(),
                    offsetX + (pieceBitmap.width - offsetX) / 6 * 5.toFloat(),
                    offsetY - bumpSize.toFloat(),
                    offsetX + (pieceBitmap.width - offsetX) / 3 * 2.toFloat(),
                    offsetY.toFloat()
                )
                path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
            }
            if (col == cr - 1) {
                // right side piece
                path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())
            } else {
                // right bump
                path.lineTo(
                    pieceBitmap.width.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 3.toFloat()
                )
                path.cubicTo(
                    pieceBitmap.width - bumpSize.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 6.toFloat(),
                    pieceBitmap.width - bumpSize.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 6 * 5.toFloat(),
                    pieceBitmap.width.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 3 * 2.toFloat()
                )
                path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())
            }
            if (row == cr - 1) {
                // bottom side piece
                path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
            } else {
                // bottom bump
                path.lineTo(
                    offsetX + (pieceBitmap.width - offsetX) / 3 * 2.toFloat(),
                    pieceBitmap.height.toFloat()
                )
                path.cubicTo(
                    offsetX + (pieceBitmap.width - offsetX) / 6 * 5.toFloat(),
                    pieceBitmap.height - bumpSize.toFloat(),
                    offsetX + (pieceBitmap.width - offsetX) / 6.toFloat(),
                    pieceBitmap.height - bumpSize.toFloat(),
                    offsetX + (pieceBitmap.width - offsetX) / 3.toFloat(),
                    pieceBitmap.height.toFloat()
                )
                path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
            }
            if (col == 0) {
                // left side piece
                path.close()
            } else {
                // left bump
                path.lineTo(
                    offsetX.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 3 * 2.toFloat()
                )
                path.cubicTo(
                    offsetX - bumpSize.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 6 * 5.toFloat(),
                    offsetX - bumpSize.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 6.toFloat(),
                    offsetX.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 3.toFloat()
                )
                path.close()
            }

            // mask the piece
            val paint = Paint()
            paint.color = -0x1000000
            paint.style = Paint.Style.FILL
            canvas.drawPath(path, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(pieceBitmap, 0f, 0f, paint)

            // draw a white border
            var border = Paint()
            border.color = -0x7f000001
            border.style = Paint.Style.STROKE
            border.strokeWidth = 8.0f
            canvas.drawPath(path, border)

            // draw a black border
            border = Paint()
            border.color = -0x80000000
            border.style = Paint.Style.STROKE
            border.strokeWidth = 3.0f
            canvas.drawPath(path, border)

            // set the resulting bitmap to the piece
            piece.setImageBitmap(jigsawPiece)
            pieces.add(piece)
            xCoord += pieceWidth
        }
        yCoord += pieceHeight
    }
    return pieces
}

fun getBitmapPositionInsideImageView(imageView: ImageView): IntArray {
    val ret = IntArray(4)
    if (imageView.drawable == null) return ret

    // Get image dimensions
    // Get image matrix values and place them in an array
    val f = FloatArray(9)
    imageView.imageMatrix.getValues(f)

    // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
    val scaleX = f[Matrix.MSCALE_X]
    val scaleY = f[Matrix.MSCALE_Y]

    // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
    val d = imageView.drawable
    val origW = d.intrinsicWidth
    val origH = d.intrinsicHeight

    // Calculate the actual dimensions
    val actW = (origW * scaleX).roundToInt()
    val actH = (origH * scaleY).roundToInt()
    ret[2] = actW
    ret[3] = actH

    // Get image position
    // We assume that the image is centered into ImageView
    val imgViewW = imageView.width
    val imgViewH = imageView.height
    val top = (imgViewH - actH) / 2
    val left = (imgViewW - actW) / 2
    ret[0] = left
    ret[1] = top
    return ret
}

fun splitImage1(
    context: Context,
    imageView: ImageView,
    piecesNumber: Int
): Bitmap {
    val cr = sqrt(piecesNumber.toDouble()).toInt()

    val pieces: ArrayList<JigsawPiece> = ArrayList<JigsawPiece>(piecesNumber)

    // Get the scaled bitmap of the source image
    val drawable = imageView.drawable as BitmapDrawable
    val bitmap = drawable.bitmap
    val dimensions: IntArray = getBitmapPositionInsideImageView(imageView)
    val scaledBitmapLeft = dimensions[0]
    val scaledBitmapTop = dimensions[1]
    val scaledBitmapWidth = dimensions[2]
    val scaledBitmapHeight = dimensions[3]
    val croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft)
    val croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop)
    val scaledBitmap =
        Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true)
    val croppedBitmap = Bitmap.createBitmap(
        scaledBitmap,
        abs(scaledBitmapLeft),
        abs(scaledBitmapTop),
        croppedImageWidth,
        croppedImageHeight
    )

    val combined =
        Bitmap.createBitmap(scaledBitmapWidth, scaledBitmapHeight, Bitmap.Config.ARGB_8888)
    val combinedcanvas = Canvas(combined)


    // Calculate the with and height of the pieces
    val pieceWidth = croppedImageWidth / cr
    val pieceHeight = croppedImageHeight / cr

    // Create each bitmap piece and add it to the resulting array
    var yCoord = 0
    for (row in 0 until cr) {
        var xCoord = 0
        for (col in 0 until cr) {
            // calculate offset for each piece
            var offsetX = 0
            var offsetY = 0
            if (col > 0) {
                offsetX = pieceWidth / 3
            }
            if (row > 0) {
                offsetY = pieceHeight / 3
            }

            // apply the offset to each piece
            val pieceBitmap = Bitmap.createBitmap(
                croppedBitmap,
                xCoord - offsetX,
                yCoord - offsetY,
                pieceWidth + offsetX,
                pieceHeight + offsetY
            )
            val piece = JigsawPiece(context)
            piece.setImageBitmap(pieceBitmap)
            piece.xCoord = xCoord - offsetX + imageView.left
            piece.yCoord = yCoord - offsetY + imageView.top
            piece.pieceWidth = pieceWidth + offsetX
            piece.pieceHeight = pieceHeight + offsetY

            // this bitmap will hold our final puzzle piece image
            val jigsawPiece = Bitmap.createBitmap(
                pieceWidth + offsetX,
                pieceHeight + offsetY,
                Bitmap.Config.ARGB_8888
            )

            // draw path
            val bumpSize = pieceHeight / 4
            val canvas = Canvas(jigsawPiece)
            val path = Path()
            path.moveTo(offsetX.toFloat(), offsetY.toFloat())
            if (row == 0) {
                // top side piece
                path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
            } else {
                // top bump
                path.lineTo(
                    offsetX + (pieceBitmap.width - offsetX) / 3.toFloat(),
                    offsetY.toFloat()
                )
                path.cubicTo(
                    offsetX + (pieceBitmap.width - offsetX) / 6.toFloat(),
                    offsetY - bumpSize.toFloat(),
                    offsetX + (pieceBitmap.width - offsetX) / 6 * 5.toFloat(),
                    offsetY - bumpSize.toFloat(),
                    offsetX + (pieceBitmap.width - offsetX) / 3 * 2.toFloat(),
                    offsetY.toFloat()
                )
                path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
            }
            if (col == cr - 1) {
                // right side piece
                path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())
            } else {
                // right bump
                path.lineTo(
                    pieceBitmap.width.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 3.toFloat()
                )
                path.cubicTo(
                    pieceBitmap.width - bumpSize.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 6.toFloat(),
                    pieceBitmap.width - bumpSize.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 6 * 5.toFloat(),
                    pieceBitmap.width.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 3 * 2.toFloat()
                )
                path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())
            }
            if (row == cr - 1) {
                // bottom side piece
                path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
            } else {
                // bottom bump
                path.lineTo(
                    offsetX + (pieceBitmap.width - offsetX) / 3 * 2.toFloat(),
                    pieceBitmap.height.toFloat()
                )
                path.cubicTo(
                    offsetX + (pieceBitmap.width - offsetX) / 6 * 5.toFloat(),
                    pieceBitmap.height - bumpSize.toFloat(),
                    offsetX + (pieceBitmap.width - offsetX) / 6.toFloat(),
                    pieceBitmap.height - bumpSize.toFloat(),
                    offsetX + (pieceBitmap.width - offsetX) / 3.toFloat(),
                    pieceBitmap.height.toFloat()
                )
                path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
            }
            if (col == 0) {
                // left side piece
                path.close()
            } else {
                // left bump
                path.lineTo(
                    offsetX.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 3 * 2.toFloat()
                )
                path.cubicTo(
                    offsetX - bumpSize.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 6 * 5.toFloat(),
                    offsetX - bumpSize.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 6.toFloat(),
                    offsetX.toFloat(),
                    offsetY + (pieceBitmap.height - offsetY) / 3.toFloat()
                )
                path.close()
            }

            // mask the piece
            val paint = Paint()
            paint.color = -0x1000000
            paint.style = Paint.Style.FILL
            canvas.drawPath(path, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(pieceBitmap, 0f, 0f, paint)

            // draw a white border
            var border = Paint()
            border.color = -0x7f000001
            border.style = Paint.Style.STROKE
            border.strokeWidth = 8.0f
            canvas.drawPath(path, border)

            // draw a black border
            border = Paint()
            border.color = -0x80000000
            border.style = Paint.Style.STROKE
            border.strokeWidth = 3.0f
            canvas.drawPath(path, border)

            // set the resulting bitmap to the piece
            combinedcanvas.drawBitmap(
                jigsawPiece,
                (xCoord - offsetX).toFloat(),
                (yCoord - offsetY).toFloat(),
                null
            )
            piece.setImageBitmap(jigsawPiece)
            pieces.add(piece)
            xCoord += pieceWidth
        }
        yCoord += pieceHeight
    }
    return combined
    //  return pieces
}
