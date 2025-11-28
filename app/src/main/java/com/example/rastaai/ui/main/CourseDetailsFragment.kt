package com.example.rastaai.ui.main

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.rastaai.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CourseDetailsFragment : Fragment(R.layout.fragment_course_details) {

    private val vm: AddEditCourseViewModel by viewModels()
    private val args: CourseDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.detailsTitle)
        val desc = view.findViewById<TextView>(R.id.detailsDescription)
        val category = view.findViewById<TextView>(R.id.detailsCategory)
        val lessons = view.findViewById<TextView>(R.id.detailsLessons)
        val score = view.findViewById<TextView>(R.id.detailsScore)

        val courseId: Long = args.courseId

        MainScope().launch {
            val course = vm.loadCourse(courseId)
            course?.let {
                title.text = it.title
                desc.text = it.description
                category.text = "Category: ${it.categoryName ?: "N/A"}"
                lessons.text = "Lessons: ${it.lessons}"
                score.text = "Score: ${it.score}"
            }
        }
    }
}
