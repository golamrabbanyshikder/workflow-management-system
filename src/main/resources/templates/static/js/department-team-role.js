/**
 * Dynamic Department-Team-Role Management JavaScript
 * Handles interactions between departments, teams, and roles
 */

class DepartmentTeamRoleManager {
    constructor() {
        this.departments = [];
        this.teams = [];
        this.roles = [];
        this.users = [];
        this.selectedDepartment = null;
        this.selectedTeam = null;
        this.selectedRole = null;
        
        this.init();
    }
    
    async init() {
        await this.loadData();
        this.setupEventListeners();
        this.initializeFilters();
    }
    
    async loadData() {
        try {
            // Load departments
            const departmentsResponse = await fetch('/api/departments');
            this.departments = await departmentsResponse.json();
            
            // Load teams
            const teamsResponse = await fetch('/api/teams');
            this.teams = await teamsResponse.json();
            
            // Load roles
            const rolesResponse = await fetch('/api/roles');
            this.roles = await rolesResponse.json();
            
            // Load users
            const usersResponse = await fetch('/api/users');
            this.users = await usersResponse.json();
            
        } catch (error) {
            console.error('Error loading data:', error);
            this.showNotification('Error loading data', 'error');
        }
    }
    
    setupEventListeners() {
        // Department selection
        document.addEventListener('change', (e) => {
            if (e.target.matches('#departmentSelect, #departmentFilter')) {
                this.handleDepartmentChange(e.target.value);
            }
        });
        
        // Team selection
        document.addEventListener('change', (e) => {
            if (e.target.matches('#teamSelect, #teamFilter')) {
                this.handleTeamChange(e.target.value);
            }
        });
        
        // Role selection
        document.addEventListener('change', (e) => {
            if (e.target.matches('#roleSelect, #roleFilter')) {
                this.handleRoleChange(e.target.value);
            }
        });
        
        // Search functionality
        document.addEventListener('input', (e) => {
            if (e.target.matches('#searchInput')) {
                this.handleSearch(e.target.value);
            }
        });
        
        // Form submissions
        document.addEventListener('submit', (e) => {
            if (e.target.matches('#teamForm, #roleForm, #userForm')) {
                this.handleFormSubmit(e);
            }
        });
    }
    
    handleDepartmentChange(departmentId) {
        this.selectedDepartment = departmentId ? parseInt(departmentId) : null;
        
        // Filter teams based on selected department
        this.filterTeams();
        
        // Update team dropdown
        this.updateTeamDropdown();
        
        // Update user assignments
        this.updateUserAssignments();
        
        // Trigger custom event
        this.dispatchEvent('departmentChanged', { departmentId });
    }
    
    handleTeamChange(teamId) {
        this.selectedTeam = teamId ? parseInt(teamId) : null;
        
        // Filter users based on selected team
        this.filterUsers();
        
        // Update user dropdown
        this.updateUserDropdown();
        
        // Trigger custom event
        this.dispatchEvent('teamChanged', { teamId });
    }
    
    handleRoleChange(roleId) {
        this.selectedRole = roleId ? parseInt(roleId) : null;
        
        // Update role permissions display
        this.updateRolePermissions();
        
        // Update user assignments
        this.updateUserAssignments();
        
        // Trigger custom event
        this.dispatchEvent('roleChanged', { roleId });
    }
    
    handleSearch(searchTerm) {
        const term = searchTerm.toLowerCase().trim();
        
        // Filter all data based on search term
        this.filterData(term);
        
        // Update all displays
        this.updateAllDisplays();
    }
    
    filterTeams() {
        if (!this.selectedDepartment) {
            this.filteredTeams = this.teams;
        } else {
            this.filteredTeams = this.teams.filter(team => 
                team.department && team.department.id === this.selectedDepartment
            );
        }
    }
    
    filterUsers() {
        if (!this.selectedTeam) {
            this.filteredUsers = this.users;
        } else {
            this.filteredUsers = this.users.filter(user => 
                user.team && user.team.id === this.selectedTeam
            );
        }
    }
    
    filterData(searchTerm) {
        if (!searchTerm) {
            this.filteredDepartments = this.departments;
            this.filteredTeams = this.teams;
            this.filteredRoles = this.roles;
            this.filteredUsers = this.users;
        } else {
            this.filteredDepartments = this.departments.filter(dept => 
                dept.name.toLowerCase().includes(searchTerm)
            );
            
            this.filteredTeams = this.teams.filter(team => 
                team.name.toLowerCase().includes(searchTerm) ||
                (team.department && team.department.name.toLowerCase().includes(searchTerm))
            );
            
            this.filteredRoles = this.roles.filter(role => 
                role.name.toLowerCase().includes(searchTerm) ||
                (role.description && role.description.toLowerCase().includes(searchTerm))
            );
            
            this.filteredUsers = this.users.filter(user => 
                user.firstName.toLowerCase().includes(searchTerm) ||
                user.lastName.toLowerCase().includes(searchTerm) ||
                user.email.toLowerCase().includes(searchTerm) ||
                user.username.toLowerCase().includes(searchTerm)
            );
        }
    }
    
