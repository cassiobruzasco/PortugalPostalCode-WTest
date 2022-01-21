package com.cassiobruzasco.wtest.view

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cassiobruzasco.wtest.R
import com.cassiobruzasco.wtest.data.model.PostalCodeModel
import com.cassiobruzasco.wtest.databinding.HomeFragmentBinding
import com.cassiobruzasco.wtest.util.normalizeString
import com.cassiobruzasco.wtest.view.adapter.PostalCodeAdapter
import com.cassiobruzasco.wtest.view.viewmodel.DownloadState
import com.cassiobruzasco.wtest.view.viewmodel.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest

class HomeFragment : Fragment() {

    private lateinit var binding: HomeFragmentBinding

    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var dialog: Dialog
    private lateinit var postalAdapter: PostalCodeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = Dialog(requireContext(), R.style.LoadingDialog)
        binding.permissionButton.setOnClickListener { requestForPermission() }
        requestForPermission()
    }

    private fun requestForPermission() {
        val isStoragePermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (!isStoragePermissionGranted) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(WRITE_EXTERNAL_STORAGE), 0)
        } else {
            binding.permissionButton.visibility = View.GONE
            viewModel.scheduleFetch(requireContext())
        }
    }

    override fun onStart() {
        super.onStart()
        configureObservers(requireView())
        configureRecycler()
    }

    private fun configureRecycler() {
        postalAdapter = PostalCodeAdapter(::showItemDetails)
        with(binding.postalCodeRecycler) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = postalAdapter
        }
    }

    private fun configureObservers(view: View) {
        lifecycleScope.launchWhenStarted {
            viewModel.fileState.collectLatest { response ->
                when (response) {
                    is DownloadState.Idle -> Unit
                    is DownloadState.Loading -> showDialog()
                    is DownloadState.Failure -> {
                        dialog.dismiss()
                        Snackbar.make(
                            view, getString(R.string.download_failed), Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is DownloadState.Success -> viewModel.fileToModel(response.data)
                    is DownloadState.HasFile -> viewModel.fileToModel(response.data)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.fileWasRead.collectLatest { response ->
                if (response.isNullOrEmpty().not()) {
                    response?.let {
                        dialog.dismiss()
                        postalAdapter.list = response
                        updateView(response)
                    }
                }
            }
        }
    }

    private fun showItemDetails(item: PostalCodeModel) {
        Toast.makeText(
            requireContext(),
            getString(
                R.string.show_details,
                item.localeName,
                item.arteryType,
                item.arteryName,
                item.designationPostal,
                item.postalCodeComplete
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showDialog() {
        if (dialog.window != null) {
            (dialog.window as Window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setContentView(R.layout.dialog_loading_layout)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun updateView(originalList: MutableList<PostalCodeModel>) {
        binding.apply {
            postalCodeRecycler.visibility = View.VISIBLE
            postalSearch.visibility = View.VISIBLE
            postalSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        val finalText = newText.normalizeString()
                        val newList: MutableList<PostalCodeModel>
                        if (finalText.length > 3) {
                            newList = postalAdapter.list.filter {
                                it.postalCodeComplete.contains(finalText) || it.normalizedArteryName.contains(
                                    finalText
                                )
                            }.toMutableList()
                            if (newList.size > 0) {
                                postalAdapter.list = newList
                                binding.postalCodeRecycler.visibility = View.VISIBLE
                                binding.notFound.visibility = View.GONE
                            } else {
                                binding.postalCodeRecycler.visibility = View.GONE
                                binding.notFound.visibility = View.VISIBLE
                            }
                        } else {
                            binding.postalCodeRecycler.visibility = View.VISIBLE
                            binding.notFound.visibility = View.GONE
                            postalAdapter.list = originalList
                        }
                    }
                    return false
                }
            })
        }
    }
}