// ui/main/AddEditCourseFragment.kt
package com.example.rastaai.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
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

    private val SENTINEL = -1L
    private val courseId: Long by lazy { args.courseId }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val inputTitle = view.findViewById<TextInputEditText>(R.id.inputTitle)
        val inputDesc = view.findViewById<TextInputEditText>(R.id.inputDesc)
        val inputLessons = view.findViewById<TextInputEditText>(R.id.inputLessons)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        val isEditing = courseId != SENTINEL

        view.findViewById<android.widget.TextView>(R.id.titleHeader).text =
            if (isEditing) "Edit Course" else "Create New Course"

        // Load categories
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.categories.collectLatest { list ->

                val names = mutableListOf("Select category")
                names.addAll(list.map { it.name })

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                if (isEditing) {
                    launch {
                        val course = vm.loadCourse(courseId)
                        course?.let {
                            val index = list.indexOfFirst { c -> c.id == it.categoryId }
                            if (index >= 0) spinner.setSelection(index + 1)
                        }
                    }
                }
            }
        }

        // If editing load values
        if (isEditing) {
            viewLifecycleOwner.lifecycleScope.launch {
                vm.loadCourse(courseId)?.let {
                    inputTitle.setText(it.title)
                    inputDesc.setText(it.description)
                    inputLessons.setText(it.lessons.toString())
                }
            }
        }

        btnSave.setOnClickListener {
            val title = inputTitle.text.toString().trim()
            val desc = inputDesc.text.toString().trim()
            val lessons = inputLessons.text.toString().toIntOrNull() ?: 0
            val pos = spinner.selectedItemPosition

            if (title.isEmpty()) return@setOnClickListener show("Title required")
            if (lessons <= 0) return@setOnClickListener show("Enter valid lessons")

            if (pos == 0) return@setOnClickListener show("Select category")

            val category = vm.categories.value[pos - 1]

            vm.saveCourse(
                id = if (isEditing) courseId else null,
                title = title,
                desc = desc,
                categoryId = category.id,
                categoryName = category.name,
                lessons = lessons
            ) {
                it.onSuccess {
                    show("Saved")
                    findNavController().navigateUp()
                }.onFailure { ex ->
                    show("Error: ${ex.message}")
                }
            }
        }

        btnCancel.setOnClickListener { findNavController().navigateUp() }
    }

    private fun show(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