    updateTeamDropdown() {
        const teamSelect = document.getElementById('teamSelect');
        if (!teamSelect) return;
        
        // Clear existing options
        teamSelect.innerHTML = '<option value="">Select Team</option>';
        
        // Add filtered teams
        this.filteredTeams.forEach(team => {
            const option = document.createElement('option');
            option.value = team.id;
            option.textContent = team.name;
            if (team.department) {
                option.textContent += ` (${team.department.name})`;
            }
            teamSelect.appendChild(option);
        });
    }
    
    updateUserDropdown() {
        const userSelect = document.getElementById('userSelect');
        if (!userSelect) return;
        
        // Clear existing options
        userSelect.innerHTML = '<option value="">Select User</option>';
        
        // Add filtered users
        this.filteredUsers.forEach(user => {
            const option = document.createElement('option');
            option.value = user.id;
            option.textContent = `${user.firstName} ${user.lastName} (${user.email})`;
            userSelect.appendChild(option);
        });
    }
    
    updateRolePermissions() {
        const permissionsContainer = document.getElementById('rolePermissions');
        if (!permissionsContainer) return;
        
        const role = this.roles.find(r => r.id === this.selectedRole);
        if (!role || !role.permissions) {
            permissionsContainer.innerHTML = '<p class="text-muted">No permissions available</p>';
            return;
        }
        
        // Display permissions
        permissionsContainer.innerHTML = role.permissions.map(permission => `
            <span class="badge bg-primary me-2 mb-2">
                <i class="fas fa-shield-alt me-1"></i>${permission}
            </span>
        `).join('');
    }
    
    updateUserAssignments() {
        const assignmentsContainer = document.getElementById('userAssignments');
        if (!assignmentsContainer) return;
        
        let assignments = this.users;
        
        // Filter by department if selected
        if (this.selectedDepartment) {
            assignments = assignments.filter(user => 
                user.userRoles && user.userRoles.some(ur => 
                    ur.department && ur.department.id === this.selectedDepartment
                )
            );
        }
        
        // Filter by team if selected
        if (this.selectedTeam) {
            assignments = assignments.filter(user => 
                user.team && user.team.id === this.selectedTeam
            );
        }
        
        // Filter by role if selected
        if (this.selectedRole) {
            assignments = assignments.filter(user => 
                user.userRoles && user.userRoles.some(ur => 
                    ur.role && ur.role.id === this.selectedRole
                )
            );
        }
        
        // Display assignments
        if (assignments.length === 0) {
            assignmentsContainer.innerHTML = '<p class="text-muted">No user assignments found</p>';
            return;
        }
        
        assignmentsContainer.innerHTML = assignments.map(user => `
            <div class="user-assignment-card">
                <div class="user-avatar">
                    ${user.firstName.charAt(0)}${user.lastName.charAt(0)}
                </div>
                <div class="user-info">
                    <div class="user-name">${user.firstName} ${user.lastName}</div>
                    <div class="user-details">
                        ${user.email} • ${user.username}
                    </div>
                    <div class="user-roles">
                        ${this.getUserRolesDisplay(user)}
                    </div>
                </div>
                <div class="assignment-actions">
                    <button class="btn btn-sm btn-outline-primary" onclick="editUserAssignment(${user.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="removeUserAssignment(${user.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `).join('');
    }
    
    getUserRolesDisplay(user) {
        if (!user.userRoles || user.userRoles.length === 0) {
            return '<span class="text-muted">No roles assigned</span>';
        }
        
        return user.userRoles.map(ur => `
            <span class="badge bg-secondary me-1">
                ${ur.role.name}
                ${ur.department ? ` (${ur.department.name})` : ''}
                ${ur.team ? ` [${ur.team.name}]` : ''}
            </span>
        `).join('');
    }
    
    updateAllDisplays() {
        this.updateTeamDropdown();
        this.updateUserDropdown();
        this.updateRolePermissions();
        this.updateUserAssignments();
    }
    
    handleFormSubmit(event) {
        const form = event.target;
        
        // Add client-side validation
        if (!this.validateForm(form)) {
            event.preventDefault();
            return;
        }
        
        // Show loading state
        this.setFormLoading(form, true);
        
        // Handle different form types
        if (form.matches('#teamForm')) {
            this.handleTeamFormSubmit(form);
        } else if (form.matches('#roleForm')) {
            this.handleRoleFormSubmit(form);
        } else if (form.matches('#userForm')) {
            this.handleUserFormSubmit(form);
        }
    }
    
    validateForm(form) {
        let isValid = true;
        const errors = [];
        
        // Remove previous error states
        form.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
        
        // Validate required fields
        form.querySelectorAll('[required]').forEach(field => {
            if (!field.value.trim()) {
                field.classList.add('is-invalid');
                errors.push(`${field.name || field.id} is required`);
                isValid = false;
            }
        });
        
        // Validate email format
        const emailFields = form.querySelectorAll('input[type="email"]');
        emailFields.forEach(field => {
            if (field.value && !this.isValidEmail(field.value)) {
                field.classList.add('is-invalid');
                errors.push('Invalid email format');
                isValid = false;
            }
        });
        
        // Show errors if any
        if (!isValid) {
            this.showNotification(errors.join(', '), 'error');
        }
        
        return isValid;
    }
    
    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    setFormLoading(form, loading) {
        const submitButton = form.querySelector('button[type="submit"]');
        if (!submitButton) return;
        
        if (loading) {
            submitButton.disabled = true;
            submitButton.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Saving...';
        } else {
            submitButton.disabled = false;
            submitButton.innerHTML = submitButton.getAttribute('data-original-text') || 'Submit';
        }
    }
    
