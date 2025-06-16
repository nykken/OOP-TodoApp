  function confirmDelete(message) {
      return confirm(message || 'Are you sure you want to delete this item?');
    }

  function openModal(modalId) {
    document.getElementById(modalId).classList.add('show');
  }

  function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('show');
  }

  // Close modal when clicking outside
  window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
      event.target.classList.remove('show');
    }
  }

  // Close modal with Escape key
  document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
      const openModal = document.querySelector('.modal.show');
      if (openModal) {
        openModal.classList.remove('show');
      }
    }
  });