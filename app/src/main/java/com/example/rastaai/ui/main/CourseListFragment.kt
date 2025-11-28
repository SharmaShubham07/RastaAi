package com.example.rastaai.ui.main

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rastaai.R
import com.example.rastaai.data.local.db.CourseEntity
import com.example.rastaai.ui.adapter.CourseAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CourseListFragment : Fragment(R.layout.fragment_course_list) {

    private val vm: CourseListViewModel by viewModels()
    private lateinit var adapter: CourseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = view.findViewById<TextView>(R.id.emptyView)

        adapter = CourseAdapter(
            onClick = { course ->
                // pass real Long id to details
                val action = CourseListFragmentDirections.actionToDetails(course.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { course ->
                showDeleteDialog(course)
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val search = view.findViewById<SearchView>(R.id.searchView)
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(text: String?): Boolean {
                vm.setQuery(text ?: "")
                return true
            }
        })

        view.findViewById<FloatingActionButton>(R.id.fabAdd)
            .setOnClickListener {
                // pass -1L explicitly to indicate "create new" (destination requires a long)
                val action = CourseListFragmentDirections.actionCourseListToAddCourse(-1L)
                findNavController().navigate(action)
            }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.courses.collectLatest { list ->
                adapter.submitList(list)
                emptyView.isVisible = list.isEmpty()
            }
        }
    }

    private fun showDeleteDialog(course: CourseEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete this course?")
            .setPositiveButton("Delete") { _, _ ->
                vm.deleteCourse(course) { /* optional completion */ }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
