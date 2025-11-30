package com.example.rastaai.ui.main

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CourseListFragment : Fragment(R.layout.fragment_course_list) {

    private val vm: CourseListViewModel by viewModels()
    private lateinit var adapter: CourseAdapter

    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private lateinit var fab: FloatingActionButton
    private lateinit var search: SearchView
    private lateinit var emptyState: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        progress = view.findViewById(R.id.progress)
        fab = view.findViewById(R.id.fabAdd)
        search = view.findViewById(R.id.searchView)
        emptyState = view.findViewById(R.id.emptyState)

        adapter = CourseAdapter(
            onClick = { course ->
                val action = CourseListFragmentDirections.actionToDetails(course.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { course ->
                showDeleteDialog(course)
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                vm.setQuery(newText ?: "")
                return true
            }
        })

        fab.setOnClickListener {
            val action = CourseListFragmentDirections.actionCourseListToAddCourse(-1)
            findNavController().navigate(action)
        }

        lifecycleScope.launch {
            vm.courses.collectLatest { list ->
                progress.isVisible = false
                if (list.isEmpty()) {
                    emptyState.isVisible = true
                    recycler.isVisible = false
                } else {
                    emptyState.isVisible = false
                    recycler.isVisible = true
                    adapter.submitList(list)
                }
            }
        }
    }

    private fun showDeleteDialog(course: CourseEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Course")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                vm.deleteCourse(course) {}
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
