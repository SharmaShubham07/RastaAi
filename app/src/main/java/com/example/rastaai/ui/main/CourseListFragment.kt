package com.example.rastaai.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton  // Changed import
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CourseListFragment : Fragment(R.layout.fragment_course_list) {

    private val vm: CourseListViewModel by viewModels()
    private lateinit var adapter: CourseAdapter

    // views
    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private lateinit var fab: ExtendedFloatingActionButton  // Changed to ExtendedFloatingActionButton
    private lateinit var search: SearchView
    private lateinit var btnFilter: MaterialButton
    private lateinit var emptyState: View
    private lateinit var btnAddFirst: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        progress = view.findViewById(R.id.progress)
        fab = view.findViewById(R.id.fabAdd)  // Now correctly typed as ExtendedFloatingActionButton
        search = view.findViewById(R.id.searchView)
        btnFilter = view.findViewById(R.id.btnFilter)
        emptyState = view.findViewById(R.id.emptyState)
        btnAddFirst = view.findViewById(R.id.btnAddFirst)

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

        // Search listener
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                vm.setQuery(newText ?: "")
                return true
            }
        })

        btnFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Filter UI coming next", Toast.LENGTH_SHORT).show()
        }

        fab.setOnClickListener {
            navigateToAddCourse()
        }

        // Add click listener for the "Add Course" button in empty state
        btnAddFirst.setOnClickListener {
            navigateToAddCourse()
        }

        lifecycleScope.launch {
            vm.courses
                .onStart {
                    progress.isVisible = true
                    recycler.isVisible = false
                    emptyState.isVisible = false
                }
                .catch { e ->
                    progress.isVisible = false
                    recycler.isVisible = false
                    emptyState.isVisible = true
                    emptyView.text = "Failed to load courses"
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .collectLatest { list ->
                    progress.isVisible = false
                    if (list.isNullOrEmpty()) {
                        recycler.isVisible = false
                        emptyState.isVisible = true
                        emptyView.text = "No courses yet — tap + to add one"
                    } else {
                        emptyState.isVisible = false
                        recycler.isVisible = true
                        adapter.submitList(list)
                    }
                }
        }
    }

    private fun navigateToAddCourse() {
        val action = CourseListFragmentDirections.actionCourseListToAddCourse(-1)
        findNavController().navigate(action)
    }

    private fun showDeleteDialog(course: CourseEntity) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete this course?")
            .setPositiveButton("Delete") { _, _ ->
                vm.deleteCourse(course) {}
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}