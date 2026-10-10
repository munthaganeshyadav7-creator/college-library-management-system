/**
 * COLLEGE LIBRARY MANAGEMENT SYSTEM - MAIN JAVASCRIPT
 * B.Tech Technical Training (TT) Session Project
 * Vanilla JavaScript Fetch API integration with Spring Boot Backend
 */

// Determine backend API Base URL
const API_BASE_URL = (window.location.origin.includes('localhost:8080') || window.location.origin.includes('127.0.0.1:8080'))
  ? `${window.location.origin}/api`
  : 'https://college-library-backend-9skk.onrender.com/api';

// ===================================================================
// AUTHENTICATION & SESSION MANAGEMENT
// ===================================================================

function getAuthToken() {
  return localStorage.getItem('lib_token');
}

function getCurrentUser() {
  const userJson = localStorage.getItem('lib_user');
  if (!userJson) return null;
  try {
    return JSON.parse(userJson);
  } catch (e) {
    return null;
  }
}

function saveAuthSession(authData) {
  localStorage.setItem('lib_token', authData.token);
  localStorage.setItem('lib_user', JSON.stringify({
    id: authData.id,
    name: authData.name,
    email: authData.email,
    role: authData.role
  }));
}

function clearAuthSession() {
  localStorage.removeItem('lib_token');
  localStorage.removeItem('lib_user');
}

function getAuthHeaders() {
  const headers = {
    'Content-Type': 'application/json'
  };
  const token = getAuthToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

// Redirect user based on their role
function redirectByRole(role) {
  if (role === 'STUDENT') {
    window.location.href = 'student-dashboard.html';
  } else if (role === 'LIBRARIAN') {
    window.location.href = 'librarian-dashboard.html';
  } else if (role === 'ADMIN') {
    window.location.href = 'admin-dashboard.html';
  } else {
    window.location.href = 'index.html';
  }
}

// Guard page based on allowed roles
function checkPageAccess(allowedRoles = []) {
  const user = getCurrentUser();
  if (!user || !getAuthToken()) {
    window.location.href = 'login.html';
    return false;
  }
  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    alert('Access Denied: You do not have permission to view this page.');
    redirectByRole(user.role);
    return false;
  }
  return true;
}

// Logout
async function handleLogout() {
  try {
    await fetch(`${API_BASE_URL}/logout`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
  } catch (e) {
    // Ignore network error on logout
  }
  clearAuthSession();
  window.location.href = 'login.html';
}

// ===================================================================
// UI NOTIFICATIONS & ALERTS
// ===================================================================

function showAlert(containerId, message, type = 'info') {
  const container = document.getElementById(containerId);
  if (!container) return;

  const icon = type === 'success' ? '✓' : type === 'danger' ? '⚠' : 'ℹ';
  container.innerHTML = `
    <div class="alert alert-${type}">
      <strong>${icon}</strong> ${message}
    </div>
  `;
}

function clearAlert(containerId) {
  const container = document.getElementById(containerId);
  if (container) container.innerHTML = '';
}

// ===================================================================
// MODAL CONTROLS
// ===================================================================

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
  }
}

// ===================================================================
// GLOBAL NAVBAR RENDERER
// ===================================================================

