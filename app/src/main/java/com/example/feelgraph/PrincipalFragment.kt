package com.example.feelgraph

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.feelgraph.databinding.FragmentPrincipalBinding
import org.opencv.core.Point


class PrincipalFragment : Fragment() {

    private var _binding: FragmentPrincipalBinding? = null
    private val binding get() = _binding!!

    private enum class InteractionState {
        SEARCHING_STARTPOINT,
        INTERACTING_WITH_LINES
    }

    private val pdfUtils by lazy { PdfUtils(requireContext()) }
    private val vibratorManager by lazy { VibrationManager(requireContext()) }
    private val soundManager by lazy { SoundManager(requireContext())}
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var imageHandler: ImageHandler


    private var startPoint: Point? = null
    private var currentState = InteractionState.SEARCHING_STARTPOINT
    private var processedLines: List<Line> = emptyList()
    private var userPreference: String = "Vibration"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrincipalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.userPreference.observe(viewLifecycleOwner){ preference ->
            userPreference = preference
            updateCommandsText(preference)
        }

        imageHandler = ImageHandler()

        setupClickListeners()
        setupTouchListener()
    }


    private fun handlePdfResult(uri:Uri){
        pdfUtils.convertPdfToBitmap(uri)?.let{bitmap ->
            imageHandler.colourImage(bitmap) { startPoint, processedLines, processedBitmap ->
                this.startPoint = startPoint
                this.processedLines = processedLines
                binding.imageView.setImageBitmap(processedBitmap)
                currentState = InteractionState.SEARCHING_STARTPOINT
            }

        }
    }


    private val pdfPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                handlePdfResult(it) }
        }

    private fun setupClickListeners() {
        binding.addGraphicButton.setOnClickListener{
            binding.textView.visibility = View.GONE
            pdfPickerLauncher.launch("application/pdf")
        }

        binding.backButton.setOnClickListener{
            findNavController().navigate(R.id.action_principalFragment_to_selectionFragment)
        }

        binding.settingsButton.setOnClickListener{
            findNavController().navigate(R.id.action_principalFragment_to_settingsFragment)
        }

        setupTouchListener()

    }

    private fun setupTouchListener() {
        binding.imageView.setOnTouchListener { view, event ->
            handleTouch(view, event)
        }
    }

    private fun handleTouch(view: View, event: MotionEvent): Boolean {
        when (currentState) {
            InteractionState.SEARCHING_STARTPOINT -> {
                if (event.action == MotionEvent.ACTION_DOWN && startPoint != null) {
                    val touchPoint = TouchUtils.transformTouchToBitmapCoordinates(event.x, event.y, view, binding.imageView)
                    if (TouchUtils.isTouchNearStartPoint(touchPoint.x, touchPoint.y, startPoint!!, 25f)) {
                        currentState = InteractionState.INTERACTING_WITH_LINES
                        if(userPreference == "Vibration")
                            vibratorManager.vibrateStartPoint()
                        else
                            soundManager.playStartPointSound()

                        return true
                    }
                }
            }

            InteractionState.INTERACTING_WITH_LINES -> {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val touchedLine = processedLines.find { line ->
                        TouchUtils.isTouchNearLine(event.x, event.y, line, view, binding.imageView)
                    }

                    touchedLine?.let { line ->
                        val lineType = TouchUtils.getLineTypeBasedOnSlope(line)
                        if(userPreference == "Vibration")
                            vibratorManager.vibrateForLineType(lineType)
                        else
                            soundManager.playForLineType(lineType)
                        return true
                    }
                }
            }
        }
        return false
    }


    private fun updateCommandsText(preference: String){
        val textViewExplicative = binding.textView
        textViewExplicative.text = when(preference) {
            "Vibration" -> "COMANDOS: \n" +
                    "PUNTO DE INICIO: VIBRACION INTERMITENTE\nLINEA ASCENDENTE: VIBRACION INTENSA\n" +
                    "LINEA DESCENDENTE: VIBRACION DEBIL\nLINEA HORIZONTAL: VIBRACION ESTABLE"
            "Sound" -> "COMANDOS: \n" +
                    "PUNTO DE INICIO: SONIDO DESTELLO\nLINEA ASCENDENTE: SONIDO INTERMITENTE RAPIDO\n" +
                    "LINEA DESCENDENTE: SONIDO INTERMITENTE LENTO\nLINEA HORIZONTAL: SONIDO ESTABLE"
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
