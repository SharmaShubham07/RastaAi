package com.example.rastaai.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rastaai.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditCourseFragment : Fragment(R.layout.fragment_add_edit_course) {

    private val vm: AddEditCourseViewModel by viewModels()
    private val args: AddEditCourseFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleHeader = view.findViewById<android.widget.TextView>(R.id.titleHeader)
        val inputTitle = view.findViewById<TextInputEditText>(R.id.inputTitle)
        val inputDesc = view.findViewById<TextInputEditText>(R.id.inputDesc)
        val inputLessons = view.findViewById<TextInputEditText>(R.id.inputLessons)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        val courseId: Long = args.courseId
        val isEditing = courseId != -1L

        // Set header title based on mode
        titleHeader.text = if (isEditing) {
            "Edit Course"
        } else {
            "Create New Course"
        }

        // Setup edge-to-edge
        setupEdgeToEdge(view)

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // Load categories and set spinner adapter
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.categories.collectLatest { list ->
                val names = list.map { it.name }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    names
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinner.adapter = adapter

                // Preselect category when editing
                if (isEditing) {
                    launch {
                        val course = vm.loadCourse(courseId)
                        course?.let {
                            val index = list.indexOfFirst { c -> c.id == it.categoryId }
                            if (index >= 0) {
                                spinner.setSelection(index)
                            }
                        }
                    }
                }
            }
        }

        // Load existing course if editing
        if (isEditing) {
            viewLifecycleOwner.lifecycleScope.launch {
                vm.loadCourse(courseId)?.let { course ->
                    inputTitle.setText(course.title)
                    inputDesc.setText(course.description)
                    inputLessons.setText(course.lessons.toString())
                }
            }
        }

        btnSave.setOnClickListener {
            saveCourse(inputTitle, inputDesc, inputLessons, spinner)
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveCourse(
        inputTitle: TextInputEditText,
        inputDesc: TextInputEditText,
        inputLessons: TextInputEditText,
        spinner: Spinner
    ) {
        val title = inputTitle.text.toString().trim()
        val desc = inputDesc.text.toString().trim()
        val lessons = inputLessons.text.toString().toIntOrNull() ?: 0
        val selectedPosition = spinner.selectedItemPosition
        val category = if (selectedPosition >= 0) {
            vm.categories.value.getOrNull(selectedPosition)
        } else {
            null
        }

        // Validation
        when {
            title.isEmpty() -> {
                showError("Title is required")
                inputTitle.requestFocus()
                return
            }
            lessons <= 0 -> {
                showError("Enter valid lesson count")
                inputLessons.requestFocus()
                return
            }
            selectedPosition < 0 -> {
                showError("Please select a category")
                return
            }
        }

        vm.saveCourse(
            id = if (args.courseId == -1L) null else args.courseId,
            title = title,
            description = desc,
            categoryId = category?.id,
            categoryName = category?.name,
            lessons = lessons
        ) { result ->
            result.onSuccess {
                findNavController().navigateUp()
            }.onFailure {
                showError("Failed to save: ${it.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setupEdgeToEdge(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                systemBars.bottom
            )
            insets
        }
    }
}