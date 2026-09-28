package com.hisabkitab.feature.categories.editor

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabkitab.feature.categories.R
import com.hisabkitab.core.data.repository.CategoryRepository
import com.hisabkitab.core.icons.CategoryIcons
import com.hisabkitab.core.model.Category
import com.hisabkitab.core.model.CategoryColors
import com.hisabkitab.core.model.TransactionType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryEditorUiState(
    val isEditing: Boolean,
    val isLoading: Boolean,
    val name: String = "",
    val type: TransactionType,
    val iconKey: String = CategoryIcons.all.first().key,
    val color: Int = CategoryColors.all.first(),
    val transactionCount: Int = 0,
    @StringRes val nameError: Int? = null,
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
) {
    val canSave: Boolean get() = !isLoading && !isSaving && name.isNotBlank()
}

@HiltViewModel(assistedFactory = CategoryEditorViewModel.Factory::class)
class CategoryEditorViewModel @AssistedInject constructor(
    @Assisted private val categoryId: Long?,
    @Assisted initialType: TransactionType,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CategoryEditorUiState(
            isEditing = categoryId != null,
            isLoading = categoryId != null,
            type = initialType,
            // Vary the starting color so new categories don't all look alike.
            color = CategoryColors.all.random(),
        ),
    )
    val uiState: StateFlow<CategoryEditorUiState> = _uiState.asStateFlow()

    init {
        if (categoryId != null) {
            viewModelScope.launch {
                val category = categoryRepository.getCategory(categoryId)
                if (category == null) {
                    _uiState.update { it.copy(isLoading = false, isFinished = true) }
                    return@launch
                }
                val count = categoryRepository.transactionCount(categoryId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = category.name,
                        type = category.type,
                        iconKey = category.iconKey,
                        color = category.color,
                        transactionCount = count,
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name.take(MAX_NAME_LENGTH), nameError = null) }

    fun onTypeChange(type: TransactionType) = _uiState.update { it.copy(type = type, nameError = null) }

    fun onIconSelected(key: String) = _uiState.update { it.copy(iconKey = key) }

    fun onColorSelected(color: Int) = _uiState.update { it.copy(color = color) }

    fun save() {
        val state = _uiState.value
        val name = state.name.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(nameError = R.string.error_name_required) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            if (categoryRepository.isNameTaken(name, state.type, categoryId)) {
                _uiState.update { it.copy(isSaving = false, nameError = R.string.error_name_taken) }
                return@launch
            }
            categoryRepository.save(
                Category(
                    id = categoryId ?: 0,
                    name = name,
                    iconKey = state.iconKey,
                    color = state.color,
                    type = state.type,
                ),
            )
            _uiState.update { it.copy(isSaving = false, isFinished = true) }
        }
    }

    fun delete() {
        val id = categoryId ?: return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            categoryRepository.delete(id)
            _uiState.update { it.copy(isSaving = false, isFinished = true) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(categoryId: Long?, initialType: TransactionType): CategoryEditorViewModel
    }

    private companion object {
        const val MAX_NAME_LENGTH = 30
    }
}
