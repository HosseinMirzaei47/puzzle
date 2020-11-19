package com.example.puzzleapp.ui

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.puzzleapp.databinding.FragmentDragBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs


@AndroidEntryPoint
class DragFragment : Fragment(), OnTouchListener {


    private lateinit var binding: FragmentDragBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDragBinding.inflate(
            inflater, container, false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*  binding.puzzle.setOnTouchListener(object : OnTouchListener {
              override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                  when (p1?.action) {
                      MotionEvent.ACTION_MOVE -> {
                          println("jalil Move")
                      }
                      MotionEvent.ACTION_DOWN -> {
                          println("jalil down")
                      }
                      MotionEvent.ACTION_UP -> {
                          println("jalil up")


                      }
                  }
                  return true
              }

          })
  */

        binding.puzzle.setOnTouchListener(this)
        binding.puzzle1.setOnTouchListener(this)


        /*  binding.puzzle.setOnTouchListener(object : OnTouchListener {
              var DownPT = PointF() // Record Mouse Position When Pressed Down
              var StartPT = PointF() // Record Start Position of 'img'


              override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                  val x = event!!.rawX
                  val y = event!!.rawY
                  when (event?.action) {
                      MotionEvent.ACTION_MOVE -> {
                          println("jalil move event x= $x")
                          println("jalil move event y= $y")
                          println("jalil  event x= ${v?.x}")
                          println("jalil  event y= ${v?.y}")
                          binding.puzzle.x = (StartPT.x + x - DownPT.x).toInt().toFloat()
                          binding.puzzle.y = (StartPT.y + y - DownPT.y).toInt().toFloat()
                          StartPT[binding.puzzle.x] = binding.puzzle.y

                      }
                      MotionEvent.ACTION_DOWN -> {
                          println("jalil down x= ${event.x}")
                          println("jalil down y= ${event.y}")
                          DownPT[x] = y
                          StartPT[binding.puzzle.x] = binding.puzzle.y
                      }

                      MotionEvent.ACTION_UP -> {
                      }
                      else -> {
                      }
                  }
                  return true
              }


          })*/


    }

    private var _xDelta: Float = 0.0f
    private var _yDelta: Float = 0.0f
    private var _xDolta = 0f
    private var _yDolta = 0f
    private var direction = -1

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, event: MotionEvent): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                println("mmb x= ${event.x} y= ${event.y} raw x= ${event.rawX} y=${event.rawY}")
                println("mmb x= ${p0?.x} y= ${p0?.y}")
                println("mmb x ${p0!!.x - event.rawX} y ${p0.y - event.rawY}")
                _xDelta = p0!!.x - event.rawX
                _yDelta = p0.y - event.rawY
                _xDolta = event.rawX
                _yDolta = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                //println("mmb dolta $_xDolta  $_yDolta")
                // println("mmb ${abs(_xDolta - event.rawX)}  ${abs(_yDolta - event.rawY)}")
                //  println("mmb ${abs(_xDolta - event.rawX)}  ${abs(_yDolta - event.rawY)}")
                if (direction < 0) {
                    if (abs(_xDolta - event.rawX) > abs(_yDolta - event.rawY)) {
                        if ((_xDolta - event.rawX) < 0) {
                            //println("mmb chap kard")
                            direction = 3
                        } else {
                            //println("mmb rast kard")
                            direction = 1
                        }
                    } else {
                        if ((_yDolta - event.rawY) < 0) {
                            //println("mmb hava kard")
                            direction = 4
                        } else {
                            // println("mmb zamin kard")
                            direction = 2
                        }
                    }
                }

                p0!!.animate()
                    .x(event.rawX + _xDelta)
                    .y(event.rawY + _yDelta)
                    .rotationBy(50f)
                    .setDuration(0)
                    .start()
                /*if (direction==1||direction==3){
                    p0!!.animate()
                        .x(event.rawX + _xDelta)
                        .rotationBy(50f)
                        .setDuration(0)
                        .start()
                }else{
                    p0!!.animate()
                        .y(event.rawY + _yDelta)
                        .rotationBy(50f)
                        .setDuration(0)
                        .start()
                }*/
            }
            MotionEvent.ACTION_UP -> {
                when (direction) {
                    1 -> Toast.makeText(requireContext(), "chap", Toast.LENGTH_SHORT)
                        .show()
                    2 -> Toast.makeText(requireContext(), "hava", Toast.LENGTH_SHORT)
                        .show()
                    3 -> Toast.makeText(requireContext(), "rast", Toast.LENGTH_SHORT)
                        .show()
                    4 -> Toast.makeText(requireContext(), "bottom", Toast.LENGTH_SHORT)
                        .show()
                }

                direction = -1

            }
            else -> return false
        }
        return true
    }
}

