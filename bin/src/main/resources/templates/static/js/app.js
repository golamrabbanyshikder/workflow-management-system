// Dynamic team loading based on department selection
document.addEventListener('DOMContentLoaded', function() {
    // Initialize right panel functionality
    initRightPanel();
    
    // Existing team loading functionality
    const departmentSelect = document.getElementById('department');
    const teamSelect = document.getElementById('team');

    if (departmentSelect && teamSelect) {
        departmentSelect.addEventListener('change', function() {
            const departmentId = this.value;

            teamSelect.innerHTML = '<option value="">Select Team</option>';

            if (departmentId) {
                fetch('/api/teams?departmentId=' + departmentId)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok');
                        }
                        return response.json();
                    })
                    .then(teams => {
                        teams.forEach(team => {
                            const option = document.createElement('option');
                            option.value = team.id;
                            option.textContent = team.name;
                            teamSelect.appendChild(option);
                        });
                    })
                    .catch(error => {
                        console.error('Error loading teams:', error);
                    });
            }
        });
    }
});

// Right Panel functionality
function initRightPanel() {
    const rightPanel = document.getElementById('rightPanel');
    const toggleBtn = document.getElementById('rightPanelToggle');
    const closeBtn = document.getElementById('closePanel');
    const overlay = document.createElement('div');
    overlay.className = 'right-panel-overlay';
    document.body.appendChild(overlay);

    // Toggle panel
    function togglePanel() {
        rightPanel.classList.toggle('show');
        overlay.classList.toggle('show');
        document.body.style.overflow = rightPanel.classList.contains('show') ? 'hidden' : '';
    }

    // Close panel
    function closePanel() {
        rightPanel.classList.remove('show');
        overlay.classList.remove('show');
        document.body.style.overflow = '';
    }

    // Event listeners
    if (toggleBtn) {
        toggleBtn.addEventListener('click', togglePanel);
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', closePanel);
    }

    if (overlay) {
        overlay.addEventListener('click', closePanel);
    }

    // Close panel when pressing Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && rightPanel.classList.contains('show')) {
            closePanel();
        }
    });

    // Handle chevron rotation for collapsible sections
    const sectionHeaders = document.querySelectorAll('.section-header[data-bs-toggle="collapse"]');
    sectionHeaders.forEach(header => {
        header.addEventListener('click', function() {
            const target = document.querySelector(this.getAttribute('data-bs-target'));
            const chevron = this.querySelector('.fa-chevron-down');
            
            if (target) {
                target.addEventListener('shown.bs.collapse', function() {
                    if (chevron) chevron.style.transform = 'rotate(180deg)';
                });
                
                target.addEventListener('hidden.bs.collapse', function() {
                    if (chevron) chevron.style.transform = 'rotate(0deg)';
                });
            }
        });
    });
}