    async handleTeamFormSubmit(form) {
        try {
            const formData = new FormData(form);
            const data = Object.fromEntries(formData);
            
            const response = await fetch('/admin/teams/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            });
            
            if (response.ok) {
                this.showNotification('Team created successfully!', 'success');
                form.reset();
                setTimeout(() => {
                    window.location.href = '/admin/teams';
                }, 1500);
            } else {
                const error = await response.text();
                this.showNotification(error || 'Failed to create team', 'error');
            }
        } catch (error) {
            console.error('Error creating team:', error);
            this.showNotification('Error creating team', 'error');
        } finally {
            this.setFormLoading(form, false);
        }
    }
    
    async handleRoleFormSubmit(form) {
        try {
            const formData = new FormData(form);
            const data = Object.fromEntries(formData);
            
            // Handle permissions array
            const permissions = Array.from(form.querySelectorAll('input[name="permissions"]:checked'))
                .map(cb => cb.value);
            data.permissions = permissions;
            
            const response = await fetch('/admin/roles/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });
            
            if (response.ok) {
                this.showNotification('Role created successfully!', 'success');
                form.reset();
                setTimeout(() => {
                    window.location.href = '/admin/roles';
                }, 1500);
            } else {
                const error = await response.text();
                this.showNotification(error || 'Failed to create role', 'error');
            }
        } catch (error) {
            console.error('Error creating role:', error);
            this.showNotification('Error creating role', 'error');
        } finally {
            this.setFormLoading(form, false);
        }
    }
    
    showNotification(message, type = 'info') {
        // Remove existing notifications
        const existingNotifications = document.querySelectorAll('.dynamic-notification');
        existingNotifications.forEach(notif => notif.remove());
        
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show dynamic-notification`;
        notification.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        // Add to page
        const container = document.querySelector('.container') || document.body;
        container.insertBefore(notification, container.firstChild);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 5000);
    }
    
    dispatchEvent(eventName, detail) {
        const event = new CustomEvent(eventName, { detail });
        document.dispatchEvent(event);
    }
    
    initializeFilters() {
        // Initialize filter states
        this.filteredDepartments = this.departments;
        this.filteredTeams = this.teams;
        this.filteredRoles = this.roles;
        this.filteredUsers = this.users;
        
        // Set up filter toggles
        const filterToggles = document.querySelectorAll('.filter-toggle');
        filterToggles.forEach(toggle => {
            toggle.addEventListener('click', () => {
                const filterType = toggle.getAttribute('data-filter');
                this.toggleFilter(filterType);
            });
        });
    }
    
    toggleFilter(filterType) {
        const filterContainer = document.getElementById(`${filterType}Filters`);
        if (!filterContainer) return;
        
        const isVisible = filterContainer.style.display !== 'none';
        filterContainer.style.display = isVisible ? 'none' : 'block';
        
        // Update toggle button
        const toggle = document.querySelector(`[data-filter="${filterType}"]`);
        if (toggle) {
            toggle.innerHTML = isVisible ? 
                '<i class="fas fa-filter me-2"></i>Show Filters' : 
                '<i class="fas fa-filter me-2"></i>Hide Filters';
        }
    }
}

// Utility functions for global access
window.editUserAssignment = function(userId) {
    window.location.href = `/admin/users/edit/${userId}`;
};

window.removeUserAssignment = function(userId) {
    if (confirm('Are you sure you want to remove this user assignment?')) {
        // Implementation depends on specific requirements
        window.location.href = `/admin/users/remove-assignment/${userId}`;
    }
};

window.toggleTeamStatus = function(teamId, currentStatus) {
    const newStatus = !currentStatus;
    if (confirm(`Are you sure you want to ${newStatus ? 'activate' : 'deactivate'} this team?`)) {
        fetch(`/admin/teams/status/${teamId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `active=${newStatus}`
        }).then(response => {
            if (response.ok) {
                location.reload();
            } else {
                alert('Failed to update team status');
            }
        });
    }
};

window.toggleRoleStatus = function(roleId, currentStatus) {
    const newStatus = !currentStatus;
    if (confirm(`Are you sure you want to ${newStatus ? 'activate' : 'deactivate'} this role?`)) {
        fetch(`/admin/roles/status/${roleId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `active=${newStatus}`
        }).then(response => {
            if (response.ok) {
                location.reload();
            } else {
                alert('Failed to update role status');
            }
        });
    }
};

// Initialize the manager when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.departmentTeamRoleManager = new DepartmentTeamRoleManager();
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DepartmentTeamRoleManager;
}