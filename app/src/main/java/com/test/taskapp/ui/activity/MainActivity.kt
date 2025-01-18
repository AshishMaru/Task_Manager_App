package com.test.taskapp.ui.activity

import com.test.taskapp.databinding.ActivityMainBinding
import com.test.taskapp.model.Task
import com.test.taskapp.ui.adapter.TaskAdapter
import com.test.taskapp.ui.viewmodel.TaskViewModel
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
   // private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private var taskViewModel: TaskViewModel? = null;
    private var taskToEdit: Task? = null // Track the task being edited

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        setupRecyclerView()

        taskViewModel?.allTasks?.observe(this) { tasks ->
            adapter.submitList(tasks)
        }

        // Search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTasks(newText)
                return true
            }
        })

        // Button to add Task
        binding.btnAddTask.setOnClickListener {
            val taskName = binding.etTaskName.text.toString()
            val taskDescription = binding.etTaskDescription.text.toString()

            if (taskName.isNotEmpty() && taskDescription.isNotEmpty()) {
                if (taskToEdit != null) {
                    // Update existing task
                    val updatedTask = taskToEdit!!.copy(name = taskName, description = taskDescription)
                    taskViewModel?.updateTask(updatedTask)
                    taskToEdit = null
                    binding.btnAddTask.text = "Add Task" // Reset button text
                } else {
                    // Add new task
                    val task = Task(name = taskName, description = taskDescription)
                    taskViewModel?.insertTask(task)
                }
                binding.etTaskName.text?.clear()
                binding.etTaskDescription.text?.clear()
            } else {
                if(taskName.isEmpty()){
                    binding.etTaskName.error = "Enter Task Name"
                } else if(taskDescription.isEmpty()) {
                    binding.etTaskDescription.error = "Enter Task Description"
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onEditClick = { task -> editTask(task) },
            onDeleteClick = { task -> deleteTask(task) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun editTask(task: Task) {
        taskViewModel?.insertTask(task)
        taskToEdit = task
        binding.etTaskName.setText(task.name)
        binding.etTaskDescription.setText(task.description)
        binding.btnAddTask.text = "Edit Task" // Update button text
        // Scroll to the top of the NestedScrollView with animation
        binding.nestedScrollView.smoothScrollTo(0, 0)
    }

    private fun deleteTask(task: Task) {
        taskViewModel?.deleteTask(task)
    }
    private fun filterTasks(query: String?) {
        query?.let {
            val filteredList = taskViewModel?.allTasks?.value?.filter {
                it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
            adapter.submitList(filteredList)
        }
    }
}