function renderNavbar(activePage = '') {
  const navContainer = document.getElementById('navbar-container');
  if (!navContainer) return;

  const user = getCurrentUser();
  const isLoggedIn = !!user;

  let linksHtml = '';

  if (!isLoggedIn) {
    // Public visitor
    linksHtml = `
      <li><a href="index.html" class="${activePage === 'home' ? 'active' : ''}">Home</a></li>
      <li><a href="books.html" class="${activePage === 'books' ? 'active' : ''}">Book Catalog</a></li>
      <li><a href="login.html" class="${activePage === 'login' ? 'active' : ''}">Login</a></li>
      <li><a href="register.html" class="${activePage === 'register' ? 'active' : ''}">Register</a></li>
    `;
  } else if (user.role === 'STUDENT') {
    // Student Links
    linksHtml = `
      <li><a href="student-dashboard.html" class="${activePage === 'dashboard' ? 'active' : ''}">Dashboard</a></li>
      <li><a href="books.html" class="${activePage === 'books' ? 'active' : ''}">Explore Books</a></li>
      <li><a href="my-borrowings.html" class="${activePage === 'borrowings' ? 'active' : ''}">My Borrowings</a></li>
    `;
  } else if (user.role === 'LIBRARIAN') {
    // Librarian Links
    linksHtml = `
      <li><a href="librarian-dashboard.html" class="${activePage === 'dashboard' ? 'active' : ''}">Dashboard</a></li>
      <li><a href="manage-books.html" class="${activePage === 'manage-books' ? 'active' : ''}">Manage Inventory</a></li>
      <li><a href="issue-return.html" class="${activePage === 'issue-return' ? 'active' : ''}">Issue / Return</a></li>
      <li><a href="reports.html" class="${activePage === 'reports' ? 'active' : ''}">Audit Reports</a></li>
    `;
  } else if (user.role === 'ADMIN') {
    // Admin Links
    linksHtml = `
      <li><a href="admin-dashboard.html" class="${activePage === 'dashboard' ? 'active' : ''}">Admin Overview</a></li>
      <li><a href="manage-books.html" class="${activePage === 'manage-books' ? 'active' : ''}">Book Inventory</a></li>
      <li><a href="issue-return.html" class="${activePage === 'issue-return' ? 'active' : ''}">Issue / Return</a></li>
      <li><a href="reports.html" class="${activePage === 'reports' ? 'active' : ''}">Reports & Penalties</a></li>
    `;
  }

  let userSectionHtml = '';
  if (isLoggedIn) {
    userSectionHtml = `
      <div class="nav-user">
        <div class="user-tag">
          <span class="user-name">${user.name}</span>
          <span class="user-role">${user.role}</span>
        </div>
        <button onclick="handleLogout()" class="btn btn-sm btn-secondary" title="Sign Out">Logout</button>
      </div>
    `;
  }

  navContainer.innerHTML = `
    <nav class="navbar">
      <a href="${isLoggedIn ? (user.role === 'STUDENT' ? 'student-dashboard.html' : user.role === 'LIBRARIAN' ? 'librarian-dashboard.html' : 'admin-dashboard.html') : 'index.html'}" class="nav-brand">
        <svg viewBox="0 0 24 24"><path d="M12 3L1 9L12 15L21 10.09V17H23V9M5 13.18V17.18L12 21L19 17.18V13.18L12 17L5 13.18Z"/></svg>
        <span>College Library</span>
        ${isLoggedIn ? `<span class="badge-role">${user.role}</span>` : ''}
      </a>
      <ul class="nav-links">
        ${linksHtml}
      </ul>
      ${userSectionHtml}
    </nav>
  `;
}

// Render simple footer
function renderFooter() {
  const footerContainer = document.getElementById('footer-container');
  if (!footerContainer) return;

  footerContainer.innerHTML = `
    <footer class="footer">
      <p><strong>College Library Management System</strong> &bull; B.Tech Technical Training (TT) Session Project</p>
      <p style="margin-top: 0.25rem; font-size: 0.775rem;">Java 17 &bull; Spring Boot 3 &bull; Spring Data JPA &bull; Spring Security &bull; MySQL 8 &bull; Vanilla JS</p>
    </footer>
  `;
}

// Currency & Date formatters
function formatCurrency(amount) {
  if (amount == null) return '₹0.00';
  return '₹' + Number(amount).toFixed(2);
}

function formatDate(dateString) {
  if (!dateString) return '-';
  const parts = dateString.split('-');
  if (parts.length === 3) {
    const d = new Date(parts[0], parts[1] - 1, parts[2]);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
  return dateString;
}

// Initialize components on DOM load
document.addEventListener('DOMContentLoaded', () => {
  renderFooter();
});
